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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Experiment {

  public List<Double> evals
          = Collections.synchronizedList(new ArrayList<>());

  final public int n;
  final public ESMode mode;
  final int mu;
  final int lambda;

  public double stddev;
  public int iterations;

  public Experiment(int n, ESMode mode, int mu, int lambda, double stddev) {

    this.n = n;
    this.mode = mode;
    this.mu = mu;
    this.lambda = lambda;
    this.stddev = stddev;
    System.out.printf(
            """
            %d-dimensional sphere experiment constructed: mode=[%d%s%d], stddev=%f
            """, n,
            mu, mode.symbol, lambda,
            stddev);
  }

  public String getESMode() {

    return String.format("(%d%s%d)", this.mu, this.mode.symbol, this.lambda);
  }

  public double calcEval(Individual idv) {

    if (idv.getEval() == -1) {
      idv.setEval(Arrays.stream(idv.getChromosome()).map(i -> i * i).sum());
      evals.add(idv.getEval());
    }
    return idv.getEval();
  }

  public String membersToString(List<Individual> members) {

    return members.stream()
            .map(this::calcEval)
            .map(eval -> String.format("%.3f", eval))
            .collect(Collectors.joining(", "));
  }

  public String run(Path csv) {
    var start = Instant.now();

    var parent = new Object() {

      List<Individual> members = Stream.generate(() -> Individual.generate(n))
              .limit(mu).toList();
    };

    // ES loop
    var output = new ArrayList<String>();
    output.add("Iteration, Average, "
            + IntStream.of(0, mu)
                    .mapToObj(i -> "X" + i)
                    .collect(Collectors.joining(", ")) + ", "
            + IntStream.of(0, lambda)
                    .mapToObj(i -> "Y" + i)
                    .collect(Collectors.joining(", ")));
    var finished = false;
    var iteration = 0;

    do {
      var avg = parent.members.stream()
              .mapToDouble(this::calcEval)
              .average().getAsDouble();

      if (avg <= 0.0005 || ++this.iterations >= 10000000)
        finished = true;

      var offspring = IntStream.generate(() -> rngInt(mu))
              .limit(lambda)
              .mapToObj(i -> parent.members.get(i).getChromosome())
              .map(this::mutation)
              .map(Individual::new)
              .toList();

      var line = String.format("%d, %.3f, ", iteration++, avg)
              + membersToString(parent.members) + ", "
              + membersToString(offspring);

      output.add(line);

      parent.members = Stream.concat(offspring.stream(), parent.members.stream())
              .limit(mode == ESMode.Plus ? lambda + mu : lambda)
              .sorted(Comparator.comparing(this::calcEval))
              .limit(mu)
              .toList();

    } while (!finished);

    try {
      Files.write(csv, output);
    } catch (IOException ex) {
      Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
    }

    var time_elapsed = Duration.between(start, Instant.now());
    var hours = time_elapsed.toHoursPart();
    return String.format("Time elapsed = %s%02d m %02d s",
            (hours > 0) ? time_elapsed.toHoursPart() + " h " : "",
            time_elapsed.toMinutesPart(), time_elapsed.toSecondsPart());
  }

  public double[] mutation(double[] chromosome) {

    return Arrays.stream(chromosome)
            .map(gene -> gene += rngGaussian(stddev))
            .toArray();
  }
}
