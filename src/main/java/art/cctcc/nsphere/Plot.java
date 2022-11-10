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

import art.cctcc.nsphere.Parameters.ESMode;
import java.awt.GraphicsEnvironment;
import java.util.stream.IntStream;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.None;

/**
 * TO-DO
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Plot {

  public static void main(String[] args) {

    var e = new Experiment(10, ESMode.Plus, 1, 1, 0.01);
    var evalSize = e.evals.size();
    var groups = 100;
    var groupSize = evalSize / groups;
    var title = "";
    var chart = new XYChartBuilder()
            .title(String.format("%s%s, stddev=%.2f",
                    title, e.getESMode(), e.stddev))
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
//    var f_png = path.resolve(String.format("%s(dev=%f).png", output_filename, e.stddev));
//    System.out.println("Writing plot to " + f_png);
//    try {
//      Files.write(f_png, BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG));
//    } catch (IOException ex) {
//      Logger.getLogger(ESMain.class.getName()).log(Level.SEVERE, null, ex);
//    }
    if (!GraphicsEnvironment.isHeadless()) {
      new SwingWrapper(chart).setTitle(title).displayChart();
    }
  }
}
