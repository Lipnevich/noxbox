const functions = require('firebase-functions');
const admin = require('firebase-admin');
const BigDecimal = require('big.js');
const Q = require('q');

const rewardAccount = functions.config().keys.rewardaccount;
const db = admin.database();
const users = db.ref('users')

exports.init = function (request) {
    var deferred = Q.defer();

    request.profile = {};
    request.profile.uid = request.uid;
    request.profile.email = request.email;
    request.profile.travelMode = 'walking';
    if(request.photoUrl) request.profile.photoUrl = user.photoUrl;
    if(request.displayName) request.profile.displayName = user.displayName;

    request.rating = {};
    request.rating.received = {};
    request.rating.received.liked = 0;
    request.rating.received.dislikes = 0;
    request.rating.sent = {};
    request.rating.sent.liked = 0;
    request.rating.sent.dislikes = 0;
    request.rating.notResponded = 0;
    request.rating.canceled = 0;

    users.child(request.uid).set({
        'profile' : request.profile,
        'wallet' : request.wallet,
        'rating' : request.rating;
    });
    deferred.resolve(request);

    return deferred.promise;
}

exports.notifyBalanceUpdated = function (request) {
    var deferred = Q.defer();

    let ref = events.child(request.id).child('balanceUpdated');
    var messageId = ref.push().key;
    ref.child(messageId).set({
        'id' : messageId,
        'type' : 'balanceUpdated',
        'wallet' : { 'balance' : request.wallet.balance,
                     'address' : request.wallet.address }
    });
    deferred.resolve(request);

    return deferred.promise;
}

exports.notifyPayerBalanceUpdated = function (request) {
    var deferred = Q.defer();

    var payerId;
    for(id in request.noxbox.payers) {
        payerId = id;
    }

    db.ref('profiles').child(payerId).child('wallet').once('value').then(function(snapshot) {
        request.wallet = snapshot.val();
        if(!request.wallet) request.wallet = {};
        request.wallet.balance = request.wallet.balance || '0';
        request.wallet.frozenMoney = request.wallet.frozenMoney || '0';
        request.wallet.availableMoney = '' + (new BigDecimal(request.wallet.balance)
            .minus(new BigDecimal(request.wallet.frozenMoney)));

        var ref = events.child(payerId).child('balanceUpdated');
        var eventId = ref.push().key;
        ref.child(eventId).set({
            'id' : eventId,
            'type' : 'balanceUpdated',
            'wallet' : { 'balance' : request.wallet.balance,
                         'address' : request.wallet.address,
                         'frozenMoney' : request.wallet.frozenMoney}
        });

        deferred.resolve(request);
    });

    return deferred.promise;
}

exports.releaseRequest = function (request) {
    var deferred = Q.defer();

    db.ref('requests/' + request.id).remove();
    deferred.resolve(request);

    return deferred.promise;
}

exports.updateBalance = function (request) {
    var deferred = Q.defer();

    db.ref('profiles/' + request.id + '/wallet').update(request.wallet);
    deferred.resolve(request);

    return deferred.promise;
}

exports.createRating = function (request) {
    var deferred = Q.defer();

    db.ref('profiles/' + request.id + '/rating').set({
        received : {likes : 0, dislikes : 0},
        sent : {likes : 0, dislikes : 0}
    });
    deferred.resolve(request);

    return deferred.promise;
}

exports.getWallet = function (request) {
    var deferred = Q.defer();

    console.log(request);

    db.ref('profiles').child(request.id).child('wallet').once('value').then(function(snapshot) {
        request.wallet = snapshot.val();
        if(!request.wallet || !request.wallet.address) {
            request.error = 'Wallet is not created yet';
            deferred.reject(request);
        } else {
            request.wallet.balance = request.wallet.balance || '0';
            request.wallet.previousBalance = request.wallet.balance;
            request.wallet.frozenMoney = request.wallet.frozenMoney || '0';
            request.wallet.availableMoney = '' + (new BigDecimal(request.wallet.balance)
                .minus(new BigDecimal(request.wallet.frozenMoney)));

            deferred.resolve(request);
        }
    });

    return deferred.promise;
}

