const functions = require('firebase-functions');
const Q = require('q');
const BigDecimal = require('big.js');

const WavesAPI = require('waves-api');
const Waves = WavesAPI.create(WavesAPI.MAINNET_CONFIG);
const password = functions.config().keys.seedpass ? functions.config().keys.seedpass : 'Salt';
const decimals = new BigDecimal('100000000');
const blockchainTransactionFee = new BigDecimal('0.001');
const rewardAmount = new BigDecimal('0.010');

exports.create = function (request) {
    var deferred = Q.defer();

  	const seed = Waves.Seed.create();
  	const encrypted = seed.encrypt(password);

  	if(!request.wallet) request.wallet = {};
  	request.wallet.balance = '0';
    request.wallet.address = seed.address;
 	request.wallet.seed = encrypted;

  	console.log('New address ' + seed.address + ' was created for profile ' + request.uid);
	deferred.resolve(request);

    return deferred.promise;
}

exports.getBalance = function (request) {
    var deferred = Q.defer();

    if(!request.wallet.address) {
        request.wallet.balance = '0';
        request.wallet.frozenMoney = '0';
        request.wallet.availableMoney = '0';
        request.wallet.availableMoneyWithoutFee = '0';
        console.log('Wallet is not created yet ' + request.wallet);

        deferred.resolve(request);
    } else {
        Waves.API.Node.v1.addresses.balance(request.wallet.address).then((balance) => {
            console.log('Response ', balance);

            request.wallet.balance = '' + new BigDecimal(balance.balance).div(decimals);
            request.wallet.frozenMoney = request.wallet.frozenMoney ? request.wallet.frozenMoney : '0';
            request.wallet.availableMoney = '' + new BigDecimal(request.wallet.balance)
                    .minus(new BigDecimal(request.wallet.frozenMoney));
            request.wallet.availableMoneyWithoutFee = '' + new BigDecimal(request.wallet.availableMoney)
                     .minus(blockchainTransactionFee);
            console.log('Current money available is ' + request.wallet.availableMoney + ' for profile '
                    + request.id + ' and wallet ' + request.wallet.address);

            deferred.resolve(request);
        }, (error) => {
                 request.error = error.message;
                 deferred.reject(request);
        });
    }

    return deferred.promise;
}

exports.refund = function (request) {
    console.log('Refund ' + request.wallet.availableMoneyWithoutFee);

    request.transfer = {};
    request.transfer.payerId = request.id;
    request.transfer.from = request.wallet.seed;
    request.transfer.to = request.toAddress;
    request.transfer.amount = request.wallet.availableMoneyWithoutFee;

    return transfer(request);
}

exports.tryToReward = function (request) {
    console.log('Reward ' + rewardAmount);

    request.transfer = {};
    request.transfer.payerId = request.reward.payerId;
    request.transfer.from = request.reward.seed;
    request.transfer.receiverId = request.uid;
    request.transfer.to = request.wallet.address;
    request.transfer.amount = '' + rewardAmount;
    request.transfer.optional = true;

    return transfer(request);
}

exports.pay = function (request) {
    request.priceWithoutFee = '' + (new BigDecimal(request.noxbox.price))
            .minus(blockchainTransactionFee);

    console.log('Pay price without fee ' + request.priceWithoutFee + ' from ' + request.payer.id
                    + ' to ' + request.performer.id);

    request.transfer = {};
    request.transfer.payerId = request.payer.id;
    request.transfer.from = request.payer.seed;
    request.transfer.receiverId = request.performer.id;
    request.transfer.to = request.performer.address;
    request.transfer.amount = request.priceWithoutFee;
    request.transfer.unfreeze = true;

    return transfer(request);
}

transfer = function (request) {
    var deferred = Q.defer();

    if(!request.transfer.from && request.transfer.optional) {
        deferred.resolve(request);
    } else {
        const amountInDecimals = '' + new BigDecimal(request.transfer.amount).times(decimals);
        const transferData = {
            // An arbitrary address
            recipient: request.transfer.to,
            // ID of a token, or WAVES
            assetId: 'WAVES',
            // The real amount is the given number divided by 10^(precision of the token)
            amount: amountInDecimals,
            // The same rules for these two fields
            feeAssetId: 'WAVES',
            fee: 100000,
            // 140 bytes of data (it's allowed to use Uint8Array here)
            attachment: '',
            timestamp: Date.now()
        };

        const restoredPhrase = Waves.Seed.decryptSeedPhrase(request.transfer.from, password);
        const seed = Waves.Seed.fromExistingPhrase(restoredPhrase);

        Waves.API.Node.v1.assets.transfer(transferData, seed.keyPair).then((responseData) => {
            console.log(responseData);
            deferred.resolve(request);
        }, (error) => {
            if(request.transfer.optional) {
                console.log(error.message);
                deferred.resolve(request);
            } else {
                request.error = error.message;
                deferred.reject(request);
            }
        });
    }

    return deferred.promise;
}
