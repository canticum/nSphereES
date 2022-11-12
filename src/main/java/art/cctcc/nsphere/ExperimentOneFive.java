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
import static art.cctcc.nsphere.Parameters.rngGaussian;
import static art.cctcc.nsphere.Parameters.rngInt;
import art.cctcc.nsphere.enums.ESType;
import java.util.Arrays;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ExperimentOneFive extends ExperimentFSS {

  private double[] stddev_prime;
  private int g;
  private double a;
  private int[] g_s;

  public ExperimentOneFive(int n, ESMode mode,
          int mu, int lambda, double stddev, int g, double a) {

    super(n, mode, mu, lambda, stddev);
    this.stddev_prime = new double[lambda];
    Arrays.fill(this.stddev_prime, stddev);
    this.g = g;
    this.a = a;
    this.g_s = new int[lambda];
    Arrays.fill(g_s, 0);
  }

  public ExperimentOneFive(int n, ESMode mode,
          int mu, int lambda, double stddev, int g) {

    this(n, mode, mu, lambda, stddev, g, 0.817);
  }

  @Override
  public String getTitle() {

    return String.format("%s: %s%s, initial stddev=%.2f",
            super.getTitle(), ESType.OneFive.description, getESMode(), stddev);
  }

  public void updateStddev(int idv) {

    var p_s = 1.0 * g_s[idv] / g;
    if (p_s > 0.2)
      this.stddev_prime[idv] /= a;
    else if (p_s < 0.2)
      this.stddev_prime[idv] *= a;
    this.g_s[idv] = 0;
  }

  @Override
  public Individual mutation(int idv) {

    var select = rngInt(mu);
    if (this.iterations % this.g == 0)
      this.updateStddev(idv);
    var chromosome = parents.get(select).chromosome;
    var mutant_idv = new Individual(Arrays.stream(chromosome)
            .map(gene -> gene + rngGaussian(stddev_prime[idv]))
            .toArray());
    if (this.getEval(mutant_idv) < this.getEval(parents.get(select)))
      g_s[idv]++;
    return mutant_idv;
  }
}