exports.getRewardWallet = function (request) {
    var deferred = Q.defer();

    request.reward = {};
    console.log('Reward account ' + rewardAccount);
    if(!rewardAccount) {
        deferred.resolve(request);
    } else {
        users.child(rewardAccount).child('wallet').child('seed').once('value').then(function(snapshot) {
            request.reward.payerId = rewardAccount;
            request.reward.seed = snapshot.val();
            if(!request.reward.seed) {
                console.log('Reward account is not exists ' + rewardAccount);
            }
            deferred.resolve(request);
        });
    }

    return deferred.promise;
}

exports.getPrice = function (request) {
    var deferred = Q.defer();

    db.ref('prices').child(request.noxboxType).once('value').then(function(snapshot) {
        request.price = snapshot.val();
        if(!request.price) {
            request.error = 'Price for ' + request.noxboxType + ' not exist. Please setup prices table.';
            deferred.reject(request);
        }
        deferred.resolve(request);
    });

    return deferred.promise;
}

exports.isEnoughMoneyForRequest = function (request) {
    var deferred = Q.defer();

    if(!request.wallet.availableMoney ||
        new BigDecimal(request.wallet.availableMoney).lte(new BigDecimal(request.price))) {
        request.error = 'No money available for request';
        deferred.reject(request);
    }
    deferred.resolve(request);
    return deferred.promise;
}

exports.isEnoughMoneyForRefund = function (request) {
    var deferred = Q.defer();

    if(!request.wallet.availableMoneyWithoutFee ||
        new BigDecimal(request.wallet.availableMoneyWithoutFee).lte(new BigDecimal(0))) {
        console.log(request.wallet.availableMoneyWithoutFee);
        request.error = 'No money available for refund';
        deferred.reject(request);
    }
    deferred.resolve(request);
    return deferred.promise;
}

exports.getPayerProfile = function (request) {
    var deferred = Q.defer();

    db.ref('profiles').child(request.payer.id).once('value').then(function(snapshot) {
        var payer = snapshot.val().profile;
        var rating = snapshot.val().rating;
        var notificationKeys = snapshot.val().notificationKeys;

        request.payer = {
            'id' : payer.id,
            'name' : payer.name
        };
        if(notificationKeys && notificationKeys.payerAndroidKey) {
            request.payerAndroidNotificationKey = notificationKeys.payerAndroidKey;
        }
        if(payer.photo) {
            request.payer.photo = payer.photo;
        }
        if(rating) {
            request.payer.rating = rating;
        }
        deferred.resolve(request);
    });

    return deferred.promise;
}

exports.getPayerSeed = function (request) {
    var deferred = Q.defer();

    // TODO (nli) pay to each performer in case there is more then 1 performer in noxbox
    if(!request.payer) request.payer = {};
    for(id in request.noxbox.payers) {
        request.payer.id = id;
    }

    db.ref('profiles').child(request.payer.id).child("wallet").child("seed").once('value').then(function(snapshot) {
        request.payer.seed = snapshot.val();
        deferred.resolve(request);
    });

    return deferred.promise;
}

exports.getPerformerAddress = function (request) {
    var deferred = Q.defer();

    var performerId;
    if(!request.performer) request.performer = {};
    for(id in request.noxbox.performers) {
        request.performer.id = id;
    }

    db.ref('profiles').child(request.performer.id).child("wallet").child("address").once('value').then(function(snapshot)   {
        request.performer.address = snapshot.val();
        deferred.resolve(request);
    });

    return deferred.promise;
}

exports.getPerformerProfile = function (request) {
    var deferred = Q.defer();

    db.ref('profiles').child(request.performer.id).once('value').then(function(snapshot) {
        if(!snapshot.val()) {
            request.error = 'Performer does not exists ' + request.performer.id;
            deferred.reject(request);
        } else {
            var performer = snapshot.val().profile;
            var rating = snapshot.val().rating;
            var notificationKeys = snapshot.val().notificationKeys;

            request.performer = {
                'id' : performer.id,
                'name' : performer.name,
                'position' : request.performer.position,
                'travelMode' : performer.travelMode
            };

            if(notificationKeys && notificationKeys.performerAndroidKey) {
                request.performerAndroidNotificationKey = notificationKeys.performerAndroidKey;
            }
            if(performer.photo) {
                request.performer.photo = performer.photo;
            }
            if(rating) {
                request.performer.rating = rating;
            }

            deferred.resolve(request);
        }
    });

    return deferred.promise;
}

