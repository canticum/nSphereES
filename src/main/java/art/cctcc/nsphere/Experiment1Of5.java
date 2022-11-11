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
import java.util.Arrays;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Experiment1Of5 extends ExperimentFSS {

  private double stddev_prime;
  private int g;
  private double a;
  private int g_s;

  public Experiment1Of5(int n, ESMode mode,
          int mu, int lambda, double stddev, int g, double a) {

    super(n, mode, mu, lambda, stddev);
    this.stddev_prime = stddev;
    this.g = g;
    this.a = a;
  }

  public Experiment1Of5(int n, ESMode mode,
          int mu, int lambda, double stddev, int g) {

    this(n, mode, mu, lambda, stddev, g, 0.817);
  }

  public void updateStddev() {
    var p_s = 1.0 * g_s / g;
    if (p_s > 0.2)
      this.stddev_prime /= a;
    else if (p_s < 0.2)
      this.stddev_prime *= a;
    this.g_s = 0;
  }

  @Override
  public Individual mutation(int idv) {

    if (this.iterations % this.g == 0)
      this.updateStddev();
    var chromosome = parents.get(idv).chromosome;
    var mutant_idv = new Individual(Arrays.stream(chromosome)
            .map(gene -> gene + rngGaussian(stddev_prime))
            .toArray());
    if (this.getEval(mutant_idv) < this.getEval(parents.get(idv)))
      g_s++;
    return mutant_idv;
  }
}
