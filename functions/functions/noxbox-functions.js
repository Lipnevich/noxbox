const functions = require('firebase-functions');
const admin = require('firebase-admin');
const BigDecimal = require('big.js');
const Q = require('q');

const rewardAccount = functions.config().keys.rewardaccount;
const db = admin.firestore();

exports.init = function (request) {
    var deferred = Q.defer();

    var profile = { id : request.uid,
                    travelMode : 'walking' };
    if(request.photoUrl) profile.photoUrl = request.photoUrl;
    if(request.displayName) profile.name = request.displayName;
    profile.wallet = { address : request.wallet.address,
                       balance : '0',
                       frozen : '0' };

    db.collection('profiles').doc(request.uid).set({
        profile : profile,
        seed : request.wallet.seed
    });
    deferred.resolve(request);

    return deferred.promise;
}


