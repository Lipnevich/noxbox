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
  'tosUrl': 'doc/rules.pdf',
  'privacyPolicyUrl': 'doc/NoxBoxPrivacyPolicy.pdf',
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

let map;
function initMap() {
  map = new google.maps.Map(document.getElementById('map'), {
    zoom: 2,
    center: new google.maps.LatLng(0, 0),
    mapTypeControlOptions: { mapTypeIds: [] },
  });

  let lightMapType = new google.maps.StyledMapType(
    [
  {
    "featureType": "administrative.land_parcel",
    "elementType": "labels",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi.business",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "labels.icon",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "road.local",
    "elementType": "labels",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "transit",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  }
  ],
  {name: 'Light Map'});

  let darkMapType = new google.maps.StyledMapType(
    [
    {
      "elementType": "geometry",
      "stylers": [
        {
          "color": "#242f3e"
        }
      ]
    },
    {
      "elementType": "labels.text.fill",
      "stylers": [
        {
          "color": "#746855"
        }
      ]
    },
    {
      "elementType": "labels.text.stroke",
      "stylers": [
        {
          "color": "#242f3e"
        }
      ]
    },
    {
      "featureType": "administrative.locality",
      "elementType": "labels.text.fill",
      "stylers": [
        {
          "color": "#d59563"
        }
      ]
    },
    {
      "featureType": "poi",
      "elementType": "labels.text",
      "stylers": [
        {
          "visibility": "off"
        }
      ]
    },
    {
      "featureType": "poi",
      "elementType": "labels.text.fill",
      "stylers": [
        {
          "color": "#d59563"
        }
      ]
    },
    {
      "featureType": "poi.business",
      "stylers": [
        {
          "visibility": "off"
        }
      ]
    },
    {
      "featureType": "poi.park",
      "elementType": "geometry",
      "stylers": [
        {
          "color": "#263c3f"
        }
      ]
    },
    {
      "featureType": "poi.park",
      "elementType": "labels.text.fill",
      "stylers": [
        {
          "color": "#6b9a76"
        }
      ]
    },
    {
      "featureType": "road",
      "elementType": "geometry",
      "stylers": [
        {
          "color": "#38414e"
        }
      ]
    },
    {
      "featureType": "road",
      "elementType": "geometry.stroke",
      "stylers": [
        {
          "color": "#212a37"
        }
      ]
    },
    {
      "featureType": "road",
      "elementType": "labels.icon",
      "stylers": [
        {
          "visibility": "off"
        }
      ]
    },
    {
      "featureType": "road",
      "elementType": "labels.text.fill",
      "stylers": [
        {
          "color": "#9ca5b3"
        }
      ]
    },
    {
      "featureType": "road.highway",
      "elementType": "geometry",
      "stylers": [
        {
          "color": "#746855"
        }
      ]
    },
    {
      "featureType": "road.highway",
      "elementType": "geometry.stroke",
      "stylers": [
        {
          "color": "#1f2835"
        }
      ]
    },
    {
      "featureType": "road.highway",
      "elementType": "labels.text.fill",
      "stylers": [
        {
          "color": "#f3d19c"
        }
      ]
    },
    {
      "featureType": "transit",
      "stylers": [
        {
          "visibility": "off"
        }
      ]
    },
    {
      "featureType": "transit",
      "elementType": "geometry",
      "stylers": [
        {
          "color": "#2f3948"
        }
      ]
    },
    {
      "featureType": "transit.station",
      "elementType": "labels.text.fill",
      "stylers": [
        {
          "color": "#d59563"
        }
      ]
    },
    {
      "featureType": "water",
      "elementType": "geometry",
      "stylers": [
        {
          "color": "#17263c"
        }
      ]
    },
    {
      "featureType": "water",
      "elementType": "labels.text.fill",
      "stylers": [
        {
          "color": "#515c6d"
        }
      ]
    },
    {
      "featureType": "water",
      "elementType": "labels.text.stroke",
      "stylers": [
        {
          "color": "#17263c"
        }
      ]
    }],
    {name: 'Dark Map'});

  if (window.matchMedia("(prefers-color-scheme: dark)").matches) {
    map.mapTypes.set('styled_map', darkMapType);
  } else {
    map.mapTypes.set('styled_map', lightMapType);
  }
  map.setMapTypeId('styled_map');

  let script = document.createElement('script');
  script.src = 'https://us-central1-noxbox-project.cloudfunctions.net/map';
  document.getElementsByTagName('head')[0].appendChild(script);
}

window.map_callback = results => {
  let markers = [];
  for (let i = 0; i < results.length; i++) {
    let latLng = new google.maps.LatLng(results[i].latitude, results[i].longitude);
    let serviceRole = results[i].key.split(';')[2];
    let serviceType = results[i].key.split(';')[3];
    let marker = new google.maps.Marker({
      position: latLng,
      map: map,
      icon: {
          url: '../img/services/ic_' + serviceType + '_' + serviceRole + '.png',
          scaledSize: new google.maps.Size(36, 36),
          origin: new google.maps.Point(0,0),
          anchor: new google.maps.Point(0, 36)
      }
    });
    markers.push(marker);
  }

  let url = 'img/logo.png';
  let clusterStyles = [
    {
      textColor: 'white',
      url: url,
      height: 48,
      width: 48,
      textSize: 36
    }
  ];

  let mcOptions = {
      gridSize: 36,
      styles: clusterStyles,
      maxZoom: 15
  };

  let markerCluster = new MarkerClusterer(map, markers, mcOptions);
}





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
