const functions = require('firebase-functions');
const admin = require('firebase-admin');
const math = require("bigdecimal");
const Q = require('q');

const db = admin.database();

exports.notifyBalanceUpdated = function (request) {
    var deferred = Q.defer();

    if(request.id == "default") {
        deferred.resolve(request);
    } else {
        let ref = db.ref('messages').child(request.id);
        var messageId = ref.push().key;
        ref.child(messageId).set({
            'id' : messageId,
            'type' : 'balanceUpdated',
            'wallet' : { 'balance' : request.wallet.balance,
                         'address' : request.wallet.address }
        });
        deferred.resolve(request);
    }

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
        request.wallet.availableMoney = '' + (new math.BigDecimal(request.wallet.balance)
            .subtract(new math.BigDecimal(request.wallet.frozenMoney)));

        var ref = db.ref('messages').child(payerId);
        var messageId = ref.push().key;
        ref.child(messageId).set({
            'id' : messageId,
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

    db.ref('profiles').child(request.id).child('wallet').once('value').then(function(snapshot) {
        request.wallet = snapshot.val();
        if(!request.wallet) {
            request.error = 'Wallet is not created yet';
            deferred.reject(request);
        }

        request.wallet.balance = request.wallet.balance || '0';
        request.wallet.frozenMoney = request.wallet.frozenMoney || '0';
        request.wallet.availableMoney = '' + (new math.BigDecimal(request.wallet.balance)
            .subtract(new math.BigDecimal(request.wallet.frozenMoney)));

        deferred.resolve(request);
    });

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
        new math.BigDecimal(request.wallet.availableMoney).compareTo(new math.BigDecimal(request.price)) < 0) {
        console.log(request.wallet.availableMoney);
        request.error = 'No money available for request';
        deferred.reject(request);
    }
    deferred.resolve(request);
    return deferred.promise;
}

exports.isEnoughMoneyForRefund = function (request) {
    var deferred = Q.defer();

    if(!request.wallet.availableMoneyWithoutFee ||
        new math.BigDecimal(request.wallet.availableMoneyWithoutFee).compareTo(new math.BigDecimal(0)) <= 0) {
        console.log(request.wallet.availableMoneyWithoutFee);
        request.error = 'No money available for refund';
        deferred.reject(request);
    }
    deferred.resolve(request);
    return deferred.promise;
}

exports.getPayerProfile = function (request) {
    var deferred = Q.defer();

    db.ref('profiles').child(request.id).once('value').then(function(snapshot) {
        var payer = snapshot.val().profile;
        var rating = snapshot.val().rating;

        request.payer = {
            'id' : payer.id,
            'name' : payer.name
        };
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
        var performer = snapshot.val().profile;
        var rating = snapshot.val().rating;

        request.performer = {
            'id' : performer.id,
            'name' : performer.name,
            'position' : request.performer.position,
            'travelMode' : performer.travelMode
        };
        if(performer.photo) {
            request.performer.photo = performer.photo;
        }
        if(rating) {
            request.performer.rating = rating;
        }

        deferred.resolve(request);
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

    let ref = db.ref('messages').child(request.performer.id);
    var messageId = ref.push().key;
    ref.child(messageId).set({
        'id' : messageId,
        'type' : 'ping',
        'time' : new Date().getTime(),
        'noxbox' : request.noxbox
    });
    deferred.resolve(request);

    return deferred.promise;
}

exports.freezeMoney = function (request) {
    var deferred = Q.defer();

    request.wallet.frozenMoney = '' + (new math.BigDecimal(request.wallet.frozenMoney)
        .add(new math.BigDecimal(request.price)));

    db.ref('profiles/' + request.id + '/wallet').update({'frozenMoney' : request.wallet.frozenMoney});

    deferred.resolve(request);

    return deferred.promise;
}

exports.unfreezeMoneyInTransaction = function (request) {
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

        current.frozenMoney = '' + (new math.BigDecimal(current.frozenMoney)
            .subtract(new math.BigDecimal(request.noxbox.price)));
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

    // firebase can process transaction up to 25 times in case of issues with multi access,
    // for the first time transaction processed with null instead of real value
    db.ref('noxboxes').child(request.noxboxType).child(request.noxbox.id).transaction(function(current) {
        if(!current) {
            return 0;
        }

        if(!current.timeAccepted) {
            request.error = 'Noxbox was not accepted';
            deferred.reject(request);
            return current;
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
            var ref = db.ref('messages').child(performerId);
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
            var ref = db.ref('messages').child(payerId);
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
        let ref = db.ref('messages').child(payerId);
        let messageId = ref.push().key;
        ref.child(messageId).set({
            'id' : messageId,
            'type' : 'pong',
            'noxbox' : request.noxbox
        });
    }

    deferred.resolve(request);
    return deferred.promise;
}

exports.generateSecret = function (request) {
    var deferred = Q.defer();

    for(payerId in request.noxbox.payers) {
        let secret = Math.random().toString(36).substring(2, 15);
        request.noxbox.payers[payerId].secret = secret;
    }

    deferred.resolve(request);
    return deferred.promise;
}

exports.notifyCompleted = function (request) {
    var deferred = Q.defer();

    for(payerId in request.noxbox.payers) {
        var ref = db.ref('messages').child(payerId);
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
        console.error(request.error);
    } else {
        console.error('Error ', request);
    }

    deferred.resolve(request);
    return deferred.promise;
}

exports.updatePayerBalanceInTransaction = function (request) {
    var deferred = Q.defer();

    // firebase can process transaction up to 25 times in case of issues with multi access,
    // for the first time transaction processed with null instead of real value
    var payerId;
    for(id in request.noxbox.payers) {
        payerId = id;
    }
    db.ref('profiles').child(payerId).child('wallet').transaction(function(current) {
        if(!current) {
            return 0;
        }

        current.balance = '' + (new math.BigDecimal(current.balance)
            .subtract(new math.BigDecimal(request.noxbox.price)));
        current.frozenMoney = '' + (new math.BigDecimal(current.frozenMoney)
            .subtract(new math.BigDecimal(request.noxbox.price)));

        request.wallet = current;
        console.log('Balance was updated', current);
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

exports.storePriceWithoutFee = function (request) {
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
    var ref = db.ref('messages').child(to);
    var messageId = ref.push().key;
    ref.child(messageId).set({
        'id' : messageId,
        'type' : 'dislike',
    });
}



