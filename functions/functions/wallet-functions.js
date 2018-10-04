const functions = require('firebase-functions');
const BigDecimal = require('big.js');
const WavesAPI = require('@waves/waves-api');

const Waves = WavesAPI.create(WavesAPI.MAINNET_CONFIG);
const password = functions.config().keys.seedpass ? functions.config().keys.seedpass : 'Salt';

const wavesDecimals = new BigDecimal('100000000');
const wavesFee = new BigDecimal('0.001');
const noxboxFee = new BigDecimal('0.1');

exports.create = async function (request) {
  	const seed = Waves.Seed.create();

  	request.address = seed.address;
  	request.seed = seed.encrypt(password);

  	console.log('New address ' + seed.address + ' was created for profile ' + request.uid);
  	return request;
}