const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const noxbox = require('./noxbox-functions');
const wallet = require('./wallet-functions');
const http = require('request');
const requests = functions.database.ref('/requests/{userID}');

exports.welcome = functions.auth.user().onCreate((user) => {
    return wallet.create(user).then(
           noxbox.init).then(
           noxbox.getRewardWallet).then(
           wallet.tryToReward).then(
           noxbox.updateTransferWallets).then(
           noxbox.createRating).then(
           noxbox.notifyBalanceUpdated);
});

