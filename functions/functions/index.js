const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const noxbox = require('./noxbox-functions');
const wallet = require('./wallet-functions');

exports.welcome = functions.auth.user().onCreate((user) => {
    return wallet.create(user).then(
           noxbox.init);
});

