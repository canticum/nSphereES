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

import art.cctcc.nsphere.Parameters.*;
import static art.cctcc.nsphere.Parameters.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;
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
    var epsilon0 = 0.0001;
    var exp = "unss";
    var type = ExperimentUNSS.TYPE;

    Function<Double, Experiment> getExperiment = stddev -> switch (exp) {
      case "fss" ->
        new ExperimentFSS(n, ESMode.Plus, 1, 1, stddev);
      default -> //"unss"
        new ExperimentUNSS(n, mode, mu, lambda, stddev, epsilon0);
    };

    System.out.printf(
            """
            *******************************************
            %s, %s%s
            RNG=%s, Seed=%d
            *******************************************
            """,
            n + "-dimensional Sphere Model", type, mode.getMode(mu, lambda),
            rng, seed);

    var folder = String.format("log-n%d-%s-%s_%d", n, type, mode.getMode(mu, lambda), seed);
    var path = Path.of(System.getProperty("user.dir"), "es_data", folder);
    Files.createDirectories(path);

    var start = Instant.now();

    IntStream.rangeClosed(1, run).forEach(i -> {
      System.out.println("Run#" + i);
      stddevs.stream()
              .map(getExperiment)
              .peek(e -> System.out.println(e.run(path.resolve(String.format("run_%d(dev=%.2f).csv", i, e.stddev)))))
              .forEach(e -> System.out.printf("Iterations = %s, eval sizes = %s\n", e.iterations, e.evals.size()));
      System.out.println(time_elapsed(start));
    });

    stddevs.forEach(stddev -> {
      var title = String.format("%d-dimensional sphere %s experiment: mode=%s, stddev=%.2f",
              n, type, mode.getMode(mu, lambda), stddev);
      var plot = new Plot(title, mode.getMode(mu, lambda), stddev);
      for (int i = 1; i <= run; i++) {
        var data = readCSV(path.resolve(String.format("run_%d(dev=%.2f).csv", i, stddev)));
        plot.add(String.format("run#%d%s", i, data.xData().size() >= Iter_limit ? "*" : ""),
                data.xData(), data.yData());
      }
      plot.show(path.resolve(String.format("dev=%.2f).png", stddev)));
    });
  }
}
