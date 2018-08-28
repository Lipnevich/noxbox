const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const noxbox = require('./noxbox-functions');
const wallet = require('./wallet-functions');

const oldWavesAPI = require('waves-api');
const newWavesAPI = require('@waves/waves-api');

const oldWaves = oldWavesAPI.create(oldWavesAPI.MAINNET_CONFIG);
const newWaves = newWavesAPI.create(newWavesAPI.MAINNET_CONFIG);

exports.welcome = functions.auth.user().onCreate((user) => {
    return wallet.create(user).then(
           noxbox.init);
});

exports.compareWavesLibs = functions.https.onRequest((req, res) => {
    const password = '019283485';
    const oldEncrypted = oldWaves.Seed.fromExistingPhrase(oldWaves.Seed.create().phrase).encrypt(password);
    const newEncrypted = newWaves.Seed.fromExistingPhrase(newWaves.Seed.create().phrase).encrypt(password);

    let oldSeed = oldWaves.Seed.fromExistingPhrase(oldWaves.Seed.decryptSeedPhrase(oldEncrypted, password));
    let newSeed = newWaves.Seed.fromExistingPhrase(newWaves.Seed.decryptSeedPhrase(newEncrypted, password));

    res.status(200).send('Old address ' + oldSeed.address + '<br/>New address ' + newSeed.address);
});

exports.version = functions.https.onRequest((req, res) => {
    res.status(200).send('Version 0.1');
});