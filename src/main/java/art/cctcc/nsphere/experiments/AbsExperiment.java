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
package art.cctcc.nsphere.experiments;

import art.cctcc.nsphere.Individual;
import art.cctcc.nsphere.enums.ESMode;
import static art.cctcc.nsphere.Parameters.*;
import static art.cctcc.nsphere.Tools.time_elapsed;
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
 * @param <I> Generic of Individual
 */
abstract public class AbsExperiment<I extends Individual> {

  public List<Double> evals
          = Collections.synchronizedList(new ArrayList<>());

  public final int n;
  public final ESMode mode;
  public final int mu;
  public final int lambda;

  public final double init_sigma;

  protected List<I> parents;

  public int iterations;

  public AbsExperiment(int n, ESMode mode, int mu, int lambda, double init_sigma) {

    this.n = n;
    this.mode = mode;
    this.mu = mu;
    this.lambda = lambda;
    this.init_sigma = init_sigma;
  }

  abstract public String getTitle();

  abstract protected double calcEval(I idv);

  abstract protected I mutation(int i);

  abstract protected boolean goal();

  abstract protected List<I> generate();

  public String run(Path csv) {

    var start = Instant.now();

    var output = new ArrayList<String>();
    output.add("Iteration,Average,"
            + IntStream.range(0, mu)
                    .mapToObj(i -> "X" + i)
                    .collect(Collectors.joining(",")) + ","
            + IntStream.range(0, lambda)
                    .mapToObj(i -> "Y" + i)
                    .collect(Collectors.joining(",")));

    // ES loop
    this.parents = generate();
    var finished = false;
    while (this.iterations < UpperLimit && !finished) {

      var offspring = IntStream.range(0, lambda)
              .mapToObj(this::mutation)
              .toList();

      var avg = parents.stream()
              .mapToDouble(this::getEval)
              .average().getAsDouble();

      var line = String.format("%d,%.5f,", this.iterations, avg)
              + membersToString(parents) + ","
              + membersToString(offspring);

      output.add(line);

      this.parents = Stream.concat(offspring.stream(),
              mode == ESMode.Plus ? parents.stream() : Stream.empty())
              .sorted(Comparator.comparing(this::getEval))
              .limit(mu)
              .toList();

      finished = goal();

      this.iterations++;
    }

    try {
      Files.write(csv, output);
    } catch (IOException ex) {
      Logger.getLogger(AbsExperiment.class.getName()).log(Level.SEVERE, null, ex);
    }

    return time_elapsed(start);
  }

  public double getEval(I idv) {

    if (idv.getEval() == -1) {
      var eval = this.calcEval(idv);
      idv.setEval(eval);
      evals.add(eval);
    }
    return idv.getEval();
  }

  public String membersToString(List<I> members) {

    return members.stream()
            .map(this::getEval)
            .map(eval -> String.format("%.3f", eval))
            .collect(Collectors.joining(","));
  }

  public String getESMode() {

    return this.mode.getMode(mu, lambda);
  }
}
