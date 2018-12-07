const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const noxbox = require('./noxbox-functions');
const wallet = require('./wallet-functions');
const version = 101;

exports.welcome = functions.auth.user().onCreate(user => {
    return wallet.create(user).then(noxbox.init);
});

exports.noxboxUpdated = functions.firestore.document('noxboxes/{noxboxId}').onUpdate(async(change, context) => {
    const previousNoxbox = change.before.data();
    const noxbox = change.after.data();
    console.log('Version code ' + JSON.stringify(version));
    console.log('Previous Noxbox ' + JSON.stringify(previousNoxbox));
    console.log('Noxbox updated ' + JSON.stringify(noxbox));


    if(!previousNoxbox.timeRequested && noxbox.timeRequested) {
        let push = {
            data: {
                type: 'accepting',
                time: '' + noxbox.timeRequested,
                id: noxbox.id
            },
            topic: noxbox.owner.id
        };
        await admin.messaging().send(push);
        console.log('push sent' + JSON.stringify(push));
    } else if(!previousNoxbox.timeAccepted && noxbox.timeAccepted) {
        let push = {
            data: {
                type: 'moving',
                id: noxbox.id
            },
            topic: noxbox.id
        };

        await admin.messaging().send(push);
        console.log('push sent' + JSON.stringify(push));
    } else  if(noxbox.ownerMessages && (!previousNoxbox.ownerMessages || Object.keys(previousNoxbox.ownerMessages).length < Object.keys(noxbox.ownerMessages).length)) {
        let push = {
            data: {
                 type: 'message',
                 noxboxType: noxbox.type,
                 name: noxbox.role === 'demand' ? noxbox.owner.name : '',
                 message: latestMessage(noxbox.ownerMessages).message,
                 id: noxbox.id
            },
            topic: noxbox.party.id
        };
        await admin.messaging().send(push);
        console.log('push sent' + JSON.stringify(push));
    } else if(noxbox.partyMessages && (!previousNoxbox.partyMessages || Object.keys(previousNoxbox.partyMessages).length < Object.keys(noxbox.partyMessages).length)){
        let push = {
            data: {
                 type: 'message',
                 noxboxType: noxbox.type,
                 name: noxbox.role === 'supply' ? noxbox.party.name : '',
                 message: latestMessage(noxbox.partyMessages).message,
                 id: noxbox.id
            },
            topic: noxbox.owner.id
        };
        await admin.messaging().send(push);
        console.log('push sent' + JSON.stringify(push));
    }else if((!previousNoxbox.timeOwnerVerified || !previousNoxbox.timePartyVerified) && noxbox.timeOwnerVerified && noxbox.timePartyVerified){
        let push = {
                  data: {
                      type: 'performing',
                      id: noxbox.id
                  },
                  topic: noxbox.owner.id
              };
            await admin.messaging().send(push);
            console.log('push sent' + JSON.stringify(push));
    }else if(!previousNoxbox.timeCompleted && noxbox.timeCompleted){
        let push = {
                  data: {
                      type: 'completed',
                      time: '' + noxbox.timeCompleted,
                      id: noxbox.id
                  },
                  topic: noxbox.id
              };
            await admin.messaging().send(push);
            console.log('push sent' + JSON.stringify(push));

    }else if(!previousNoxbox.timeCanceledByOwner && !previousNoxbox.timeCanceledByParty && noxbox.timeCanceledByOwner){
        let push = {
                          data: {
                              type: 'canceled',
                              id: noxbox.id
                          },
                          topic: noxbox.party.id
                      };
                    await admin.messaging().send(push);
                    console.log('push sent' + JSON.stringify(push));

    }else if(!previousNoxbox.timeCanceledByOwner && !previousNoxbox.timeCanceledByParty && noxbox.timeCanceledByParty){
             let push = {
                               data: {
                                   type: 'canceled',
                                   id: noxbox.id
                               },
                               topic: noxbox.owner.id
                           };
                         await admin.messaging().send(push);
                         console.log('push sent' + JSON.stringify(push));

         }
});

function latestMessage(messages) {
    let message;
    for(time in messages) {
        if(!message || messages[time].time > message.time) {
            message = messages[time];
        }
    }
    return message;
}

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



