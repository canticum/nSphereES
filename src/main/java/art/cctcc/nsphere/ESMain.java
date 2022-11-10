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
import static art.cctcc.nsphere.Parameters.getEpochMilli;
import static art.cctcc.nsphere.Parameters.initRandom;
import static art.cctcc.nsphere.Parameters.rng;
import com.opencsv.CSVReaderHeaderAwareBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    var mode = ESMode.Plus;
    var mu = 1;
    var lambda = 1;
    var type = ExperimentUNSS.TYPE;

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

    IntStream.rangeClosed(1, run)
            .forEach(i -> {
              System.out.println("Run#" + i);
              Stream.of(0.01, 0.1, 1.0)
                      //.map(stddev -> new ExperimentFSS(n, ESMode.Plus, 1, 1, stddev))
                      .map(stddev -> new ExperimentUNSS(n, mode, mu, lambda, stddev, 0.0001))
                      .peek(e -> System.out.println(e.run(path.resolve(String.format("run_%d(dev=%.2f).csv", i, e.stddev)))))
                      .forEach(e -> System.out.printf("Iterations = %s, eval sizes = %s\n", e.iterations, e.evals.size()));
              var time_elapsed = Duration.between(start, Instant.now());
              var hours = time_elapsed.toHoursPart();
              System.out.printf("Time elapsed = %s%02d m %02d s\n", (hours > 0) ? time_elapsed.toHoursPart() + " h " : "",
                      time_elapsed.toMinutesPart(), time_elapsed.toSecondsPart());
            });

    Stream.of(0.01, 0.1, 1.0)
            .forEach(stddev -> {
              var title = String.format("%d-dimensional sphere %s experiment: mode=%s, stddev=%.2f",
                      n, type, mode.getMode(mu, lambda), stddev);
              var plot = new Plot(title, mode.getMode(mu, lambda), stddev);
              for (int i = 1; i <= run; i++) {
                var data = readcsv(path.resolve(String.format("run_%d(dev=%.2f).csv", i, stddev)));
                plot.add("run#" + i, data.xData(), data.yData());
              }
              plot.show(path.resolve(String.format("dev=%.2f).png", stddev)));
            });
  }

  static Data readcsv(Path path) {

    System.out.println("Reading " + path);
    var xData = new ArrayList<Integer>();
    var yData = new ArrayList<Double>();
    try ( var reader = new FileReader(path.toFile());
             var rha = new CSVReaderHeaderAwareBuilder(reader).build()) {
      do {
        var row = rha.readMap();
        if (row == null)
          break;
        var iteration = Integer.valueOf(row.get("Iteration"));
        var average = Double.valueOf(row.get("Average"));
//        if (iteration > 100000)
//          break;
        xData.add(iteration);
        yData.add(average);
      } while (true);
    } catch (IOException | CsvValidationException ex) {
      Logger.getLogger(ESMain.class.getName()).log(Level.SEVERE, null, ex);
    }
    return new Data(xData, yData);
  }

  record Data(List<Integer> xData, List<Double> yData) {

  }
}
