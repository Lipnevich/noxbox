const functions = require('firebase-functions');
const BigDecimal = require('big.js');
const WavesAPI = require('waves-api');

const Waves = WavesAPI.create(WavesAPI.MAINNET_CONFIG);
const password = functions.config().keys.seedpass ? functions.config().keys.seedpass : 'Salt';

const wavesDecimals = new BigDecimal('100000000');
const wavesFee = new BigDecimal('0.001');
const noxboxFee = new BigDecimal('0.1');

exports.create = async request => {
  	const seed = Waves.Seed.create();

  	request.address = seed.address;
  	request.seed = seed.encrypt(password);

  	console.log('New address ' + seed.address + ' was created for profile ' + request.uid);
  	return request;
}

exports.send = async request => {
    request.seed = Waves.Seed.fromExistingPhrase(Waves.Seed.decryptSeedPhrase(request.encrypted, password));

    let response = await Waves.API.Node.v1.addresses.balance(request.seed.address);

    if(!request.transferable) {
        if(!response || response.balance == '0') return;
        request.transferable = new BigDecimal('' + response.balance).div(wavesDecimals).minus(wavesFee);
    } else {
        if(response.balance.lt(request.transferable)) request.transferable = response.balance;
        request.transferable = request.transferable.minus(wavesFee);
    }
    console.log('Transferable ' + request.transferable);

    return transfer(request);
}

transfer = async request => {
    const transferData = {
        assetId: 'WAVES',
        feeAssetId: 'WAVES',
        fee: 100000,
        attachment: '',
        timestamp: Date.now()
    };

    transferData.recipient = request.addressToTransfer,
    transferData.amount = '' + request.transferable.times(wavesDecimals);

    await Waves.API.Node.v1.assets.transfer(transferData, request.seed.keyPair);
    console.log('Money was transferred');

}

exports.balance = async address => {
    let response = await Waves.API.Node.v1.addresses.balance(request.seed.address);
    return new BigDecimal('' + response.balance);
}
