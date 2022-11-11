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
    var stddevs = List.of(0.01, 0.1, 1.0);
    var mode = ESMode.Plus;
    var mu = 1;
    var lambda = 1;

    //UNSS
    var epsilon0 = 0.0001;

    //OneFive
    var g = 10;
    var a = 0.817;

    var exp = ESType.OneFive;
    final var type = exp.description;

    Function<Double, Experiment> getExperiment = stddev -> switch (exp) {
      case FSS ->
        new ExperimentFSS(n, mode, mu, lambda, stddev);
      case UNSS ->
        new ExperimentUNSS(n, mode, mu, lambda, stddev, epsilon0);
      default ->
        new ExperimentOneFive(n, mode, mu, lambda, stddev, g, a);
    };

    System.out.printf(
            """
            %s
            %s, %s%s
            RNG=%s, Seed=%d
            """, "*".repeat(80),
            n + "-dimensional Sphere Model", type, mode.getMode(mu, lambda),
            rng, seed);

    var folder = String.format("log-n%d-%s-%s_%d", n, type, mode.getMode(mu, lambda), seed);
    var path = Path.of(System.getProperty("user.dir"), "es_data", folder);
    Files.createDirectories(path);

    var start = Instant.now();

    var iterations = IntStream.rangeClosed(1, run)
            .peek(i -> System.out.println("*".repeat(80) + "\nRun#" + i))
            .mapToObj(i
                    -> stddevs.stream()
                    .map(getExperiment)
                    .peek(e -> System.out.printf(
                    """
                              
                    %s
                    %s
                    Iterations = %s, eval sizes = %s
                    """, e.getTitle(),
                    e.run(path.resolve(String.format("run_%d(dev=%.2f).csv", i, e.stddev))),
                    e.iterations, e.evals.size()))
                    .collect(Collectors.toMap(e -> e.stddev, e -> e.iterations))
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

    stddevs.forEach(stddev -> {
      var title = String.format("%d-Dimensional Sphere Experiment: %s%s, stddev=%.2f",
              n, type, mode.getMode(mu, lambda), stddev);
      var plot = new Plot(title, mode.getMode(mu, lambda), stddev);
      System.out.println();
      for (int i = 1; i <= run; i++) {
        var limit = limits.get(stddev)[1] == UpperLimit ? 100 : limits.get(stddev)[1] * 3 / 2;
        var data = readCSV(path.resolve(String.format("run_%d(dev=%.2f).csv", i, stddev)), limit + 1);
        plot.add(String.format("run#%d%s", i, data.xData().size() > limit ? "*" : ""),
                data.xData(), data.yData());
      }
      plot.show(path.resolve(String.format("plot(dev=%.2f).png", stddev)));
    });
  }
}
