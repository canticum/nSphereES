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
import static art.cctcc.nsphere.Tools.rngGaussian;
import static art.cctcc.nsphere.Tools.rngInt;
import art.cctcc.nsphere.enums.ESType;
import java.util.Arrays;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ExperimentOneFive extends NDimSphere {

  private int g;
  private double a;
  private int g_s;
  private int mutation_count;

  public ExperimentOneFive(int n, ESMode mode,
          int mu, int lambda, double init_sigma, int g, double a) {

    super(n, mode, mu, lambda, init_sigma);
    this.g = g;
    this.a = a;
  }

  public ExperimentOneFive(int n, ESMode mode,
          int mu, int lambda, double sigma, int g) {

    this(n, mode, mu, lambda, sigma, g, 0.817);
  }

  @Override
  public String getTitle() {

    return String.format("%s: %s, %s, initial sigma=%.2f",
            super.getTitle(), getESMode(), ESType.OneFive.description, init_sigma);
  }

  @Override
  public Individual mutation(int offspring_index) {

    this.mutation_count++;
    var parent = parents.get(rngInt(mu));
    var chromosome = new double[n];
    Arrays.setAll(chromosome, i -> parent.chromosome[i]
            + rngGaussian(parent.sigmas[0]));
    var sigmas = Arrays.copyOf(parent.sigmas, 1);
    var offspring = new Individual(chromosome, sigmas);
    if (this.getEval(offspring) < this.getEval(parent))
      this.g_s++;
    if (this.mutation_count >= this.g) {
      var p_s = 1.0 * this.g_s / this.g;
      for (int i = 0; i < offspring.sigmas.length; i++) {
        if (p_s > 0.2) {
          parent.sigmas[i] /= a;
          offspring.sigmas[i] /= a;
        } else if (p_s < 0.2) {
          parent.sigmas[i] *= a;
          offspring.sigmas[i] *= a;
        }
      }
      this.g_s = 0;
      this.mutation_count = 0;
    }
    return offspring;
  }
}
