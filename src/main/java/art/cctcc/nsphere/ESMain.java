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
import art.cctcc.nsphere.enums.ESType;
import art.cctcc.nsphere.enums.RNG;
import art.cctcc.nsphere.enums.ESMode;
import static art.cctcc.nsphere.Parameters.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
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

    final var seed = getEpochMilli();
    initRandom(seed, RNG.Xoshiro256PlusPlus);

    final var n = 10;
    final var run = 10;
    final var init_sigmas = List.of(0.01, 0.1, 1.0);

    final var exp = ESType.UNSS;
    final var mode = ESMode.Plus;
    final var mu = 1;
    final var lambda = 1;

    //UNSS
    final var tau = 1e-7 / Math.sqrt(2 * Math.sqrt(n));
    final var tau_prime = 1 / Math.sqrt(2 * n);
    final var epsilon0 = 1e-4;

    //OneFive
    final var g = 100;
    final var a = 0.817;

    Function<Double, AbsExperiment> getExperiment = sigma -> switch (exp) {
      case FSS ->
        new ExperimentFSS(n, mode, mu, lambda, sigma);
      case UNSS ->
        new ExperimentUNSS(n, mode, mu, lambda, sigma, tau, tau_prime, epsilon0);
      default ->
        new ExperimentOneFive(n, mode, mu, lambda, sigma, g, a);
    };

    System.out.printf("""
            %s
            %s: %s, %s
            RNG=%s, Seed=%d
            """, "*".repeat(80),
            n + "-dimensional Sphere Model",
            mode.getMode(mu, lambda), exp.description,
            Rng, seed);

    var folder = String.format("n%d-%s-%s_%d", n, exp.description, mode.getMode(mu, lambda), seed);
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
              n, mode.getMode(mu, lambda), exp.description, sigma);
      var plot = new Plot(title);
      System.out.println();
      for (int i = 0; i < run; i++) {
        var iteration = iterations.get(i).get(sigma);
        var limit = limits.get(sigma)[1] == UpperLimit ? 100 : limits.get(sigma)[1] * 3 / 2;
        var data = readCSV(path.resolve(String.format("run_%d(sigma=%.2f).csv", i + 1, sigma)), limit + 1);
        plot.add(String.format("Run#%2d%s", i + 1, data.xData().size() > limit
                ? (iteration == UpperLimit ? "*" : " (" + iteration + ")") : ""),
                data.xData(), data.yData());
      }
      plot.show(path.resolve(String.format("plot(sigma=%.2f).png", sigma)));
    });
  }
}
