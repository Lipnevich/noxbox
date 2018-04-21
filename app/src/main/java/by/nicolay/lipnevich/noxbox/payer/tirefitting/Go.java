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
package by.nicolay.lipnevich.noxbox.payer.tirefitting;

import android.content.Intent;

import java.util.SortedMap;

import by.nicolay.lipnevich.noxbox.PayerActivity;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.UserType;
import by.nicolay.lipnevich.noxbox.payer.massage.R;
import by.nicolay.lipnevich.noxbox.tools.IntentAndKey;

import static by.nicolay.lipnevich.noxbox.tools.PageCodes.MY_CAR;

public class Go extends PayerActivity {

    @Override
    protected NoxboxType noxboxType() {
        return NoxboxType.tirefitting;
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

    @Override
    protected SortedMap<String, IntentAndKey> getMenu() {
        SortedMap<String, IntentAndKey> map = super.getMenu();
//        Intent intent = new Intent(getApplicationContext(), MyCarPage.class);
//        if(getMyProfile().getCurrentCar() != null) {
//            map.put(getMyProfile().getCurrentCar().toString(), new IntentAndKey().setIntent(intent).setKey(MY_CAR.getCode()));
//        } else {
//            map.put(getString(R.string.my_car), new IntentAndKey().setIntent(intent).setKey(MY_CAR.getCode()));
//        }

//        map.put(getString(R.string.payments), new Intent(getApplicationContext(), MyCarPage.class));
//        map.put(getString(R.string.free_tire_fitting), new Intent(getApplicationContext(), MyCarPage.class));
//        map.put(getString(R.string.help), new Intent(getApplicationContext(), MyCarPage.class));
//        map.put(getString(R.string.settings), new Intent(getApplicationContext(), MyCarPage.class));
        return map;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MY_CAR.getCode()) {
            menu.closeDrawer();
            createMenu();
            menu.openDrawer();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
