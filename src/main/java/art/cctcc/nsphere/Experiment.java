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

  public double getEval(Individual idv) {

    if (idv.getEval() == -1) {
      idv.setEval(this.calcEval(idv));
      evals.add(idv.getEval());
    }
    return idv.getEval();
  }

  public double calcEval(Individual idv) {
    
    return Arrays.stream(idv.getChromosome()).map(i -> i * i).sum();
  }

  public String membersToString(List<Individual> members) {

    return members.stream()
            .map(this::getEval)
            .map(eval -> String.format("%.3f", eval))
            .collect(Collectors.joining(", "));
  }

  List<Individual> parents;

  public String run(Path csv) {
    var start = Instant.now();

    parents = Stream.generate(() -> Individual.generate(n))
            .limit(mu).toList();

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
      var avg = parents.stream()
              .mapToDouble(this::getEval)
              .average().getAsDouble();

      if (avg <= 0.0005 || ++this.iterations >= 10000000)
        finished = true;

      var offspring = IntStream.generate(() -> rngInt(mu))
              .limit(lambda)
              .mapToObj(this::mutation)
              .map(Individual::new)
              .toList();

      var line = String.format("%d, %.3f, ", iteration++, avg)
              + membersToString(parents) + ", "
              + membersToString(offspring);

      output.add(line);

      parents = Stream.concat(offspring.stream(), parents.stream())
              .limit(mode == ESMode.Plus ? lambda + mu : lambda)
              .sorted(Comparator.comparing(this::getEval))
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

  public double[] mutation(int i) {

    var chromosome = parents.get(i).getChromosome();

    return Arrays.stream(chromosome)
            .map(gene -> gene + rngGaussian(stddev))
            .toArray();
  }
}
