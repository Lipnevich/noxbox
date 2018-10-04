const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const noxbox = require('./noxbox-functions');
const wallet = require('./wallet-functions');
const version = 0.8;

exports.welcome = functions.auth.user().onCreate(user => {
    return wallet.create(user).then(noxbox.init);
});

exports.version = functions.https.onRequest((req, res) => {
    res.status(200).send('Version ' + version);
});


exports.push = functions.https.onRequest((req, res) => {
    var registrationToken = 'dLHjhs_hdbs:APA91bEvOZ-3tYFxL4Jzu4G3MkNAQQ67exB6AEuNdeCINWDSFtG95qbKNH_iGFPrDCYnSvmvtaqVDKjOtlt1hdmvBvLbKqJTmx2IhdO8Ez_flRv4XQUweLZEjlrWLad-y1cs06UNTW5A';

    var message = {
      data: {
        score: '850',
        time: '2:45'
      },
      token: registrationToken
    };

    admin.messaging().send(message)
      .then(response => {
        console.log('Successfully sent message:', response);
      })
      .catch(error => {
        console.log('Error sending message:', error);
      });

    res.status(200).send('Version ' + version);
});