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

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ExperimentFSS extends NDimSphere {

  public ExperimentFSS(int n, ESMode mode, int mu, int lambda, double stddev) {

    super(n, mode, mu, lambda, stddev);
  }

  @Override
  public String getTitle() {

    return String.format("%s: %s%s, sigma=%.2f",
            super.getTitle(), ESType.FSS.description, getESMode(), sigma);
  }

  @Override
  public Individual mutation(int offspring_index) {

    var select = rngInt(mu);
    var chromosome = parents.get(select).chromosome;
    var mutant = Arrays.stream(chromosome)
            .map(gene -> gene + rngGaussian(sigma))
            .toArray();
    return new Individual(mutant);
  }
}
