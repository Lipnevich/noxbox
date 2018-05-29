# Noxbox <img src="https://noxbox.io/img/logo.png" align="right" width="100">

Mobile applications for [Payer][4] and [Performer][5], site and crypto payments of Noxbox platform <br/>
Site [noxbox.io][2] <br/>
Promo [video][3]

## On board
Google Cloud Firebase backend <br/>
Waves blockchain as payment system <br/>
Google Maps and Google Directions for coordinations between performer and customer <br/>
Firebase Functions for secure payments <br/>

## How to run

Before you start using Google Plus login
1. create a Firebase project in the Firebase console
2. add Android application with your package name from manifest
2. set SHA1 fingerprint for debug and release keystore<br/>
   keytool -exportcert -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore
3. download a google-services.json file and copy it into app/ folder

Before you start processing crypto transactions
1. choose payed plan for Firebase for external api connection
2. Upload google functions with commands: <br/>
   npm install -g firebase-tools <br/>
   firebase login <br/>
   npm install waves-api <br/>
   npm install q <br/>
   npm install request <br/>
   npm install big.js <br/>
   cd /functions/functions <br/>
   firebase deploy --only functions <br/>
3. (optional) for improving data protection set your secret word<br/>
   firebase functions:config:set keys.seedpass="Your Secret Word For user's seed encryption"
4. (optional) account with wallet for rewards
   firebase functions:config:set keys.rewardaccout="NoxBox Admin Account"

   const password = functions.config().keys.seedpass ? functions.config().keys.seedpass : 'Salt';

Before you start use Google Maps
1. activate Google Maps Android API and Google Maps Directions API
2. create google_maps_key and google_maps_server_key and copy them into /config.xml

## License

Licensed under the [Apache License 2.0][1]

	Copyright (C) 2018 Nicolay Lipnevich

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	    http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

## Donate Noxbox
BTC : 36Gk11Sd7FwCCWbRMA2n8SvzYHrPAfXm7v <br/>
ETH : 0x3e14acf74b9492e5abf02691754fda64d6283b8d <br/>
Waves : 3PHEArHiPsE8Yq32UagdewzrupY6Ycw8M73 <br/>


[1]: http://www.apache.org/licenses/LICENSE-2.0
[2]: https://noxbox.io/
[3]: https://youtu.be/E_Q1wTN27jk
[4]: https://play.google.com/store/apps/details?id=by.nicolay.lipnevich.noxbox.payer.massage
[5]: https://play.google.com/store/apps/details?id=by.nicolay.lipnevich.noxbox.performer.massage
