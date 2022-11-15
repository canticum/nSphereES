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
import static art.cctcc.nsphere.RandomNumberGenerator.*;
import art.cctcc.nsphere.enums.ESMode;
import art.cctcc.nsphere.enums.ESType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ExperimentUNSS extends NDimSphere {

  private final double tau;
  private final double tauPrime;
  private final double epsilon0;

  public ExperimentUNSS(int n, ESMode mode, int mu, int lambda, double init_sigma,
          double tau, double tauPrime, double epsilon0, int upper_limit) {

    super(n, mode, mu, lambda, init_sigma, upper_limit);
    this.tau = tau;
    this.tauPrime = tauPrime;
    this.epsilon0 = epsilon0;
  }

  public ExperimentUNSS(int n, ESMode mode,
          int mu, int lambda, double sigma, double epsilon0, int upper_limit) {

    this(n, mode, mu, lambda, sigma,
            1e-7 / Math.sqrt(2 * Math.sqrt(n)),
            1 / Math.sqrt(2 * n),
            epsilon0, upper_limit);
  }

  @Override
  public String getTitle() {

    return String.format("%s: %s, %s, initial sigma=%.2f",
            super.getTitle(), getESMode(), ESType.UNSS.description, init_sigma);
  }

  @Override
  public Individual mutation(int offspring_index) {

    var parent = parents.get(rngInt(mu));
    var gaussian_prime = rngGaussian(1);

    var chromosome = new double[n];
    var sigmas = new double[n];

    Arrays.setAll(sigmas, i
            -> Math.max(parent.sigmas[i] * Math.pow(Math.E,
                    tauPrime * gaussian_prime + tau * rngGaussian(1)),
                    epsilon0));
    Arrays.setAll(chromosome, i
            -> parent.chromosome[i] + sigmas[i] * rngGaussian(1));
    return new Individual(chromosome, sigmas);
  }

  @Override
  protected List<Individual> generate() {

    return Stream.generate(() -> {
      var chromosome = new double[n];
      Arrays.fill(chromosome, 1.0);
      var sigmas = new double[n];
      Arrays.fill(sigmas, init_sigma);
      return new Individual(chromosome, sigmas);
    })
            .limit(mu).toList();
  }
}
