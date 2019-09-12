let authConfig = {
  'signInFlow': 'popup',
  'credentialHelper': firebaseui.auth.CredentialHelper.ACCOUNT_CHOOSER_COM,
  'signInOptions': [
    {
      provider: firebase.auth.PhoneAuthProvider.PROVIDER_ID,
      recaptchaParameters: {
        size: 'invisible'
      }
    },
    {
      provider: firebase.auth.GoogleAuthProvider.PROVIDER_ID,
      clientId: CLIENT_ID,
      authMethod: 'https://accounts.google.com',
      customParameters: {
        // Forces account selection even when one account is available.
        prompt: 'select_account'
      }
    },
    {
      provider: firebase.auth.FacebookAuthProvider.PROVIDER_ID,
      scopes :[
        'public_profile',
        'email'
      ]
    },
    firebase.auth.TwitterAuthProvider.PROVIDER_ID,
  ],
  'tosUrl': document.getElementById('rulesLink').href,
  'privacyPolicyUrl': document.getElementById('privacyPolicyLink').href,
  'callbacks': {
    // Called when the user has been successfully signed in.
    'signInSuccessWithAuthResult': function(authResult, redirectUrl) {
      if (authResult.user) {
        // TODO (nli) close auth popup here
        handleSignedInUser(authResult.user);
      }
      return false;
    },
    'uiShown': function() {
      document.getElementById('auth-providers-loading').style.display = 'none';
    },
  },
};

/**
 * Displays the UI for a signed in user.
 * @param {!firebase.User} user
 */
function handleSignedInUser (user) {
  // TODO (nli) show menu instead of logout button
  document.getElementById('loginPopup').style.display = 'none';
  document.getElementById('loginBtn').style.display = 'none';
  document.getElementById('logoutBtn').style.display = 'block';
  document.getElementById('logoutIcon').src = user.photoURL;


  // document.getElementById('user-info').textContent = user.displayName + ' ' + user.email;
};

/**
 * Displays the UI for a signed out user.
 */
function handleSignedOutUser () {
  // TODO (nli) show auth ui in separate popup after click on auth button
  document.getElementById('loginBtn').style.display = 'block';
  document.getElementById('logoutBtn').style.display = 'none';
};

/**
 * Initializes the app.
 */
function initApp () {
  // TODO (nli) init map, read and draw available services on map, sign-in button, filter button and add button
  document.getElementById('logoutBtn').onclick = () => firebase.auth().signOut();

  let loginPopup = document.getElementById('loginPopup');
  document.getElementById('loginBtn').onclick = () => {
    loginPopup.style.display = 'block';
    ui.start('#auth-providers', authConfig);
  };
  document.getElementById("closeLoginPopup").onclick = () => loginPopup.style.display = 'none';
  window.onclick = event => event.target == loginPopup ? loginPopup.style.display = 'none' : '';
};

// Initialize the FirebaseUI Widget using Firebase.
let ui = new firebaseui.auth.AuthUI(firebase.auth());
// Disable auto-sign in.
ui.disableAutoSignIn();

// Listen to change in auth state so it displays the correct UI for when
// the user is signed in or not.
firebase.auth().onAuthStateChanged(function(user) {
  user ? handleSignedInUser(user) : handleSignedOutUser();
});

window.addEventListener('load', initApp);
