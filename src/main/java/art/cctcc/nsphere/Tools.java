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

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Tools {

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
