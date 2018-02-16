const functions = require('firebase-functions');
const Q = require('q');
const math = require("bigdecimal");

const BlockIo = require('block_io');
const block_io = new BlockIo({
  api_key: functions.config().keys.wallet_api,
  version: 2
});

exports.getAvailableMoneyWithoutFee = function (request) {
    var deferred = Q.defer();

    block_io.get_network_fee_estimate({
        'amounts': request.wallet.availableMoney,
        'from_labels': request.id,
        'priority': 'low',
        'to_addresses': request.toAddress
    }, function (error, data) {
        if(error) {
            request.availableMoneyWithoutFee = data.data.max_withdrawal_available;
            if(request.availableMoneyWithoutFee) {
                console.info('money without fee ', request.availableMoneyWithoutFee);
                return deferred.resolve(request);
            }
            request.error = error;
            return deferred.reject(request);
        } else {
            console.log('Network fee ' + data.data.estimated_network_fee);
            request.availableMoneyWithoutFee = '' + (new math.BigDecimal(request.wallet.availableMoney)
                .subtract(new math.BigDecimal(data.data.estimated_network_fee)));
            if(new math.BigDecimal(request.availableMoneyWithoutFee)
                .compareTo(new math.BigDecimal(0)) <= 0) {
                request.error = 'Fee ' + data.data.estimated_network_fee
                                                    + ' is bigger than funds ' + request.wallet.availableMoney;
                deferred.reject(request);
            }
            return deferred.resolve(request);
        }
    });

    return deferred.promise;
}

exports.sendMoney = function (request) {
    var deferred = Q.defer();

    block_io.withdraw_from_labels({
        'amounts': request.availableMoneyWithoutFee,
        'from_labels': request.id,
        'to_addresses': request.toAddress,
        'priority': 'low',
        'pin': functions.config().keys.wallet_pin
    }, function (error, data) {
        if(error) {
            request.error = error;
            deferred.reject(request);
        } else {
            console.log('Money was refund ' + request.availableMoneyWithoutFee + ' to address ' + request.toAddress);
            request.wallet.balance = '' + (new math.BigDecimal(request.wallet.balance)
                .subtract(new math.BigDecimal(request.wallet.availableMoney)));
            deferred.resolve(request);
        }
    });

    return deferred.promise;
}

exports.getPriceWithoutFee = function (request) {
    var deferred = Q.defer();

    var payerId;
    for(id in request.noxbox.payers) {
        payerId = id;
    }
    var performerId;
    for(id in request.noxbox.performers) {
        performerId = id;
    }

    block_io.get_network_fee_estimate({
        'amounts': request.noxbox.price,
        'from_labels': payerId,
        'priority': 'low',
        'to_labels': performerId
    }, function (error, data) {
        if(error) {
            request.priceWithoutFee = data.data.max_withdrawal_available;
            if(request.priceWithoutFee) {
                console.info('price without fee ', request.priceWithoutFee);
                return deferred.resolve(request);
            }
            request.error = error;
            return deferred.reject(request);
        } else {
            console.log('Network fee ' + data.data.estimated_network_fee);
            request.priceWithoutFee = '' + (new math.BigDecimal(request.noxbox.price)
                .subtract(new math.BigDecimal(data.data.estimated_network_fee)));
            if(new math.BigDecimal(request.priceWithoutFee).compareTo(new math.BigDecimal(0)) <= 0) {
                request.error = 'Calculation error price is lower then fee';
                deferred.reject(request);
            }
            return deferred.resolve(request);
        }
    });

    return deferred.promise;
}

exports.pay = function (request) {
    var deferred = Q.defer();

    // TODO (nli) pay to each performer in case there is more then 1 performer in noxbox
    var payerId;
    for(id in request.noxbox.payers) {
        payerId = id;
    }
    var performerId;
    for(id in request.noxbox.performers) {
        performerId = id;
    }

    console.log('amount ' + request.priceWithoutFee + ' from ' + payerId + ' to ' + performerId);

    block_io.withdraw_from_labels({
        'amounts': request.priceWithoutFee,
        'from_labels': payerId,
        'to_labels': performerId,
        'priority': 'low',
        'pin': functions.config().keys.wallet_pin
    }, function (error, data) {
        if(error) {
            request.error = error;
            deferred.reject(request);
        } else {
            console.log('Noxbox was payed ');
            deferred.resolve(request);
        }
    });

    return deferred.promise;
}

exports.create = function (request) {
    var deferred = Q.defer();

    block_io.get_new_address({'label': request.id}, function (error, data) {
        if(error) {
            var exists = 'Label already exists';
            if(('' + error).indexOf(exists, 0) >= 0) {
                block_io.get_address_balance({'label' : request.id}, function (error, data) {
                    if (error) {
                        request.error = error;
                        deferred.reject(request);
                    } else {
                        if(!request.wallet) request.wallet = {};
                        request.wallet.balance = data.data.balances[0].available_balance;
                        request.wallet.address = data.data.balances[0].address;
                        console.log("Existing balance is " + request.wallet.balance + " for profile " + request.id);
                        deferred.resolve(request);
                    }
                });
            } else {
                request.error = error;
                deferred.reject(request);
            }
        } else {
            request.wallet = {'balance' : '0.0', 'address' : data.data.address};
            console.log("Address " + data.data.address + " was created for new profile " + request.id);
            deferred.resolve(request);
        }
    });

    return deferred.promise;
}

exports.checkHookConfirmations = function (request) {
    if(!request.confirmations) {
        console.log('Not confirmed transaction');
        return false;
    } else if((request.is_green && (request.confirmations == 1 || request.confirmations == 2))
            || (!request.is_green && request.confirmations == 3)) {
        return true;
    }
    console.log('Already confirmed transaction');
    return false;
}

exports.getBalance = function (request) {
    var deferred = Q.defer();

    block_io.get_address_balance({'address' : request.wallet.address}, function (error, data) {
        if (error) {
            request.error = error;
            deferred.reject(request);
        } else {
            request.id = data.data.balances[0].label;
            request.wallet.balance = data.data.available_balance;
            console.log("Current balance is " + request.wallet.balance + " for profile " + request.id);
            deferred.resolve(request);
        }
    });

    return deferred.promise;
}

// common --------------------------
exports.getOwnerBalance = function () {
    var deferred = Q.defer();

    block_io.get_balance({}, function (error, data) {
        if (error) {
            request.error = error;
            deferred.reject(request);
        } else {
            console.log(data);
            deferred.resolve(data);
        }
    });

    return deferred.promise;
}

exports.getAllAddresses = function () {
    var deferred = Q.defer();

    block_io.get_my_addresses({}, function (error, data) {
        if (error) {
            request.error = error;
            deferred.reject(request);
        } else {
            deferred.resolve(data);
        }
    });

    return deferred.promise;
}
