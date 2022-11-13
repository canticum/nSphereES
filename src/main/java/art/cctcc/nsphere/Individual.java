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

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Individual {

  public final double[] chromosome;
  public final double[] sigmas;
  private double eval = -1;

  public Individual(double[] chromosome, double[] sigmas) {

    this.chromosome = chromosome;
    this.sigmas = sigmas;
  }

  public double getEval() {

    return this.eval;
  }

  public void setEval(double eval) {

    this.eval = eval;
  }

  @Override
  public String toString() {

    return String.format("(X: %s; Sigma: %s)",
            Arrays.stream(chromosome)
                    .mapToObj(v -> String.format("% .3f", v))
                    .collect(Collectors.joining(", ")),
            Arrays.stream(sigmas)
                    .mapToObj(v -> String.format("% .7f", v))
                    .collect(Collectors.joining(", ")));
  }
}
