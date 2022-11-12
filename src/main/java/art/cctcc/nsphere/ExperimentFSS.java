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
import static art.cctcc.nsphere.Parameters.*;
import art.cctcc.nsphere.enums.ESType;
import java.util.Arrays;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ExperimentFSS extends Experiment {

  public ExperimentFSS(int n, ESMode mode, int mu, int lambda, double stddev) {

    super(n, mode, mu, lambda, stddev);
  }

  @Override
  public String getTitle() {

    return String.format("%s: %s%s, stddev=%.2f",
            super.getTitle(), ESType.FSS.description, getESMode(), stddev);
  }

  @Override
  public double calcEval(Individual idv) {

    return Arrays.stream(idv.chromosome).map(i -> i * i).sum();
  }

  @Override
  public Individual mutation(int idv) {

    var select = rngInt(mu);
    var chromosome = parents.get(select).chromosome;
    var mutant = Arrays.stream(chromosome)
            .map(gene -> gene + rngGaussian(stddev))
            .toArray();
    return new Individual(mutant);
  }

  @Override
  protected boolean goal() {
    
    return parents.stream()
              .map(Individual::getEval)
              .filter(eval -> eval > -1)
              .anyMatch(eval -> eval <= 0.0005);
  }
}
