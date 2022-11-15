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
import static art.cctcc.nsphere.Tools.getEpochMilli;
import art.cctcc.nsphere.enums.ESMode;
import art.cctcc.nsphere.enums.ESType;
import art.cctcc.nsphere.enums.RNG;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Parameters {

  public static long Seed = getEpochMilli();
  public static RNG RandomNumberGenerator = RNG.MT;
  public static int UpperLimit = 10000000;

  public static int n = 10;
  public static int run = 10;

  public static ESType type = ESType.UNSS;
  public static ESMode mode = ESMode.Plus;
  public static int mu = 1;
  public static int lambda = 1;

  //UNSS
  public static double tau = 1e-7 / Math.sqrt(2 * Math.sqrt(n));
  public static double tau_prime = 1 / Math.sqrt(2 * n);
  public static double epsilon0 = 1e-4;

  //OneFive
  public static int g = 100;
  public static double a = 0.817;

  public static List<Double> init_sigmas = new ArrayList<>();

  public Parameters(String... args) {

    for (int i = 0; i < args.length; i++) {
      var arg = args[i].split("=");
      switch (arg[0].toLowerCase()) {
        case "seed" -> Seed = Long.parseLong(arg[1]);
        case "rng" -> RandomNumberGenerator = RNG.valueOf(arg[1]);
        case "limit" -> UpperLimit = Integer.parseInt(arg[1]);

        case "n" -> n = Integer.parseInt(arg[1]);
        case "run" -> run = Integer.parseInt(arg[1]);

        case "type" -> type = ESType.valueOf(arg[1]);
        case "mode" -> mode = ESMode.valueOf(arg[1]);
        case "mu" -> mu = Integer.parseInt(arg[1]);
        case "lambda" -> lambda = Integer.parseInt(arg[1]);
        
        case "tau" -> tau = Double.parseDouble(arg[1]);
        case "taup" -> tau_prime = Double.parseDouble(arg[1]);
        case "ep0" -> epsilon0 = Double.parseDouble(arg[1]);
        
        case "g" -> g = Integer.parseInt(arg[1]);
        case "a" -> a = Double.parseDouble(arg[1]);
        default -> init_sigmas.add(Double.valueOf(arg[0]));
      }
    }
    if (init_sigmas.isEmpty())
      init_sigmas = List.of(0.01, 0.1, 1.0);
  }

  public String toString() {

    return String.format("""
            %d-dimensional Sphere Model: %s, %s
            init sigmas = %s
            RNG=%s, Seed=%d""",
            n, mode.getMode(mu, lambda), type.description,
            init_sigmas,
            RandomNumberGenerator, Seed);
  }
}
