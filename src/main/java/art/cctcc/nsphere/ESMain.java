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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
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

    System.out.printf(
            """
            *******************************************
            %s
            Mode=%s, RNG=%s, Seed=%d
            *******************************************
            """,
            n + "-dimensional Sphere Model",
            "(1+1)", rng, seed);

    var folder = String.format("log-n%d-%s_%d", n, "(1+1)", seed);
    var path = Path.of(System.getProperty("user.dir"), "es_data", folder);
    Files.createDirectories(path);

    var start = Instant.now();

    IntStream.rangeClosed(1, 10)
            .forEach(i -> {
              System.out.println("Run#" + i);
              Stream.of(0.01, 0.1, 1.0)
                      .map(stddev
//                              -> new Experiment(n, ESMode.Plus, 1, 1, stddev))
                      ->new ExperimentUncorrelatedNStepSize(n, ESMode.Plus, 1, 1, stddev, 0))
                      .peek(e -> System.out.println(e.run(path.resolve(String.format("run_%d(dev=%.2f).csv", i, e.stddev)))))
                      .forEach(e -> System.out.printf("Iterations = %s, eval sizes = %s\n", e.iterations, e.evals.size()));

              var time_elapsed = Duration.between(start, Instant.now());
              var hours = time_elapsed.toHoursPart();
              System.out.printf("Time elapsed = %s%02d m %02d s\n", (hours > 0) ? time_elapsed.toHoursPart() + " h " : "",
                      time_elapsed.toMinutesPart(), time_elapsed.toSecondsPart());
            });
  }
}
