const functions = require('firebase-functions');
const admin = require('firebase-admin');
const BigDecimal = require('big.js');
admin.initializeApp(functions.config().firebase);

const storage = require('./noxbox-functions');
const wallet = require('./wallet-functions');
const version = 1;

exports.welcome = functions.auth.user().onCreate(async user => {
    return wallet.create(user).then(storage.init);
});

exports.noxboxUpdated = functions.firestore.document('noxboxes/{noxboxId}').onUpdate(async(change, context) => {
    const previousNoxbox = change.before.data();
    const noxbox = change.after.data();
    console.log('Version code ' + JSON.stringify(version));
    console.log('Previous Noxbox ' + JSON.stringify(previousNoxbox));
    console.log('Noxbox updated ' + JSON.stringify(noxbox));

    // TODO (nli) order completed, canceled, any stopped
    if(!previousNoxbox.timeRequested && noxbox.timeRequested) {
        let push = {
            data: {
                type: 'accepting',
                id: noxbox.id
            },
            topic: noxbox.owner.id
        };
        await admin.messaging().send(push);
        console.log('push sent' + JSON.stringify(push));
    } else if(!previousNoxbox.timeAccepted && noxbox.timeAccepted) {
        let payer = noxbox.role == 'demand' ? noxbox.owner : noxbox.party;
        let balance = await wallet.balance(payer.wallet.address);

        let push;
        if(balance.lt(new BigDecimal(noxbox.price).div(4))) {
            console.log('Not enough money for quoter hour', balance);
            let push = {
                data: {
                   //type: 'shouldWeBanHackers',
                   //type: 'bigBrotherIsWatchingYou',
                   type: 'canceled',
                   id: noxbox.id
                },
                topic: noxbox.id
            };
        } else {
            console.log('Enough money for quoter hour', balance);
            push = {
                data: {
                    type: 'moving',
                    id: noxbox.id
                },
                topic: noxbox.id
            };
        }
        await admin.messaging().send(push);
        console.log('push sent' + JSON.stringify(push));
    } else  if(noxbox.chat && noxbox.chat.ownerMessages && (!previousNoxbox.chat.ownerMessages || Object.keys(previousNoxbox.chat.ownerMessages).length < Object.keys(noxbox.chat.ownerMessages).length)) {
        let push = {
            data: {
                 type: 'message',
                 noxboxType: noxbox.type,
                 name: noxbox.role === 'demand' ? noxbox.owner.name : '',
                 message: latestMessage(noxbox.chat.ownerMessages).message,
                 id: noxbox.id
            },
            topic: noxbox.party.id
        };
        await admin.messaging().send(push);
        console.log('push sent' + JSON.stringify(push));
    } else if(noxbox.chat && noxbox.chat.partyMessages && (!previousNoxbox.chat.partyMessages || Object.keys(previousNoxbox.chat.partyMessages).length < Object.keys(noxbox.chat.partyMessages).length)){
        let push = {
            data: {
                 type: 'message',
                 noxboxType: noxbox.type,
                 name: noxbox.role === 'supply' ? noxbox.party.name : '',
                 message: latestMessage(noxbox.chat.partyMessages).message,
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
        let timeStart = noxbox.timeOwnerVerified > noxbox.timePartyVerified ? noxbox.timeOwnerVerified : noxbox.timePartyVerified;
        let timeSpent = Date.now() - timeStart;
        let payer = noxbox.role == 'demand' ? noxbox.owner : noxbox.party;
        let performer = noxbox.role == 'demand' ? noxbox.party : noxbox.owner;
        let moneyToPay = new BigDecimal('' + timeSpent)
            .div(60 * 60 * 1000)
            .mul(new BigDecimal(noxbox.price));

        let request = { };
        request.addressToTransfer = performer.wallet.address;
        request.encrypted = await storage.seed(payer.id);
        request.minPayment = new BigDecimal(noxbox.price).div(4);
        request.transferable = moneyToPay;

        await wallet.send(request);

        noxbox.total = request.transferable;

        let push = {
                  data: {
                      type: 'completed',
                      time: '' + noxbox.timeCompleted,
                      total: '' + request.transferable,
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

exports.transfer = functions.https.onCall(async (data, context) => {
    console.log('id', context.auth.uid);
    console.log('addressToTransfer', data.addressToTransfer);
    // TODO (nli) firestore noxboxes query if current noxbox present
    let request = { addressToTransfer : data.addressToTransfer};
    request.encrypted = await storage.seed(context.auth.uid);
    return wallet.send(request);
});


exports.version = functions.https.onRequest((req, res) => {
    res.status(200).send('Version ' + version);
});



