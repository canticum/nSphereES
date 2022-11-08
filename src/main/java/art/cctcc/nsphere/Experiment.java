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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

  private int n;
  private ESMode mode;
  double stddev;

  private int mu = 1;
  private int lambda = 1;

  public int generation;

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
            .map(eval -> String.format("%.2f", eval))
            .collect(Collectors.joining(", "));
  }

  public List<String> run() throws IOException {

    var start = Instant.now();

    var parent = new Object() {

      List<Individual> members = Stream.generate(() -> Individual.generate(n))
              .limit(mu).toList();
    };

    // ES loop
    var output = new ArrayList<String>();
    var finished = false;

    do {
      var avg = parent.members.stream()
              .mapToDouble(this::calcEval)
              .average().getAsDouble();

//      System.out.printf("Average eval = %.3f (g=%d)\n", avg, ++generation);
      if (avg <= 0.0005 || ++this.generation >= 10000000)
        finished = true;

      var offspring = IntStream.generate(() -> rngInt(mu))
              .limit(lambda)
              .mapToObj(i -> parent.members.get(i).getChromosome())
              .map(chromosome -> Arrays.stream(chromosome).map(gene -> gene += rngGaussian(stddev)).toArray())
              .map(Individual::new)
              .toList();

      output.add(membersToString(parent.members) + " | " + membersToString(offspring));

      parent.members = Stream.concat(offspring.stream(), parent.members.stream())
              .limit(mode == ESMode.Plus ? lambda + mu : lambda)
              .sorted(Comparator.comparing(this::calcEval))
              .limit(mu)
              .toList();

    } while (!finished);

    var time_elapsed = Duration.between(start, Instant.now());
    var hours = time_elapsed.toHoursPart();
    output.add(String.format("Time elapsed = %s%02d m %02d s",
            (hours > 0) ? time_elapsed.toHoursPart() + " h " : "",
            time_elapsed.toMinutesPart(), time_elapsed.toSecondsPart()));

    return output;
  }

}
