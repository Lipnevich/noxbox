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

  	request.wallet = { balance : '0',
  	                   address : seed.address,
  	                   seed : encrypted };

  	console.log('New address ' + seed.address + ' was created for profile ' + request.uid);
	deferred.resolve(request);

    return deferred.promise;
}