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

import art.cctcc.nsphere.enums.ESMode;
import static art.cctcc.nsphere.Parameters.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
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
public abstract class Experiment {

  public static String TYPE;

  public List<Double> evals
          = Collections.synchronizedList(new ArrayList<>());

  public final int n;
  public final ESMode mode;
  public final int mu;
  public final int lambda;

  public double stddev;
  public int iterations;

  protected List<Individual> parents;

  public Experiment(int n, ESMode mode, int mu, int lambda, double stddev) {

    this.n = n;
    this.mode = mode;
    this.mu = mu;
    this.lambda = lambda;
    this.stddev = stddev;
    System.out.println(getTitle());
  }

  protected abstract double calcEval(Individual idv);

  protected abstract double[] mutation(int i);

  public String run(Path csv) {

    var start = Instant.now();

    parents = Stream.generate(() -> Individual.generate(n))
            .limit(mu).toList();

    // ES loop
    var output = new ArrayList<String>();
    output.add("Iteration,Average,"
            + IntStream.range(0, mu)
                    .mapToObj(i -> "X" + i)
                    .collect(Collectors.joining(",")) + ","
            + IntStream.range(0, lambda)
                    .mapToObj(i -> "Y" + i)
                    .collect(Collectors.joining(",")));
    var finished = false;

    do {

      finished = this.iterations++ >= 10000000
              || parents.stream()
                      .map(Individual::getEval)
                      .filter(eval -> eval > -1)
                      .anyMatch(eval -> eval <= 0.0005);

      var offspring = IntStream.generate(() -> rngInt(mu))
              .limit(lambda)
              .mapToObj(this::mutation)
              .map(Individual::new)
              .toList();

      var avg = parents.stream()
              .mapToDouble(this::getEval)
              .average().getAsDouble();

      var line = String.format("%d,%.3f,", this.iterations, avg)
              + membersToString(parents) + ","
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
      Logger.getLogger(ExperimentFSS.class.getName()).log(Level.SEVERE, null, ex);
    }

    return time_elapsed(start);
  }

  public double getEval(Individual idv) {

    if (idv.getEval() == -1) {
      idv.setEval(this.calcEval(idv));
      evals.add(idv.getEval());
    }
    return idv.getEval();
  }

  public String membersToString(List<Individual> members) {

    return members.stream()
            .map(this::getEval)
            .map(eval -> String.format("%.3f", eval))
            .collect(Collectors.joining(","));
  }

  public String getESMode() {

    return String.format("(%d%s%d)", this.mu, this.mode.symbol, this.lambda);
  }

  public String getTitle() {

    return String.format("%d-dimensional sphere %s experiment: mode=%s, stddev=%.2f",
            n, TYPE, getESMode(), stddev);
  }
}
