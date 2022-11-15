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

import art.cctcc.nsphere.enums.RNG;
import static art.cctcc.nsphere.enums.RNG.XOR;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.apache.commons.math3.random.MersenneTwister;
import static art.cctcc.nsphere.Parameters.RandomNumberGenerator;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Tools {

  public static RandomGenerator XOR;
  public static MersenneTwister MT;

  public static void initRandom(long seed, RNG rng) {

    if (rng != null)
      Parameters.RandomNumberGenerator = rng;
    switch (Parameters.RandomNumberGenerator) {


      case Xoshiro256PlusPlus, XOR ->
        XOR = RandomGeneratorFactory.of("Xoshiro256PlusPlus").create(seed);
      case MersenneTwister,MT ->
        MT = new MersenneTwister(seed);
    }
  }

  public static double rngGaussian(double stddev) {

    return switch (RandomNumberGenerator) {
      case Xoshiro256PlusPlus, XOR ->
        XOR.nextGaussian(0, stddev);
      case MersenneTwister,MT ->
        stddev * MT.nextGaussian();
    };
  }

  public static double rngDouble() {

    return switch (RandomNumberGenerator) {
      case Xoshiro256PlusPlus, XOR ->
        XOR.nextDouble();
      case MersenneTwister,MT ->
        MT.nextDouble();
    };
  }

  public static boolean rngBoolean() {

    return switch (RandomNumberGenerator) {
      case Xoshiro256PlusPlus, XOR ->
        XOR.nextBoolean();
      case MersenneTwister,MT ->
        MT.nextBoolean();
    };
  }

  public static int rngInt(int bound) {

    return switch (RandomNumberGenerator) {
      case Xoshiro256PlusPlus, XOR ->
        XOR.nextInt(bound);
      case MersenneTwister,MT ->
        MT.nextInt(bound);
    };
  }

  public static long getEpochMilli() {

    return ZonedDateTime.now().toInstant().toEpochMilli();
  }

  public static String time_elapsed(Instant start) {

    var time_elapsed = Duration.between(start, Instant.now());
    var hours = time_elapsed.toHoursPart();
    return String.format("Time elapsed = %s%02dm %02ds",
            (hours > 0) ? time_elapsed.toHoursPart() + "h " : "",
            time_elapsed.toMinutesPart(), time_elapsed.toSecondsPart());
  }
}
