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
import art.cctcc.nsphere.enums.RNG;
import com.opencsv.CSVReaderHeaderAwareBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
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
  public static RNG Rng = RNG.MT;
  public static int UpperLimit = 10000000;

  public static void initRandom(long seed, RNG rng) {

    if (rng != null)
      Parameters.Rng = rng;
    switch (Parameters.Rng) {

      case Xoshiro256PlusPlus ->
        XOR = RandomGeneratorFactory.of("Xoshiro256PlusPlus").create(seed);
      case MersenneTwister,MT ->
        MT = new MersenneTwister(seed);
    }
  }

  public static double rngGaussian(double stddev) {

    return switch (Rng) {
      case Xoshiro256PlusPlus ->
        XOR.nextGaussian(0, stddev);
      case MersenneTwister,MT ->
        stddev * MT.nextGaussian();
    };
  }

  public static double rngDouble() {

    return switch (Rng) {
      case Xoshiro256PlusPlus ->
        XOR.nextDouble();
      case MersenneTwister,MT ->
        MT.nextDouble();
    };
  }

  public static boolean rngBoolean() {

    return switch (Rng) {
      case Xoshiro256PlusPlus ->
        XOR.nextBoolean();
      case MersenneTwister,MT ->
        MT.nextBoolean();
    };
  }

  public static int rngInt(int bound) {

    return switch (Rng) {
      case Xoshiro256PlusPlus ->
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

  static Data readCSV(Path path, int limit) {

    System.out.println("Reading " + path);
    var xData = new ArrayList<Integer>();
    var yData = new ArrayList<Double>();
    try ( var reader = new FileReader(path.toFile());
             var rha = new CSVReaderHeaderAwareBuilder(reader).build()) {
      Map<String, String> row;
      int iteration;
      while (Objects.nonNull(row = rha.readMap())
              && (iteration = Integer.parseInt(row.get("Iteration"))) <= limit) {
        xData.add(iteration);
        yData.add(Double.valueOf(row.get("Average")));
      }
    } catch (IOException | CsvValidationException ex) {
      Logger.getLogger(Parameters.class.getName()).log(Level.SEVERE, null, ex);
    }
    return new Data(xData, yData);
  }

  record Data(List<Integer> xData, List<Double> yData) {

  }
}
