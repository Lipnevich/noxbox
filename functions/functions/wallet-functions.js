const functions = require('firebase-functions');
const Q = require('q');
const math = require("bigdecimal");

const WavesAPI = require('waves-api');
const Waves = WavesAPI.create(WavesAPI.MAINNET_CONFIG);
const password = functions.config().keys.seedpass ? functions.config().keys.seedpass : 'Salt';
const decimals = new math.BigDecimal('100000000');

exports.create = function (request) {
    var deferred = Q.defer();

  	const seed = Waves.Seed.create();
  	const encrypted = seed.encrypt(password);

  	if(!request.wallet) request.wallet = {};
  	request.wallet.balance = '0';
    request.wallet.address = seed.address;
 	request.wallet.seed = encrypted;


  	console.log('New address ' + seed.address + ' was created for profile ' + request.id);
	deferred.resolve(request);

    return deferred.promise;
}

exports.getBalance = function (request) {
    var deferred = Q.defer();

    if(!request.wallet || !request.wallet.address) {
        request.wallet.balance = '0';
        request.wallet.frozenMoney = '0';
        request.wallet.availableMoney = '0';
        request.wallet.availableMoneyWithoutFee = '-0.001';
        console.log('Wallet is not created yet ' + request.wallet);
        deferred.resolve(request);
    } else {
        Waves.API.Node.v1.addresses.balance(request.wallet.address).then((balance) => {
            console.log('Response ', balance);
            request.wallet.balance = '' + new math.BigDecimal(balance.balance).divide(decimals);
            request.wallet.frozenMoney = request.wallet.frozenMoney ? request.wallet.frozenMoney : '0';
            request.wallet.availableMoney = '' + new math.BigDecimal(request.wallet.balance)
                    .subtract(new math.BigDecimal(request.wallet.frozenMoney));
            request.wallet.availableMoneyWithoutFee = '' + new math.BigDecimal(request.wallet.availableMoney)
                     .subtract(new math.BigDecimal('0.001'));
            console.log('Current balance is ' + request.wallet.balance + ' for profile '
                    + request.id + ' and wallet ' + request.wallet.address);
            deferred.resolve(request);
        });
    }

    return deferred.promise;
}

exports.refund = function (request) {
    var deferred = Q.defer();

    const restoredPhrase = Waves.Seed.decryptSeedPhrase(request.wallet.seed, password);
    const seed = Waves.Seed.fromExistingPhrase(restoredPhrase);

    var refundAmount = '' + new math.BigDecimal(request.wallet.availableMoneyWithoutFee).multiply(decimals);
    console.log('Refund ' + new math.BigDecimal(request.wallet.availableMoneyWithoutFee));

    const transferData = {
        // An arbitrary address
        recipient: request.toAddress,
        // ID of a token, or WAVES
        assetId: 'WAVES',
        // The real amount is the given number divided by 10^(precision of the token)
        amount: refundAmount,
        // The same rules for these two fields
        feeAssetId: 'WAVES',
        fee: 100000,
        // 140 bytes of data (it's allowed to use Uint8Array here)
        attachment: '',
        timestamp: Date.now()
    };

    Waves.API.Node.v1.assets.transfer(transferData, seed.keyPair).then((responseData) => {
        console.log(responseData);
        request.wallet.balance = request.wallet.frozenMoney;
        request.wallet.availableMoney = '0';
        request.wallet.availableMoneyWithoutFee = '-0.001';
        deferred.resolve(request);
    });

    return deferred.promise;
}

exports.pay = function (request) {
    var deferred = Q.defer();

    request.priceWithoutFee = '' + (new math.BigDecimal(request.noxbox.price))
            .subtract(new math.BigDecimal('0.001'));
    var priceAmount = '' + (new math.BigDecimal(request.priceWithoutFee)).multiply(decimals);
    console.log('Pay price without fee ' + request.priceWithoutFee + ' from ' + request.payer.id
                    + ' to ' + request.performer.id);

    const restoredPhrase = Waves.Seed.decryptSeedPhrase(request.payer.seed, password);
    const seed = Waves.Seed.fromExistingPhrase(restoredPhrase);

    const transferData = {
        // An arbitrary address
        recipient: request.performer.address,
        // ID of a token, or WAVES
        assetId: 'WAVES',
        // The real amount is the given number divided by 10^(precision of the token)
        amount: priceAmount,
        // The same rules for these two fields
        feeAssetId: 'WAVES',
        fee: 100000,
        // 140 bytes of data (it's allowed to use Uint8Array here)
        attachment: '',
        timestamp: Date.now()
    };

    Waves.API.Node.v1.assets.transfer(transferData, seed.keyPair).then((responseData) => {
        console.log(responseData);
        deferred.resolve(request);
    });

    return deferred.promise;
}