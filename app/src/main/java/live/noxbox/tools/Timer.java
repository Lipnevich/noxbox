/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package live.noxbox.tools;

import android.os.CountDownTimer;

public abstract class Timer {

    private CountDownTimer timer;

    abstract protected void timeout();

    public void stop() {
        if(timer != null) timer.cancel();
    }

    public Timer start(int seconds) {
        if(timer != null) timer.cancel();

        timer = new CountDownTimer(seconds * 1000, 100) {
            public void onTick(long millisUntilFinished) {}

            public void onFinish() {
                timeout();
            }
        };
        timer.start();

        return this;
    }

}