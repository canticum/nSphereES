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

import static art.cctcc.nsphere.Parameters.rngGaussian;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ExperimentUNSS extends ExperimentFSS {

  public static String TYPE = "uncorrelated n-step-size";

  private double[][] stddevs;
  private final double tau;
  private final double tauPrime;
  private final double epsilon0;

  public ExperimentUNSS(int n, Parameters.ESMode mode,
          int mu, int lambda, double stddev,
          double tau, double tauPrime, double epsilon0) {

    super(n, mode, mu, lambda, stddev);
    this.stddevs = new double[mu][n];
    IntStream.range(0, mu)
            .forEach(i -> Arrays.fill(this.stddevs[i], stddev));
    this.tau = tau;
    this.tauPrime = tauPrime;
    this.epsilon0 = epsilon0;
  }

  public ExperimentUNSS(int n, Parameters.ESMode mode,
          int mu, int lambda, double stddev, double epsilon0) {

    this(n, mode, mu, lambda, stddev,
            stddev * 0.0000001 / Math.sqrt(2 * Math.sqrt(n)),
            1 / Math.sqrt(2 * n),
            epsilon0);
  }

  /**
   *
   * @param i individual index
   * @return
   */
  public void updateStddev(int i) {

    var gaussian_prime = rngGaussian(1);
    IntStream.range(0, n).forEach(j -> {
      var d_prime = this.stddevs[i][j] * Math.pow(Math.E,
              tauPrime * gaussian_prime + tau * rngGaussian(1));
      if (d_prime < this.epsilon0)
        d_prime = this.epsilon0;
      this.stddevs[i][j] = d_prime;
    });
  }

  @Override
  public double[] mutation(int i) {

    this.updateStddev(i);
    var chromosome = parents.get(i).getChromosome();
    return IntStream.range(0, chromosome.length)
            .mapToDouble(j -> chromosome[j] + this.stddevs[i][j] * rngGaussian(1))
            .toArray();
  }
}
