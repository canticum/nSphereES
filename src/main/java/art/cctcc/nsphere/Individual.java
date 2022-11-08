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

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Individual {

  private final double[] chromosome;
  private double eval = -1;

  public Individual(double[] chromosome) {

    this.chromosome = chromosome;
  }

  public double[] getChromosome() {

    return chromosome;
  }

  public double getEval() {

    return this.eval;
  }

  public void setEval(double eval) {

    this.eval = eval;
  }

  public static Individual generate(int n) {

    var chromosome = new double[n];
    Arrays.fill(chromosome, 1);
    return new Individual(chromosome);
  }

  public String render() {

    return Arrays.toString(chromosome);
  }
}
