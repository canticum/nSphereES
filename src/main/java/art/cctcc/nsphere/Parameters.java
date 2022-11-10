package art.cctcc.nsphere;

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
import java.time.ZonedDateTime;
import java.util.random.RandomGeneratorFactory;
import java.util.random.RandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Parameters {

  public static RandomGenerator XOR;
  public static MersenneTwister MT;
  public static final double P_mutation = 0.25;
  public static final double P_recombinant = 0.75;
  public static RNG rng = RNG.MT;

  public static enum ESMode {

    Plus('+'), Comma(',');

    public char symbol;

    private ESMode(char symbol) {

      this.symbol = symbol;
    }

    public String getMode(int mu, int lambda) {

      return String.format("(%d%s%d)", mu, this.symbol, lambda);
    }
  }

  public static enum RNG {

    Xoshiro256PlusPlus, MersenneTwister, MT
  }

  public static void initRandom(long seed, RNG rng) {

    if (rng != null)
      Parameters.rng = rng;
    switch (Parameters.rng) {
      case Xoshiro256PlusPlus ->
        XOR = RandomGeneratorFactory.of("Xoshiro256PlusPlus").create(seed);
      case MersenneTwister,MT ->
        MT = new MersenneTwister(seed);
    }
  }

  public static double rngGaussian(double stddev) {

    return switch (rng) {
      case Xoshiro256PlusPlus ->
        XOR.nextGaussian(0, stddev);
      case MersenneTwister,MT ->
        stddev * MT.nextGaussian();
    };
  }

  public static double rngDouble() {

    return switch (rng) {
      case Xoshiro256PlusPlus ->
        XOR.nextDouble();
      case MersenneTwister,MT ->
        MT.nextDouble();
    };
  }

  public static boolean rngBoolean() {

    return switch (rng) {
      case Xoshiro256PlusPlus ->
        XOR.nextBoolean();
      case MersenneTwister,MT ->
        MT.nextBoolean();
    };
  }

  public static int rngInt(int bound) {

    return switch (rng) {
      case Xoshiro256PlusPlus ->
        XOR.nextInt(bound);
      case MersenneTwister,MT ->
        MT.nextInt(bound);
    };
  }

  public static long getEpochMilli() {

    return ZonedDateTime.now().toInstant().toEpochMilli();
  }
}
