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

exports.balance = requests.child('balance').onCreate(event => {
    return noxbox.getWallet(event.val()).then(
           wallet.getBalance).then(
           noxbox.updateBalance).then(
           noxbox.notifyBalanceUpdated).then(
           noxbox.sendPushNotification, noxbox.logError).then(
           noxbox.releaseRequest);
});

exports.refund = requests.child('refund').onCreate(event => {
    return noxbox.getWallet(event.val()).then(
           wallet.getBalance).then(
           noxbox.isEnoughMoneyForRefund).then(
           wallet.refund).then(
           noxbox.updateTransferWallets).then(
           noxbox.notifyBalanceUpdated).then(
           noxbox.sendPushNotification, noxbox.logError).then(
           noxbox.releaseRequest);
});

exports.request = requests.child('request').onCreate(event => {
    return noxbox.getWallet(event.val()).then(
           wallet.getBalance).then(
           noxbox.getPrice).then(
           noxbox.isEnoughMoneyForRequest).then(
           noxbox.getPayerProfile).then(
           noxbox.getPerformerProfile).then(
           noxbox.createNoxbox).then(
           noxbox.pingPerformer).then(
           noxbox.syncPayer).then(
           noxbox.sendPushNotification).then(
           noxbox.freezeMoney).then(
           noxbox.notifyBalanceUpdated, noxbox.logError).then(
           noxbox.releaseRequest);
});

exports.cancel = requests.child('cancel').onCreate(event => {
    return noxbox.cancelNoxbox(event.val()).then(
           noxbox.notifyCanceled).then(
           noxbox.sendPushNotification).then(
           // TOdO (nli) pay in case more then 3 minutes gone
           noxbox.removeAvailablePerformer).then(
           noxbox.unfreezeMoney).then(
           noxbox.notifyPayerBalanceUpdated, noxbox.logError).then(
           noxbox.releaseRequest);
});

exports.accept = requests.child('accept').onCreate(event => {
    return noxbox.acceptNoxbox(event.val()).then(
           noxbox.generateSecret).then(
           noxbox.getPayerProfile).then(
           noxbox.notifyAccepted).then(
           noxbox.sendPushNotification, noxbox.logError).then(
           noxbox.releaseRequest);
});

exports.complete = requests.child('complete').onCreate(event => {
    return noxbox.completeNoxbox(event.val()).then(
           noxbox.notifyCompleted).then(
           noxbox.getPayerSeed).then(
           noxbox.getPerformerAddress).then(
           wallet.pay).then(
           noxbox.updateTransferWallets).then(
           // TODO (nli) send fee to the noxbox address
           noxbox.storePriceWithoutFeeInNoxbox).then(
           noxbox.notifyPayerBalanceUpdated).then(
           noxbox.sendPushNotification).then(
           noxbox.likePerformer).then(
           noxbox.likePayer, noxbox.logError).then(
           noxbox.releaseRequest);
});

exports.story = requests.child('story/{eventId}').onCreate(event => {
    return noxbox.sendPushNotification(event.val());
});


// tODO (nli) request comment issue
// TODO (nli) resolve issue with dislike, like, moneyBack for performer
// TODO (nli) check voting results, money back in case of (votes > 10) and (moneyBack > 60%)
// TODO (nli) in case of (votes > 24) and (no one side > 60%) dislike

exports.dislike = requests.child('dislike').onCreate(event => {
    return noxbox.dislikeNoxbox(event.val()).then(
           noxbox.blacklist).then(
           noxbox.updateRatings).then(
           noxbox.notifyDisliked, noxbox.logError).then(
           noxbox.releaseRequest);
});

exports.info = functions.https.onRequest((request, response) => {
    return noxbox.info({}).then(function(result){
            response.write(JSON.stringify(result));
            response.end();
    });
});

exports.availables = (req, res) => {

  admin.database().ref('geo').once('value').then(function(allServices) {
    	var json = [];

    	allServices.forEach(function(service) {
          	json.push({ key : service.key, latitude : service.val().l[0], longitude : service.val().l[1] });
  		});

    	res.status(200).send('availables_callback(' + JSON.stringify(json) + ')');
  	});

}

exports.about = functions.https.onRequest((request, response) => { return response.send("NoxBox. Dedicated to my dear wife Mara"); });

exports.showFunctionIp = functions.https.onRequest((request, response) => {
    return http("http://www.showmyip.gr/", { 'json': true }, (err, res, body) => { return response.send(body); });
});


