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

exports.push = functions.https.onRequest((request, response) => {
    let message = {
      data: {
        type: 'message',
        id: 'somelongidstringthatwasgeneratedbyfirestore',

      },
      topic: 'hqeaykYp2Cfd6Ys9v01kRwzid9j1'
    };
    admin.messaging().send(message)
      .then(o => {
        return response.send("Success version 4");
      })
      .catch(e => {
        return response.send("Error version 4");
      });
});

