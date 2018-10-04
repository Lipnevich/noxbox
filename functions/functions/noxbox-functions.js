const functions = require('firebase-functions');
const admin = require('firebase-admin');
const BigDecimal = require('big.js');
const db = admin.firestore();
db.settings({timestampsInSnapshots: true});

exports.init = async function (request) {
    let profile = { id : request.uid, travelMode : 'walking' };
    profile.wallet = { address : request.address, balance : '0' };

    if(request.photoUrl) profile.photoUrl = request.photoUrl;
    if(request.displayName) profile.name = request.displayName;

    db.collection('profiles').doc(request.uid).set(profile);
    db.collection('seeds').doc(request.uid).set({seed : request.seed});

    return request;
}