exports.createNoxbox = function (request) {
    var deferred = Q.defer();

    var noxboxRef = db.ref('noxboxes').child(request.noxboxType);
    var noxboxId = noxboxRef.push().key;
    request.noxbox = {
        'id' : noxboxId,
        'type' : request.noxboxType,
        'price' : '' + request.price,
        'position' : request.position,
        'estimationTime' : request.estimationTime,
        'timeRequested' : new Date().getTime(),
        'payers' : { [request.id] : request.payer },
        'performers' : { [request.performer.id] : request.performer }
    }
    console.log(request.noxbox);
    noxboxRef.child(request.noxbox.id).set(request.noxbox);
    deferred.resolve(request);
    return deferred.promise;
}

exports.pingPerformer = function (request) {
    var deferred = Q.defer();

    let ref = events.child(request.performer.id).child('ping');
    var messageId = ref.push().key;
    ref.child(messageId).set({
        'id' : messageId,
        'type' : 'ping',
        'payer' : request.payer,
        'estimationTime' : request.estimationTime,
        'time' : new Date().getTime(),
        'noxbox' : request.noxbox
    });

    deferred.resolve(request);

    return deferred.promise;
}

exports.sendPushNotification = function(request) {
    return getPushToken(request).then(sendPush);
}

sendPush = function (request) {
    var deferred = Q.defer();

    if(request.push && request.push.data && request.push.token) {
        if(request.wallet) {
            request.push.data.balance = request.wallet.balance;
            request.push.data.previousBalance = request.wallet.previousBalance;
        }

        console.log(request.push.data);

        let message = {
            data : request.push.data,
            android : { ttl: 1000 * 30 },
            token : request.push.token
        }

        admin.messaging().send(message).then((response) => {
            console.log('Successfully push:', response);
            deferred.resolve(request);
        }).catch((error) => {
            console.log('Error sending message:', error);
            deferred.resolve(request);
        });
    } else {
        deferred.resolve(request);
    }

    return deferred.promise;
}

getPushToken = function (request) {
    var deferred = Q.defer();

    if(request.push && !request.push.token) {
        db.ref('profiles').child(request.push.to).child("notificationKeys").once('value').then(function(snapshot) {
            var notificationKeys = snapshot.val();

            if (request.push.role == 'payer' && notificationKeys && notificationKeys.payerAndroidKey) {
                request.push.token = notificationKeys.payerAndroidKey;
            } else if (request.push.role == 'performer' && notificationKeys && notificationKeys.performerAndroidKey) {
                request.push.token = notificationKeys.performerAndroidKey;
            }

            deferred.resolve(request);
        });
    } else {
        deferred.resolve(request);
    }

    return deferred.promise;

}

exports.freezeMoney = function (request) {
    var deferred = Q.defer();

    // firebase can process transaction up to 25 times in case of issues with multi access,
    // for the first time transaction processed with null instead of real value
    db.ref('profiles').child(request.id).child('wallet').transaction(function(current) {
        if(!current) {
            return 0;
        }

        request.wallet.frozenMoney = '' + (new BigDecimal(request.wallet.frozenMoney)
                .add(new BigDecimal(request.price)));

        current.frozenMoney = request.wallet.frozenMoney;
        console.log('Money was frozen', current);
        return current;
    }, function(error, committed, snapshot) {
        if (error || !committed) {
            request.error = error;
            deferred.reject(request);
        } else {
            deferred.resolve(request);
        }
    }, true);

    return deferred.promise;
}

exports.unfreezeMoney = function (request) {
    var deferred = Q.defer();

    // TODO (nli) unfreeze for everyone in case more then 1 payer in noxbox
    var payerId;
    for(id in request.noxbox.payers) {
        payerId = id;
    }
    // firebase can process transaction up to 25 times in case of issues with multi access,
    // for the first time transaction processed with null instead of real value
    db.ref('profiles').child(payerId).child('wallet').transaction(function(current) {
        if(!current) {
            return 0;
        }

        current.frozenMoney = '' + (new BigDecimal(current.frozenMoney)
            .minus(new BigDecimal(request.noxbox.price)));
        console.info('Money unfreezed', current);
        request.wallet = current;
        return current;
    }, function(error, committed, snapshot) {
        if (error || !committed) {
            request.error = error;
            deferred.reject(request);
        } else {
            deferred.resolve(request);
        }
    }, true);

    return deferred.promise;
}

