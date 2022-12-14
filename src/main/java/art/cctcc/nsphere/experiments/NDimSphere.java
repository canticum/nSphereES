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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public abstract class NDimSphere
        extends AbsExperiment<Individual> {

  public NDimSphere(int n, ESMode mode, int mu, int lambda, double sigma, int upper_limit) {

    super(n, mode, mu, lambda, sigma, upper_limit);
  }

  @Override
  public String getTitle() {

    return String.format("%d-Dimensional Sphere Model", n);
  }

  @Override
  public double calcEval(Individual idv) {

    return Arrays.stream(idv.chromosome).map(i -> i * i).sum();
  }

  @Override
  protected boolean goal() {

    return parents.stream()
            .map(Individual::getEval)
            .filter(eval -> eval != -1)
            .anyMatch(eval -> eval <= 0.0005);
  }

  @Override
  protected List<Individual> generate() {

    return Stream.generate(() -> {
      var chromosome = new double[n];
      Arrays.fill(chromosome, 1.0);
      var sigmas = new double[1];
      Arrays.fill(sigmas, init_sigma);
      return new Individual(chromosome, sigmas);
    })
            .limit(mu).toList();
  }
}
