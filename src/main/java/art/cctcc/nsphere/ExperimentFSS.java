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

import art.cctcc.nsphere.Parameters.*;
import static art.cctcc.nsphere.Parameters.*;
import java.util.Arrays;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ExperimentFSS extends Experiment {

  public static String TYPE = "fixed-step-size";

  public ExperimentFSS(int n, ESMode mode, int mu, int lambda, double stddev) {

    super(n, mode, mu, lambda, stddev);
  }

  @Override
  public double calcEval(Individual idv) {

    return Arrays.stream(idv.getChromosome()).map(i -> i * i).sum();
  }

  @Override
  public double[] mutation(int i) {

    var chromosome = parents.get(i).getChromosome();

    return Arrays.stream(chromosome)
            .map(gene -> gene + rngGaussian(stddev))
            .toArray();
  }
}
