const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const noxbox = require('./noxbox-functions');
const wallet = require('./wallet-functions');
const version = 1.1;

exports.welcome = functions.auth.user().onCreate(user => {
    return wallet.create(user).then(noxbox.init);
});

exports.noxboxUpdated = functions.firestore.document('noxboxes/{noxboxId}').onUpdate(async(change, context) => {
    const previousNoxbox = change.before.data();
    const noxbox = change.after.data();
    console.log('Previous Noxbox ' + JSON.stringify(previousNoxbox));
    console.log('Noxbox updated ' + JSON.stringify(noxbox));
    if(!previousNoxbox.timeRequested && noxbox.timeRequested) {
        let pushAccepted = {
            data: {
                type: 'accepting',
                time: '' + noxbox.timeRequested,
                id: noxbox.id
            },
            topic: noxbox.owner.id
        };
        await admin.messaging().send(pushAccepted);
        console.log('push sent' + JSON.stringify(pushAccepted));
    } else if(!previousNoxbox.timeAccepted && noxbox.timeAccepted) {
        let pushMoving = {
            data: {
                type: 'moving',
                id: noxbox.id
            },
            topic: noxbox.party.id
        };

        await admin.messaging().send(pushMoving);
        console.log('push sent' + JSON.stringify(pushMoving));
    } else if(noxbox.ownerMessages && (!previousNoxbox.ownerMessages || previousNoxbox.ownerMessages.length < noxbox.ownerMessages.length)) {
        let ownerMessage = noxbox.ownerMessages[noxbox.ownerMessages.length - 1];
        let pushMessage = {
            data: {
                 type: 'message',
                 time: ownerMessage.time,
                 message: ownerMessage.message,
                 name: noxbox.owner.name,
                 id: noxbox.id
            },
            topic: noxbox.party.id
        };
        await admin.messaging().send(pushMessage);
        console.log('push sent' + JSON.stringify(pushMessage));
    } else if(noxbox.partyMessages && (!previousNoxbox.partyMessages || previousNoxbox.partyMessages.length < noxbox.partyMessages.length)){
        let partyMessage = noxbox.partyMessages[noxbox.partyMessages.length - 1];
        let pushMessage = {
            data: {
                 type: 'message',
                 time: partyMessage.time,
                 message: partyMessage.message,
                 name: noxbox.party.name,
                 id: noxbox.id
            },
            topic: noxbox.owner.id
        };
        await admin.messaging().send(pushMessage);
        console.log('push sent' + JSON.stringify(pushMessage));
    }
});

exports.map = functions.https.onRequest((req, res) => {
admin.database().ref('geo').once('value').then(allServices => {
    let json = [];
    allServices.forEach(service => {
        json.push({ key : service.key,
                    latitude : service.val().l[0],
                    longitude : service.val().l[1]
    })});

    res.status(200).send('map_callback(' + JSON.stringify(json) + ')');
});
});

exports.version = functions.https.onRequest((req, res) => {
res.status(200).send('Version ' + version);
});



