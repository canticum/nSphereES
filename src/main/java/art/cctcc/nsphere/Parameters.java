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
import static art.cctcc.nsphere.RandomNumberGenerator.initRandom;
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

  public long seed = getEpochMilli();
  public RNG rng = RNG.Xoshiro256PlusPlus;
  public int upper_limit = 10000000;

  public int n = 10;
  public int run = 10;

  public ESType type = ESType.UNSS;
  public ESMode mode = ESMode.Plus;
  public int mu = 1;
  public int lambda = 1;

  //UNSS
  public double tau = 1e-7 / Math.sqrt(2 * Math.sqrt(n));
  public double tau_prime = 1 / Math.sqrt(2 * n);
  public double epsilon0 = 1e-4;

  //OneFive
  public int g = 100;
  public double a = 0.817;

  public List<Double> init_sigmas = new ArrayList<>();

  public Parameters(String... args) {

    for (int i = 0; i < args.length; i++) {
      var arg = args[i].split("=");
      switch (arg[0].toLowerCase()) {
        case "seed" -> seed = Long.parseLong(arg[1]);
        case "rng" -> rng = RNG.valueOf(arg[1]);
        case "limit" -> upper_limit = Integer.parseInt(arg[1]);

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

    initRandom(seed, rng);

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
            rng, seed);
  }
}
