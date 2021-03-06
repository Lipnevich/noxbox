const functions = require('firebase-functions');
const admin = require('firebase-admin');
const BigDecimal = require('big.js');
admin.initializeApp(functions.config().firebase);

const wallet = require('./wallet-functions');
const version = 8;

const db = admin.firestore();
db.settings({timestampsInSnapshots: true});

exports.welcome = functions.auth.user().onCreate(async user => {
    await wallet.create(user);

    let profile = { id : user.uid, travelMode : 'walking', noxboxId : '' };
    profile.wallet = { address : user.address, balance : '0' };

    if(user.photoUrl) profile.photoUrl = user.photoUrl;
    if(user.displayName) profile.name = user.displayName;

    db.collection('profiles').doc(user.uid).set(profile);
    db.collection('seeds').doc(user.uid).set({seed : user.seed});

    return user;
});

exports.noxboxUpdated = functions.firestore.document('noxboxes/{noxboxId}').onUpdate(async(change, context) => {
    const previousNoxbox = change.before.data();
    const noxbox = change.after.data();
    console.log('Previous Noxbox ' + JSON.stringify(previousNoxbox));
    console.log('Noxbox updated ' + JSON.stringify(noxbox));
    let operationName = 'unknown';

    await updateRating(previousNoxbox, noxbox);

    if(previousNoxbox.finished) {
        return;
    }

    if(!previousNoxbox.timeRequested && noxbox.timeRequested) {
        operationName = 'Requested';
        let push = {
            data: {
                type: 'accepting',
                id: noxbox.id
            },
            topic: noxbox.owner.id
        };
        // TODO check ratings for console else if both users and cancel
        await admin.messaging().send(push);
        console.log('push was sent' + JSON.stringify(push));
    } else if(!previousNoxbox.timeAccepted && noxbox.timeAccepted) {
        operationName = 'Accepted';
        let payer = noxbox.role === 'demand' ? noxbox.owner : noxbox.party;
        let balance = await wallet.balance(payer.wallet.address);

        let push;
        if(balance.lt(new BigDecimal('' + noxbox.price).div(4))) {
            console.error('Not enough money ', balance);
            // TODO (nli) set notEnoughMoneyTime and finished=true
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
            console.log('Enough money ', balance);
            push = {
                data: {
                    type: 'moving',
                    id: noxbox.id
                },
                topic: noxbox.id
            };
        }
        await admin.messaging().send(push);
        console.log('push was sent' + JSON.stringify(push));
    } else  if(noxbox.chat && noxbox.chat.ownerMessages && (!previousNoxbox.chat.ownerMessages || Object.keys(previousNoxbox.chat.ownerMessages).length < Object.keys(noxbox.chat.ownerMessages).length)) {
        operationName = noxbox.role === 'demand' ? 'Payer say Performer' : 'Performer say Payer';
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
        console.log('push was sent' + JSON.stringify(push));
    } else if(noxbox.chat && noxbox.chat.partyMessages && (!previousNoxbox.chat.partyMessages || Object.keys(previousNoxbox.chat.partyMessages).length < Object.keys(noxbox.chat.partyMessages).length)){
        operationName = noxbox.role === 'supply' ? 'Payer say Performer' : 'Performer say Payer';
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
        operationName = 'Verified';
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
        operationName = 'Completed';
        let payer = noxbox.role === 'demand' ? noxbox.owner : noxbox.party;
        console.log('payer', payer);
        let performer = noxbox.role === 'demand' ? noxbox.party : noxbox.owner;
        console.log('performer', performer);
        let moneyToPay = new BigDecimal(noxbox.price);

        let request = { };
        request.addressToTransfer = performer.wallet.address;
        request.encrypted = (await db.collection('seeds').doc(noxbox.payerId).get()).data().seed;
        request.attachment = 'Payment for ' + noxbox.type + '. Processed with love by NoxBox';
        request.transferable = moneyToPay;
        request.type = noxbox.type;
        if(performer.referral && performer.referral !== payer.id) {
            let referralProfile = await db.collection('profiles').doc(performer.referral).get();
            if(referralProfile.exists && referralProfile.data().wallet && referralProfile.data().wallet.address) {
                request.referral = referralProfile.data().wallet.address;
            }
        }else if(performer.referral && performer.referral === payer.id){
             request.discountFee = true;
        }

        await wallet.send(request);

        let push = {
              data: {
                  type: 'completed',
                  time: '' + noxbox.timeCompleted,
                  total: '' + request.transferred,
                  id: noxbox.id
              },
              topic: noxbox.id
        };
        await admin.messaging().send(push);
        console.log('push was sent' + JSON.stringify(push));

    }else if(!previousNoxbox.timeCanceledByOwner && !previousNoxbox.timeCanceledByParty && noxbox.timeCanceledByOwner){
        operationName = noxbox.role === 'demand' ? 'Canceled by Payer' : 'Canceled by Performer';
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
         operationName = noxbox.role === 'supply' ? 'Canceled by Payer' : 'Canceled by Performer';
         let push = {
               data: {
                   type: 'canceled',
                   id: noxbox.id
               },
               topic: noxbox.owner.id
           };
         await admin.messaging().send(push);
         console.log('push sent' + JSON.stringify(push));
     }else if(!previousNoxbox.timePartyRejected && !previousNoxbox.timeOwnerRejected && noxbox.timeOwnerRejected){
            operationName =  'Rejected by Owner';
         let push = {
               data: {
                    type: 'rejected',
                    id: noxbox.id
               },
               topic: noxbox.party.id
            };
         await admin.messaging().send(push);
         console.log('push sent' + JSON.stringify(push));
     }else if(!previousNoxbox.timePartyRejected && !previousNoxbox.timeOwnerRejected && noxbox.timePartyRejected){
            operationName =  'Rejected by Party';
            let push = {
                           data: {
                               type: 'rejected',
                               id: noxbox.id
                           },
                           topic: noxbox.owner.id
                       };
                     await admin.messaging().send(push);
                     console.log('push sent' + JSON.stringify(push));
     }
     console.log('Operation ' + operationName + ' v ' + JSON.stringify(version));
});

