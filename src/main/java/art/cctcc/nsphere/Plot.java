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

import com.opencsv.CSVReaderHeaderAwareBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.awt.GraphicsEnvironment;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.None;

/**
 * TO-DO
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Plot {

  private final String title;
  private final XYChart chart;

  public Plot(String title) {

    this.title = title;
    this.chart = new XYChartBuilder()
            .title(title)
            .xAxisTitle(String.format("Iterations"))
            .yAxisTitle("Evals")
            .width(1200)
            .height(800)
            .build();
  }

  public void add(String series, List<Integer> xData, List<Double> yData) {

    chart.addSeries(series, xData, yData).setMarker(new None());
  }

  public void show(Path path) {

    System.out.println("Writing plot to " + path);
    try {
      Files.write(path, BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG));
    } catch (IOException ex) {
      Logger.getLogger(Plot.class.getName()).log(Level.SEVERE, null, ex);
    }
    if (!GraphicsEnvironment.isHeadless()) {
      new SwingWrapper(chart).setTitle(title).displayChart();
    }
  }

  public static PlotData readCSV(Path path, int limit) {

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
    return new PlotData(xData, yData);
  }

  record PlotData(List<Integer> xData, List<Double> yData) {

  }
}