exports.cancelNoxbox = function (request) {
    var deferred = Q.defer();

    if(request.reason) {
        console.info(request.reason);
    }
    // firebase can process transaction up to 25 times in case of issues with multi access,
    // for the first time transaction processed with null instead of real value
    db.ref('noxboxes').child(request.noxboxType).child(request.noxbox.id).transaction(function(current) {
        if(!current) {
            return 0;
        }

        // timeout in case more than a single minute gone since request
        let timeout = ((new Date().getTime() - current.timeRequested) / 1000) > 30;
        if(!current.timeAccepted && timeout) {
            request.acceptTimeout = true;
        }

        if(current.timeCompleted) {
            request.error = 'Noxbox already completed ' + current.timeCompleted;
            deferred.reject(request);
            return current;
        }

        if(current.timeCanceled) {
            request.error = 'Noxbox already canceled ' + current.timeCanceled;
            deferred.reject(request);
            return current;
        }

        var isCanceledByPayer = false;
        for(payerId in current.payers) {
            if(request.id == payerId) {
                isCanceledByPayer = true;
            }
        }

        var isCanceledByPerformer = false;
        for(performerId in current.performers) {
            if(request.id == performerId) {
                isCanceledByPerformer = true;
            }
        }

        if(!isCanceledByPayer && !isCanceledByPerformer) {
            request.error = 'Attempt to cancel noxbox not by member';
            deferred.reject(request);
            return current;
        }

        if(isCanceledByPerformer && current.timeAccepted) {
            request.error = 'Attempt to cancel noxbox by performer after accept';
            deferred.reject(request);
            return current;
        }

        current.timeCanceled = new Date().getTime();
        console.info('Noxbox canceled', current);
        request.noxbox = current;
        return current;
    }, function(error, committed, snapshot) {
        if (error || !committed) {
            request.error = error;
            deferred.reject(request);
        } else {
            deferred.resolve(request);
        }
    }, true);

    return deferred.promise;
}

exports.removeAvailablePerformer = function (request) {
    var deferred = Q.defer();

    if(request.acceptTimeout) {
        db.ref('availablePerformers').child(request.noxboxType).once('value').then(function(snapshot) {
              if(snapshot.hasChildren()) {
                snapshot.forEach(function(item) {
                    if(item.key.startsWith(request.performer.id)) {
                        db.ref('availablePerformers').child(request.noxboxType)
                            .child(item.key).remove();
                        console.info('Performer ' + item.key + ' become not available due to timeout');
                    }
                });
              }
        });
    }
    deferred.resolve(request);

    return deferred.promise;
}


exports.acceptNoxbox = function (request) {
    var deferred = Q.defer();

    // firebase can process transaction up to 25 times in case of issues with multi access,
    // for the first time transaction processed with null instead of real value
    db.ref('noxboxes').child(request.noxboxType).child(request.noxbox.id).transaction(function(current) {
        if(!current) {
            return 0;
        }

        if(current.timeCompleted) {
            request.error = 'Noxbox already completed ' + current.timeCompleted;
            deferred.reject(request);
            return current;
        }

        if(current.timeCanceled) {
            request.error = 'Noxbox already canceled ' + current.timeCanceled;
            deferred.reject(request);
            return current;
        }

        var isAcceptedByPerformer = false;
        for(performerId in current.performers) {
            if(request.id == performerId) {
                isAcceptedByPerformer = true;
            }
        }

        if(!isAcceptedByPerformer) {
            request.error = 'Attempt to accept noxbox not by performer';
            deferred.reject(request);
            return current;
        }

        current.timeAccepted = new Date().getTime();
        request.noxbox = current;
        console.info('Noxbox accepted', current);
        return current;
    }, function(error, committed, snapshot) {
        if (error || !committed) {
            request.error = error;
            deferred.reject(request);
        } else {
            deferred.resolve(request);
        }
    }, true);

    return deferred.promise;
}