async function persistRating(userId, role, type, rate, delta) {
    let docRef = db.collection('ratings').doc(userId);
    let doc = await db.runTransaction(t => t.get(docRef));
    if (!doc.exists) {
        console.log('Create rating');
        await docRef.set({ [role] : { [type] : { [rate] : 1}}});
    } else {
        console.log('Update rating');
        let ratings = doc.data();
        if(!ratings[role]){
            ratings[role] = {};
        }
        if(!ratings[role][type]){
            ratings[role][type] = {};
        }
        if(!ratings[role][type][rate]){
            ratings[role][type][rate] = 0;
        }

        let currentRate = ratings[role][type][rate] + delta;
        if(currentRate < 0){
            console.warn('currentRate is negative');
            currentRate = 0;
        }
        ratings[role][type][rate] = currentRate;
        await doc.ref.update(ratings);
        console.log(role + ' ' + type + ' ' + rate + ' ' + (currentRate));
    }
}

async function persistRatingComment(userWhoGotId, userWhoSentId, role, type, comment){
        let docRef = db.collection('ratings').doc(userWhoGotId);
        let doc = await db.runTransaction(t => t.get(docRef));
        if (doc.exists) {
            let comments = doc.data();
            if(!comments[role][type]['comments']){
                comments[role][type]['comments'] = {};
            }
            comments[role][type]['comments'][userWhoSentId] = comment;
            await doc.ref.update(comments);
            console.log('Transaction success!');
        }
}

