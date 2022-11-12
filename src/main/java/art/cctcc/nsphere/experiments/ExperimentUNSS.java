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
import art.cctcc.nsphere.enums.ESType;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ExperimentUNSS extends AbsExpNDimSphere {

  private double[][] sigmas;
  private final double tau;
  private final double tauPrime;
  private final double epsilon0;

  public ExperimentUNSS(int n, ESMode mode,
          int mu, int lambda, double sigma,
          double tau, double tauPrime, double epsilon0) {

    super(n, mode, mu, lambda, sigma);
    this.sigmas = new double[mu][n];
    IntStream.range(0, mu)
            .forEach(i -> Arrays.fill(this.sigmas[i], sigma));
    this.tau = tau;
    this.tauPrime = tauPrime;
    this.epsilon0 = epsilon0;
  }

  public ExperimentUNSS(int n, ESMode mode,
          int mu, int lambda, double sigma, double epsilon0) {

    this(n, mode, mu, lambda, sigma,
            1e-7 / Math.sqrt(2 * Math.sqrt(n)),
            1 / Math.sqrt(2 * n),
            epsilon0);
  }

  @Override
  public String getTitle() {
    
    return String.format("%s: %s%s, initial sigma=%.2f",
            super.getTitle(), ESType.UNSS.description, getESMode(), sigma);
  }

  public void updateSigma(int idv) {

    var gaussian_prime = rngGaussian(1);
    IntStream.range(0, n).forEach(i -> {
      var d_prime = this.sigmas[idv][i] * Math.pow(Math.E,
              tauPrime * gaussian_prime + tau * rngGaussian(1));
      if (d_prime < this.epsilon0)
        d_prime = this.epsilon0;
      this.sigmas[idv][i] = d_prime;
    });
  }

  @Override
  public Individual mutation(int offspring_index) {

    var select = rngInt(mu);
    this.updateSigma(select);
    var chromosome = parents.get(select).chromosome;
    var mutant = IntStream.range(0, chromosome.length)
            .mapToDouble(i -> chromosome[i] + this.sigmas[select][i] * rngGaussian(1))
            .toArray();
    return new Individual(mutant);
  }
}