exports.completeNoxbox = function (request) {
    var deferred = Q.defer();

    // firebase can process transaction up to 25 times in case of issues with multi access,
    // for the first time transaction processed with null instead of real value
    db.ref('noxboxes').child(request.noxboxType).child(request.noxbox.id).transaction(function(current) {
        if(!current) {
            return 0;
        }

        if(current.timeCompleted) {
            request.error = 'Noxbox already completed ' + current.timeCompleted;
            deferred.reject(request);
            return current;
        }

        if(current.timeCanceled) {
            request.error = 'Noxbox already canceled ' + current.timeCanceled;
            deferred.reject(request);
            return current;
        }

        var isCompletedByPerformer = false;
        for(performerId in current.performers) {
            if(request.id == performerId) {
                isCompletedByPerformer = true;
            }
        }

        if(!isCompletedByPerformer) {
            request.error = 'Attempt to complete noxbox not by performer';
            deferred.reject(request);
            return current;
        }

        current.timeCompleted = new Date().getTime();
        request.noxbox = current;
        console.info('Noxbox completed', current);
        return current;
    }, function(error, committed, snapshot) {
        if (error || !committed) {
            request.error = error;
            deferred.reject(request);
        } else {
            deferred.resolve(request);
        }
    }, true);

    return deferred.promise;
}

exports.notifyCanceled = function (request) {
    var deferred = Q.defer();

    for(performerId in request.noxbox.performers) {
        if(performerId != request.id) {
            var ref = events.child(performerId).child('gnop');
            var messageId = ref.push().key;
            ref.child(messageId).set({
                'id' : messageId,
                'type' : 'gnop',
                'noxbox' : request.noxbox
            });
        }
    }

    for(payerId in request.noxbox.payers) {
        if(payerId != request.id) {
            var ref = events.child(payerId).child('gnop');
            var messageId = ref.push().key;
            ref.child(messageId).set({
                'id' : messageId,
                'type' : 'gnop',
                'noxbox' : request.noxbox
            });
        }
    }

    console.info('Cancel notified');
    deferred.resolve(request);
    return deferred.promise;
}

exports.notifyAccepted = function (request) {
    var deferred = Q.defer();

    for(payerId in request.noxbox.payers) {
        let ref = events.child(payerId).child('pong');
        let eventId = ref.push().key;
        ref.child(eventId).set({
            'id' : eventId,
            'type' : 'pong',
            'noxbox' : request.noxbox,
            'estimationTime' : request.estimationTime
        });
    }

    deferred.resolve(request);
    return deferred.promise;
}

exports.syncPayer = function (request) {
    var deferred = Q.defer();

    for(payerId in request.noxbox.payers) {
        let ref = events.child(payerId).child('sync');
        let eventId = ref.push().key;
        ref.child(eventId).set({
            'id' : eventId,
            'type' : 'sync',
            'noxbox' : request.noxbox,
            'estimationTime' : request.estimationTime
        });
    }

    deferred.resolve(request);
    return deferred.promise;
}

exports.generateSecret = function (request) {
    var deferred = Q.defer();

    for(payerId in request.noxbox.payers) {
        let secret = Math.random().toString(36).substring(2, 15);
        console.log('Secret generated ', secret);
        request.noxbox.payers[payerId].secret = secret;
    }

    deferred.resolve(request);
    return deferred.promise;
}

exports.notifyCompleted = function (request) {
    var deferred = Q.defer();

    for(payerId in request.noxbox.payers) {
        var ref = db.ref('events').child(payerId).child('complete');
        var messageId = ref.push().key;
        ref.child(messageId).set({
            'id' : messageId,
            'type' : 'complete',
            'noxbox' : request.noxbox
        });
    }

    deferred.resolve(request);
    return deferred.promise;
}

exports.logError = function (request) {
    var deferred = Q.defer();

    if(request.error) {
        console.warn(request.error);
    } else {
        console.error('Error ', request);
    }

    deferred.resolve(request);
    return deferred.promise;
}

exports.updateTransferWallets = function (request) {
    return updatePayerBalance(request).then(updateReceiverBalance);
}

updatePayerBalance = function (request) {
    var deferred = Q.defer();

    if(!request.transfer.payerId) {
        deferred.resolve(request);
    } else {
        // firebase can process transaction up to 25 times in case of issues with multi access,
        // for the first time transaction processed with null instead of real value
        db.ref('profiles').child(request.transfer.payerId).child('wallet').transaction(function(current) {
            if(!current) {
                return 0;
            }

            current.balance = '' + (new BigDecimal(current.balance || '0')
                .minus(new BigDecimal(request.transfer.amount || '0')));
            if(request.transfer.unfreeze) {
                current.frozenMoney = '' + (new BigDecimal(current.frozenMoney || '0')
                    .minus(new BigDecimal(request.transfer.amount || '0')));
            }

            if(request.transfer.payerId == request.id) {
                request.wallet = current;
            }
            return current;
        }, function(error, committed, snapshot) {
            if (error || !committed) {
                request.error = error;
                deferred.reject(request);
            } else {
                deferred.resolve(request);
            }
        }, true);
    }

    return deferred.promise;
}