async function updateRating(previousNoxbox, noxbox) {
    if(!noxbox.finished) return;

    if (!previousNoxbox.timeCompleted && noxbox.timeCompleted) {
        if(noxbox.role === 'supply'){
            await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'sentLikes', +1);
            await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'receivedLikes', +1);
            await persistRating(noxbox.party.id, 'demandsRating', noxbox.type, 'sentLikes', +1);
            await persistRating(noxbox.party.id, 'demandsRating', noxbox.type, 'receivedLikes', +1);
        } else {
            await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'sentLikes', +1);
            await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'receivedLikes', +1);
            await persistRating(noxbox.party.id, 'suppliesRating', noxbox.type, 'sentLikes', +1);
            await persistRating(noxbox.party.id, 'suppliesRating', noxbox.type, 'receivedLikes', +1);
        }
    }else if(!previousNoxbox.timeCanceledByOwner && noxbox.timeCanceledByOwner){
        if(noxbox.role === 'supply'){
            await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'canceled', +1);
        } else {
            await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'canceled', +1);
        }
    }else if(!previousNoxbox.timeCanceledByParty && noxbox.timeCanceledByParty){
        if(noxbox.role === 'supply'){
            await persistRating(noxbox.party.id, 'demandsRating', noxbox.type, 'canceled', +1);
        } else {
            await persistRating(noxbox.party.id, 'suppliesRating', noxbox.type, 'canceled', +1);
        }
    }else if(!previousNoxbox.timeTimeout && noxbox.timeTimeout){
        if(noxbox.role === 'supply'){
            await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'notResponded', +1);
        } else {
            await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'notResponded', +1);
        }
    }else if(!previousNoxbox.timeOwnerRejected && noxbox.timeOwnerRejected){
        if(noxbox.role === 'supply'){
            await persistRating(noxbox.party.id, 'demandsRating', noxbox.type, 'notVerified', +1);
        } else {
            await persistRating(noxbox.party.id, 'suppliesRating', noxbox.type, 'notVerified', +1);
        }
    }else if(!previousNoxbox.timePartyRejected && noxbox.timePartyRejected){
        if(noxbox.role === 'supply'){
            await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'notVerified', +1);
        } else {
            await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'notVerified', +1);
        }
    }else if(previousNoxbox.timeCompleted && noxbox.timeCompleted){
         if (!previousNoxbox.ownerComment && noxbox.ownerComment) {
            let comment = { time : Date.now(),
                            like : (noxbox.timeOwnerLiked >= noxbox.timeOwnerDisliked),
                            text : noxbox.ownerComment };
            if(noxbox.role === 'supply'){
                await persistRatingComment(noxbox.party.id, noxbox.owner.id, 'demandsRating', noxbox.type, comment);
            } else {
                await persistRatingComment(noxbox.party.id, noxbox.owner.id, 'suppliesRating', noxbox.type, comment);
            }

        } else if(!previousNoxbox.partyComment && noxbox.partyComment){
            let comment = { time : Date.now(),
                            like : (noxbox.timePartyLiked >= noxbox.timePartyDisliked),
                            text : noxbox.partyComment };
            if(noxbox.role === 'supply'){
                await persistRatingComment(noxbox.owner.id, noxbox.party.id, 'suppliesRating', noxbox.type, comment);
            } else {
                await persistRatingComment(noxbox.owner.id, noxbox.party.id, 'demandsRating', noxbox.type, comment);
            }
        } else if(previousNoxbox.timeOwnerDisliked !== noxbox.timeOwnerDisliked) {
            if(noxbox.role === 'supply'){
                //TODO unite dislikes and likes in single method
                await persistRating(noxbox.party.id, 'demandsRating', noxbox.type, 'receivedDislikes', +1);
                await persistRating(noxbox.party.id, 'demandsRating', noxbox.type, 'receivedLikes', -1);
                await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'sentDislikes', +1);
                await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'sentLikes', -1);
                if(noxbox.ownerComment){
                    let comment = { time : Date.now(),
                        like : (noxbox.timeOwnerLiked >= noxbox.timeOwnerDisliked),
                        text : noxbox.ownerComment };
                    await persistRatingComment(noxbox.party.id, noxbox.owner.id, 'demandsRating', noxbox.type, comment);
                }
            } else {
                await persistRating(noxbox.party.id, 'suppliesRating', noxbox.type, 'receivedDislikes', +1);
                await persistRating(noxbox.party.id, 'suppliesRating', noxbox.type, 'receivedLikes', -1);
                await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'sentDislikes', +1);
                await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'sentLikes', -1);
                if(noxbox.ownerComment){
                    let comment = { time : Date.now(),
                        like : (noxbox.timeOwnerLiked >= noxbox.timeOwnerDisliked),
                        text : noxbox.ownerComment };
                    await persistRatingComment(noxbox.party.id, noxbox.owner.id, 'suppliesRating', noxbox.type, comment);
                }
            }


        } else if(previousNoxbox.timeOwnerLiked !== noxbox.timeOwnerLiked) {
            if(noxbox.role === 'supply'){
                await persistRating(noxbox.party.id, 'demandsRating', noxbox.type, 'receivedDislikes', -1);
                await persistRating(noxbox.party.id, 'demandsRating', noxbox.type, 'receivedLikes', +1);
                await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'sentDislikes', -1);
                await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'sentLikes', +1);
                if(noxbox.ownerComment){
                    let comment = { time : Date.now(),
                         like : (noxbox.timeOwnerLiked >= noxbox.timeOwnerDisliked),
                         text : noxbox.ownerComment };
                    await persistRatingComment(noxbox.party.id, noxbox.owner.id, 'demandsRating', noxbox.type, comment);
                }
            } else {
                await persistRating(noxbox.party.id, 'suppliesRating', noxbox.type, 'receivedDislikes', -1);
                await persistRating(noxbox.party.id, 'suppliesRating', noxbox.type, 'receivedLikes', +1);
                await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'sentDislikes', -1);
                await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'sentLikes', +1);
                if(noxbox.ownerComment){
                    let comment = { time : Date.now(),
                         like : (noxbox.timeOwnerLiked >= noxbox.timeOwnerDisliked),
                         text : noxbox.ownerComment };
                    await persistRatingComment(noxbox.party.id, noxbox.owner.id, 'suppliesRating', noxbox.type, comment);
                }
            }

        } else if(previousNoxbox.timePartyDisliked !== noxbox.timePartyDisliked){
            if(noxbox.role === 'supply'){
                await persistRating(noxbox.party.id, 'demandsRating', noxbox.type, 'sentDislikes', +1);
                await persistRating(noxbox.party.id, 'demandsRating', noxbox.type, 'sentLikes', -1);
                await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'receivedDislikes', +1);
                await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'receivedLikes', -1);
                if(noxbox.partyComment){
                    let comment = { time : Date.now(),
                        like : (noxbox.timePartyLiked >= noxbox.timePartyDisliked),
                        text : noxbox.partyComment };
                    await persistRatingComment(noxbox.owner.id, noxbox.party.id, 'suppliesRating', noxbox.type, comment);
                }
            } else {
                await persistRating(noxbox.party.id, 'suppliesRating', noxbox.type, 'sentDislikes', +1);
                await persistRating(noxbox.party.id, 'suppliesRating', noxbox.type, 'sentLikes', -1);
                await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'receivedDislikes', +1);
                await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'receivedLikes', -1);
                if(noxbox.partyComment){
                    let comment = { time : Date.now(),
                        like : (noxbox.timePartyLiked >= noxbox.timePartyDisliked),
                        text : noxbox.partyComment };
                    await persistRatingComment(noxbox.owner.id, noxbox.party.id, 'demandsRating', noxbox.type, comment);
                }
            }

        } else if(previousNoxbox.timePartyLiked !== noxbox.timePartyLiked){
            if(noxbox.role === 'supply'){
                await persistRating(noxbox.party.id, 'demandsRating', noxbox.type, 'sentDislikes', -1);
                await persistRating(noxbox.party.id, 'demandsRating', noxbox.type, 'sentLikes', +1);
                await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'receivedDislikes', -1);
                await persistRating(noxbox.owner.id, 'suppliesRating', noxbox.type, 'receivedLikes', +1);
                if(noxbox.partyComment){
                    let comment = { time : Date.now(),
                        like : (noxbox.timePartyLiked >= noxbox.timePartyDisliked),
                        text : noxbox.partyComment };
                    await persistRatingComment(noxbox.owner.id, noxbox.party.id, 'suppliesRating', noxbox.type, comment);
                }
            } else {
                await persistRating(noxbox.party.id, 'suppliesRating', noxbox.type, 'sentDislikes', -1);
                await persistRating(noxbox.party.id, 'suppliesRating', noxbox.type, 'sentLikes', +1);
                await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'receivedDislikes', -1);
                await persistRating(noxbox.owner.id, 'demandsRating', noxbox.type, 'receivedLikes', +1);
                if(noxbox.partyComment){
                    let comment = { time : Date.now(),
                        like : (noxbox.timePartyLiked >= noxbox.timePartyDisliked),
                        text : noxbox.partyComment };
                    await persistRatingComment(noxbox.owner.id, noxbox.party.id, 'demandsRating', noxbox.type, comment);
                }
            }
        }

    }
}

