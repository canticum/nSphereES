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
import static art.cctcc.nsphere.Parameters.rngGaussian;
import static art.cctcc.nsphere.Parameters.rngInt;
import art.cctcc.nsphere.enums.ESType;
import java.util.Arrays;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ExperimentOneFive extends AbsExpNDimSphere {

  private double[] sigma_prime;
  private int g;
  private double a;
  private int[] g_s;

  public ExperimentOneFive(int n, ESMode mode,
          int mu, int lambda, double sigma, int g, double a) {

    super(n, mode, mu, lambda, sigma);
    this.sigma_prime = new double[lambda];
    Arrays.fill(this.sigma_prime, sigma);
    this.g = g;
    this.a = a;
    this.g_s = new int[lambda];
    Arrays.fill(g_s, 0);
  }

  public ExperimentOneFive(int n, ESMode mode,
          int mu, int lambda, double sigma, int g) {

    this(n, mode, mu, lambda, sigma, g, 0.817);
  }

  @Override
  public String getTitle() {

    return String.format("%s: %s%s, initial sigma=%.2f",
            super.getTitle(), ESType.OneFive.description, getESMode(), sigma);
  }

  public void updateSigma(int offspring_index) {

    var p_s = 1.0 * g_s[offspring_index] / g;
    if (p_s > 0.2)
      this.sigma_prime[offspring_index] /= a;
    else if (p_s < 0.2)
      this.sigma_prime[offspring_index] *= a;
    this.g_s[offspring_index] = 0;
  }

  @Override
  public Individual mutation(int offspring_index) {

    var select = rngInt(mu);
    if (this.iterations % this.g == 0)
      this.updateSigma(offspring_index);
    var chromosome = parents.get(select).chromosome;
    var mutant_idv = new Individual(Arrays.stream(chromosome)
            .map(gene -> gene + rngGaussian(sigma_prime[offspring_index]))
            .toArray());
    if (this.getEval(mutant_idv) < this.getEval(parents.get(select)))
      g_s[offspring_index]++;
    return mutant_idv;
  }
}
