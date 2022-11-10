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
import static art.cctcc.nsphere.Parameters.rngInt;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ExperimentUncorrelatedNStepSize extends Experiment {

  private double[][] stddevs;
  private final double tau;
  private final double tauPrime;
  private final double epsilon0;

  public ExperimentUncorrelatedNStepSize(int n, Parameters.ESMode mode,
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

  public ExperimentUncorrelatedNStepSize(int n, Parameters.ESMode mode,
          int mu, int lambda, double stddev, double epsilon0) {

    this(n, mode, mu, lambda, stddev,
            1.0 / Math.sqrt(2 * Math.sqrt(n)),
            1.0 / Math.sqrt(2 * n),
            epsilon0);
  }

  public double[] updateStddev(int i) {

    var gaussian_prime = rngGaussian(1);
    var gaussians = DoubleStream.generate(() -> rngGaussian(1))
            .limit(n)
            .toArray();
    for (int j = 0; j < n; j++) {
      var d_prime = this.stddevs[i][j] * Math.pow(Math.E,
              tauPrime * gaussian_prime + tau * gaussians[j]);
//      if (d_prime < this.epsilon0)
//        d_prime = this.epsilon0;
      this.stddevs[i][j] = d_prime;
    }
    return gaussians;
  }

  @Override
  public double[] mutation(int i) {

    var chromosome = parents.get(i).getChromosome();

    var gaussians = this.updateStddev(i);

    return IntStream.range(0, chromosome.length)
//            .peek(j -> System.out.printf("%.3f, %.3f\n", this.stddevs[i][j], gaussians[j]))
            .mapToDouble(j -> chromosome[j] += this.stddevs[i][j] * gaussians[j])
            .toArray();
  }

  @Override
  public String getESMode() {

    return super.getESMode() + "(uncorrelated n step-sizes)";
  }
}
