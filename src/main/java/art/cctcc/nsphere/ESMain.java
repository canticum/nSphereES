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
import static art.cctcc.nsphere.Parameters.getEpochMilli;
import static art.cctcc.nsphere.Parameters.initRandom;
import static art.cctcc.nsphere.Parameters.rng;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.None;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ESMain {

  public static void main(String[] args) throws IOException {

    var start = Instant.now();

    Properties props = new Properties();
    for (String arg : args) {
      var prop = arg.split("=");
      var value = prop.length == 1 ? "" : prop[1];
      props.setProperty(prop[0], value);
    }
    var n = Integer.parseInt(props.getProperty("n", "10"));
    var seed = Long.parseLong(props.getProperty("seed",
            String.valueOf(getEpochMilli())));
    var mode = ESMode.valueOf(props.getProperty("mode", "Comma"));
//            "Plus"));
    var mu = Integer.parseInt(props.getProperty("mu", "1"));
    var lambda = Integer.parseInt(props.getProperty("lambda", "1"));
    var output_filename = props.getProperty("output",
            String.format("log-n%d-%s_%d", n, mode, seed));

    initRandom(seed, RNG.Xoshiro256PlusPlus);

    var title = n + "-dimensional Sphere Model";
    var info = String.format(
            """
            %s
            Mode=%s, mu=%d, lambda=%d 
            RNG=%s, Seed=%d
            """, title,
            mode, mu, lambda,
            rng, seed);
    System.out.print(info);

    var e1 = new Experiment(10, ESMode.Plus, mu, lambda, 0.01);
    var e2 = new Experiment(10, ESMode.Plus, mu, lambda, 0.1);
    var e3 = new Experiment(10, ESMode.Plus, mu, lambda, 1.0);

    var output1 = e1.run();
    var output2 = e2.run();
    var output3 = e3.run();

    var path = Path.of(System.getProperty("user.dir"), "es_data");
    Files.createDirectories(path);
//    var f_txt = path.resolve(output_filename + ".txt");
//    var f_png = path.resolve(output_filename + ".png");

    var time_elapsed = Duration.between(start, Instant.now());
    var hours = time_elapsed.toHoursPart();
    System.out.printf(
            """
            
            *******************************************
            %sGeneration = %s
            evals size = %s
            Time elapsed = %s%02d m %02d s
            *******************************************
            """, info,
            e1.generation + ", " + e2.generation + ", " + e3.generation,
            e1.evals.size() + ", " + e2.evals.size() + ", " + e3.evals.size(),
            (hours > 0) ? time_elapsed.toHoursPart() + " h " : "",
            time_elapsed.toMinutesPart(), time_elapsed.toSecondsPart());

    Files.write(path.resolve(output_filename + "(dev=0.01).txt"), output1);
    Files.write(path.resolve(output_filename + "(dev=0.1).txt"), output2);
    Files.write(path.resolve(output_filename + "(dev=1.0).txt"), output3);

    Stream.of(e1, e2, e3).forEach(e -> {

      var evalSize = e.evals.size();
      var groups = 100;
      var groupSize = evalSize / groups;

      var chart = new XYChartBuilder()
              .title(String.format("%s (%d%s%d), stddev=%f",
                      title, mu, mode, lambda, e.stddev))
              .xAxisTitle(String.format("1 %% = approx. %d evals", groupSize))
              .yAxisTitle("Avg. Evals")
              .width(1200)
              .height(600)
              .build();

      var xData = IntStream.rangeClosed(1, groups)
              .map(group -> (int) (100.0 * group / groups))
              .boxed()
              .toList();

      var evalAverage = IntStream.range(0, groups)
              .mapToDouble(
                      group -> IntStream.range(group * groupSize, (group + 1) * groupSize)
                              .mapToDouble(i -> e.evals.get(i))
                              .average().getAsDouble())
              .boxed()
              .toList();

      var evalMin = IntStream.range(0, groups)
              .mapToDouble(
                      group -> IntStream.range(group * groupSize, (group + 1) * groupSize)
                              .mapToDouble(i -> e.evals.get(i))
                              .min().getAsDouble())
              .boxed()
              .toList();

      var mNone = new None();
      chart.addSeries("avg_" + e.stddev, xData, evalAverage)
              .setMarker(mNone);
      chart.addSeries("min_" + e.stddev, xData, evalMin)
              .setMarker(mNone);
      var f_png = path.resolve(String.format("%s(dev=%f).png", output_filename, e.stddev));
      System.out.println("Writing plot to " + f_png);
      try {
        Files.write(f_png, BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG));
      } catch (IOException ex) {
        Logger.getLogger(ESMain.class.getName()).log(Level.SEVERE, null, ex);
      }
      if (!GraphicsEnvironment.isHeadless()) {
        new SwingWrapper(chart).setTitle(title).displayChart();
      }
    });

  }
}
