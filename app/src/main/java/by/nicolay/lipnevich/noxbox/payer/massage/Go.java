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
package by.nicolay.lipnevich.noxbox.payer.massage;

import by.nicolay.lipnevich.noxbox.PayerActivity;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.UserType;
import by.nicolay.lipnevich.noxbox.performer.massage.R;

public class Go extends PayerActivity {

    @Override
    protected NoxboxType noxboxType() {
        return NoxboxType.massage;
    }

    @Override
    protected UserType userType() {
        return UserType.payer;
    }

    protected int getPerformerDrawable() {
        return R.drawable.masseur;
    }

    @Override
    protected int getPayerDrawable() {
        return R.drawable.pointer;
    }


}