function latestMessage(messages) {
    let message;
    for(time in messages) {
        if(!message || messages[time].time > message.time) {
            message = messages[time];
        }
    }
    return message;
}

// TODO read available noxboxes from firestore directly when draw map on site
exports.map = functions.https.onRequest((req, res) => {
    admin.database().ref('geo').once('value').then(allServices => {
        let json = [];
        allServices.forEach(service => {
            json.push({ key : service.key,
                        latitude : service.val().l[0],
                        longitude : service.val().l[1]
        })});
        console.log(json.length);
        res.status(200).send('map_callback(' + JSON.stringify(json) + ')');
    });
});

exports.transfer = functions.https.onCall(async (data, context) => {
    console.log('id', context.auth.uid);
    console.log('addressToTransfer', data.addressToTransfer);
    // check if active noxbox present
    let currentNoxboxToPay = await db.collection('noxboxes')
        .where('payerId', '==', context.auth.uid).where('finished', '==', false).get();
    if(!currentNoxboxToPay.empty) {
        currentNoxboxToPay.forEach(one => {
            console.log('notFinished', one.data().id);
        });
        throw Error ('Attempt to return money with not finished noxbox');
    }

    let request = { addressToTransfer : data.addressToTransfer};
    request.encrypted = (await db.collection('seeds').doc(context.auth.uid).get()).data().seed;
    request.attachment = 'Money transfer. Processed with love by NoxBox';
    return wallet.send(request);
});

// export in emergency case only
reset = functions.https.onRequest(async (req, res) => {
    let profiles = await db.collection('profiles').get();
    profiles.forEach(data => {
        let profile = data.data();
        profile.noxboxId = '';
        db.collection('profiles').doc(profile.id).set(profile);
    });
    res.status(200).send('Version ' + version);
});

exports.balance = functions.https.onRequest(async (req, res) => {
    let balance = new BigDecimal('0');
    let profiles = await db.collection('profiles').get();
    profiles.forEach(data => {
         let profile = data.data();
         if(profile.wallet && profile.wallet.balance)
            balance = balance.plus(new BigDecimal(profile.wallet.balance));
    });
    res.status(200).send('Balance ' + balance + ' Waves');
});

exports.version = functions.https.onRequest((req, res) => {
    res.status(200).send('Version ' + version);
});