updateReceiverBalance = function (request) {
    var deferred = Q.defer();

    if(!request.transfer.receiverId) {
        deferred.resolve(request);
    } else {
        // firebase can process transaction up to 25 times in case of issues with multi access,
        // for the first time transaction processed with null instead of real value
        db.ref('profiles').child(request.transfer.receiverId).child('wallet').transaction(function(current) {
            if(!current) {
                return 0;
            }

            current.balance = '' + (new BigDecimal(current.balance || '0')
                .add(new BigDecimal(request.transfer.amount || '0')));
            current.frozenMoney = current.frozenMoney || '0';

            if(request.transfer.receiverId == request.id) {
                request.wallet = current;
            }
            return current;
        }, function(error, committed, snapshot) {
            if (error || !committed) {
                request.error = error;
                deferred.reject(request);
            } else {
                deferred.resolve(request);
            }
        }, true);
    }

    return deferred.promise;
}


exports.likePayer = function (request) {
    var deferred = Q.defer();

    var payerId;
    for(id in request.noxbox.payers) {
        payerId = id;
    }
    // firebase can process transaction up to 25 times in case of issues with multi access,
    // for the first time transaction processed with null instead of real value
    db.ref('profiles').child(payerId).child('rating').transaction(function(current) {
        if(!current) {
            return 0;
        }
        current.received.likes = current.received.likes + 1;
        current.sent.likes = current.sent.likes + 1;

        return current;
    }, function(error, committed, snapshot) {
        if (error || !committed) {
            request.error = error;
            deferred.reject(request);
        } else {
            console.log('Payer was liked', snapshot.val());
            deferred.resolve(request);
        }
    }, true);

    return deferred.promise;
}

exports.likePerformer = function (request) {
    var deferred = Q.defer();

    var performerId;
    for(id in request.noxbox.performers) {
        performerId = id;
    }
    // firebase can process transaction up to 25 times in case of issues with multi access,
    // for the first time transaction processed with null instead of real value
    db.ref('profiles').child(performerId).child('rating').transaction(function(current) {
        if(!current) {
            return 0;
        }
        current.received.likes = current.received.likes + 1;
        current.sent.likes = current.sent.likes + 1;

        return current;
    }, function(error, committed, snapshot) {
        if (error || !committed) {
            request.error = error;
            deferred.reject(request);
        } else {
            console.log('Performer was liked', snapshot.val());
            deferred.resolve(request);
        }
    }, true);

    return deferred.promise;
}

exports.storePriceWithoutFeeInNoxbox = function (request) {
    var deferred = Q.defer();

    db.ref('noxboxes').child(request.noxbox.type).child(request.noxbox.id)
        .update({'priceWithoutFee' : request.priceWithoutFee});
    deferred.resolve(request);

    return deferred.promise;
}

exports.dislikeNoxbox = function (request) {
    var deferred = Q.defer();

    db.ref('noxboxes').child(request.noxboxType).child(request.noxbox.id).transaction(function(current) {
        // firebase can process transaction up to 25 times in case of issues with multi access,
        // for the first time transaction processed with null instead of real value
        if(!current) {
            return 0;
        }

        var profile;
        if(current.payers[request.id]) {
            request.isPayer = true;
            profile = current.payers[request.id];
        }

        if(current.performers[request.id]) {
            request.isPerformer = true;
            profile = current.performers[request.id];
        }

        if(!request.isPayer && !request.isPerformer) {
            request.error = 'Request from not an noxbox member ' + request.id;
            deferred.reject(request);
            return current;
        }

        if(!current.timeCompleted) {
            request.error = 'Dislike attempt for noxbox which is not completed';
            deferred.reject(request);
            return current;
        }

        if(profile.timeDisliked) {
            request.error = 'Attempt to second dislike noxbox ' + current.id;
            deferred.reject(request);
            return current;
        }

        profile.timeDisliked = new Date().getTime();
        request.noxbox = current;
        if(request.isPayer) {
            console.info('Noxbox ' + request.noxbox.id + ' disliked by payer');
        } else {
            console.info('Noxbox ' + request.noxbox.id + ' disliked by performer');
        }
        return current;
    }, function(error, committed, snapshot) {
        if (error || !committed) {
            request.error = error;
            deferred.reject(request);
        } else {
            deferred.resolve(request);
        }
    }, true);

    return deferred.promise;
}

