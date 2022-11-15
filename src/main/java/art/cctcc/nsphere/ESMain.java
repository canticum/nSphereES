/*
 * Copyright 2022 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package art.cctcc.nsphere;

import art.cctcc.nsphere.experiments.ExperimentFSS;
import art.cctcc.nsphere.experiments.ExperimentOneFive;
import art.cctcc.nsphere.experiments.ExperimentUNSS;
import art.cctcc.nsphere.experiments.AbsExperiment;
import static art.cctcc.nsphere.Tools.time_elapsed;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ESMain {

  public static void main(String... args) throws IOException {

    final var params = new Parameters(args);

    final var seed = params.seed;

    final var n = params.n;
    final var run = params.run;
    final var init_sigmas = params.init_sigmas;
    final var upper_limit = params.upper_limit;

    final var type = params.type;
    final var mode = params.mode;
    final var mu = params.mu;
    final var lambda = params.lambda;

    //UNSS
    final var tau = params.tau;
    final var tau_prime = params.tau_prime;
    final var epsilon0 = params.epsilon0;

    //OneFive
    final var g = params.g;
    final var a = params.a;

    Function<Double, AbsExperiment> getExperiment = sigma -> switch (type) {
      case FSS ->
        new ExperimentFSS(n, mode, mu, lambda, sigma, upper_limit);
      case UNSS ->
        new ExperimentUNSS(n, mode, mu, lambda, sigma, tau, tau_prime, epsilon0, upper_limit);
      default ->
        new ExperimentOneFive(n, mode, mu, lambda, sigma, g, a, upper_limit);
    };

    System.out.println("*".repeat(80));
    System.out.println(params);

    var folder = String.format("n%d-%s-%s_%d", n, type.description, mode.getMode(mu, lambda), seed);
    var path = Path.of(System.getProperty("user.dir"), "es_data", folder);
    Files.createDirectories(path);

    var start = Instant.now();

    var iterations = IntStream.range(0, run)
            .peek(i -> System.out.printf("""
                                         %s
                                         Run#%2d
                                         """, "*".repeat(80), i + 1))
            .mapToObj(i
                    -> init_sigmas.stream()
                    .map(getExperiment)
                    .peek(e -> System.out.printf(
                    """
                    \n%s
                    %s
                    Iterations = %s, eval sizes = %s
                    """, e.getTitle(),
                    e.run(path.resolve(String.format("run_%d(sigma=%.2f).csv", i + 1, e.init_sigma))),
                    e.iterations, e.evals.size()))
                    .collect(Collectors.toMap(e -> e.init_sigma, e -> e.iterations))
            ).toList();
    System.out.println("*".repeat(80));
    System.out.println(time_elapsed(start));

    var limits = iterations.get(0).entrySet().stream()
            .map(e -> Map.entry(e.getKey(), new int[]{e.getValue(), 0}))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    for (int i = 1; i < run; i++) {
      for (var e : iterations.get(i).entrySet()) {
        var key = e.getKey();
        if (e.getValue() > limits.get(key)[0]) {
          limits.get(key)[1] = limits.get(key)[0];
          limits.get(key)[0] = e.getValue();
        } else if (e.getValue() > limits.get(key)[1])
          limits.get(key)[1] = e.getValue();
      }
    }

    init_sigmas.forEach(sigma -> {
      var title = String.format("%d-Dimensional Sphere Model: %s, %s, sigma=%.2f",
              n, mode.getMode(mu, lambda), type.description, sigma);
      var plot = new Plot(title);
      System.out.println();
      for (int i = 0; i < run; i++) {
        var iteration = iterations.get(i).get(sigma);
        var limit = limits.get(sigma)[1] == params.upper_limit ? 100 : limits.get(sigma)[1] * 3 / 2;
        var data = Plot.readCSV(path.resolve(String.format("run_%d(sigma=%.2f).csv", i + 1, sigma)), limit + 1);
        plot.add(String.format("Run#%2d%s", i + 1, data.xData().size() > limit
                ? (iteration == params.upper_limit ? "*" : " (" + iteration + ")") : ""),
                data.xData(), data.yData());
      }
      plot.show(path.resolve(String.format("plot(sigma=%.2f).png", sigma)));
    });
  }
}
