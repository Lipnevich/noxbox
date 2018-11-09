const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const noxbox = require('./noxbox-functions');
const wallet = require('./wallet-functions');
const version = 1.0;

exports.welcome = functions.auth.user().onCreate(user => {
    return wallet.create(user).then(noxbox.init);
});

exports.noxboxUpdated = functions.firestore.document('noxboxes/{noxboxId}')
    .onUpdate(async(change, context) => {
      const previousNoxbox = change.before.data();
      const noxbox = change.after.data();
      console.log('Previous Noxbox ' + JSON.stringify(previousNoxbox));
      console.log('Noxbox updated ' + JSON.stringify(noxbox));

      if(!previousNoxbox.timeRequested && noxbox.timeRequested) {
          let pushRequested = {
              data: {
                   type: 'requesting',
                   id: noxbox.id
              },
              topic: noxbox.owner.id
          };
          await admin.messaging().send(pushRequested);
          console.log('push sent' + JSON.stringify(pushRequested));
      }
});

exports.version = functions.https.onRequest((req, res) => {
    res.status(200).send('Version ' + version);
});