exports.blacklist = function (request) {
    var deferred = Q.defer();

    for(performerId in request.noxbox.performers) {
        for(payerId in request.noxbox.payer) {
            // TODO (nli) how can we store list of ids?
            console.info('Blacklisted Payer id ' + payerId + ' for Performer id ' + performerId);
            db.ref('profiles').child(payerId).child('blacklist').child(performerId).set({});
            db.ref('profiles').child(performerId).child('blacklist').child(payerId).set({});
        }
    }

    deferred.resolve(request);
    return deferred.promise;
}

exports.updateRatings = function (request) {
    var deferred = Q.defer();

    if(request.isPayer) {
        for(performerId in request.noxbox.performers) {
            console.info('Update rating for performer ' + performerId +  ' and payer ' + request.id);
            dislikeRates(request.id, performerId);
        }
    } else if(request.isPerformer) {
        for(payerId in request.noxbox.payers) {
            console.info('Update rating for payer ' + payerId +  ' and performer ' + request.id);
            dislikeRates(request.id, payerId);
        }
    }

    deferred.resolve(request);
    return deferred.promise;
}

function dislikeRates(from, to) {
    // this updated could be async safe

    db.ref('profiles').child(from).child('rating').transaction(function(current) {
        // firebase can process transaction up to 25 times in case of issues with multi access,
        // for the first time transaction processed with null instead of real value
        if(!current) {
            return 0;
        }
        current.sent.likes = current.sent.likes - 1;
        current.sent.dislikes = current.sent.dislikes + 1;

        return current;
    }, function(error, committed, snapshot) {
        if (error || !committed) {
            console.warn('Failed to update rating from ', from);
        }
    }, true);

    db.ref('profiles').child(to).child('rating').transaction(function(current) {
            // firebase can process transaction up to 25 times in case of issues with multi access,
            // for the first time transaction processed with null instead of real value
            if(!current) {
                return 0;
            }
            current.received.likes = current.received.likes - 1;
            current.received.dislikes = current.received.dislikes + 1;

            return current;
        }, function(error, committed, snapshot) {
            if (error || !committed) {
                console.warn('Failed to update rating to ', to);
            }
    }, true);

}

exports.notifyDisliked = function (request) {
    var deferred = Q.defer();

    if(request.isPayer) {
        for(performerId in request.noxbox.performers) {
            console.info('Dislike message was sent to performer ' + performerId);
            sendDislike(performerId);
        }
    } else if(request.isPerformer) {
        for(payerId in request.noxbox.payers) {
            console.info('Dislike message was sent to payer ' + payerId);
            sendDislike(payerId);
        }
    }

    deferred.resolve(request);
    return deferred.promise;
}

function sendDislike(to) {
    var ref = db.ref('events').child(to).child('dislike');
    var messageId = ref.push().key;
    ref.child(messageId).set({
        'id' : messageId,
        'type' : 'dislike',
    });
}

exports.info = function () {
    var deferred = Q.defer();

    db.ref('profiles').once('value').then(function(event) {
        let total = new BigDecimal(0);
        let frozenTotal = new BigDecimal(0);
        let number = 0;
        if (event.hasChildren()) {
            event.forEach(function(item) {
               let balance = item.child('wallet').child('balance').val();
               let frozenMoney = item.child('wallet').child('frozenMoney').val();
               if(balance && balance != null) {
                   total = total.add(new BigDecimal(balance));
                   if(frozenMoney && frozenMoney != null) {
                        frozenTotal = frozenTotal.add(new BigDecimal(frozenMoney));
                   }
                   number++;
               }
            });

            var info = { 'members' : number, 'balance' : + total, 'frozen' : + frozenTotal  }
            console.log(info);
        }

        deferred.resolve(info);
    });

    return deferred.promise;
}

exports.unfreezeAll = function () {
    var deferred = Q.defer();

    db.ref('profiles').once('value').then(function(event) {
        if (event.hasChildren()) {
            event.forEach(function(item) {
                db.ref('profiles').child(item.key).child('wallet').child('frozenMoney').set('0');
            });
        }

        deferred.resolve('all money were unfreezed');
    });

    return deferred.promise;
}


