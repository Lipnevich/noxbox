const authConfig = {
  'signInFlow': 'popup',
  'credentialHelper': firebaseui.auth.CredentialHelper.ACCOUNT_CHOOSER_COM,
  'signInOptions': [
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
      provider: firebase.auth.PhoneAuthProvider.PROVIDER_ID,
      recaptchaParameters: {
        size: 'invisible'
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
  'privacyPolicyUrl': 'doc/privacyPolicy.pdf',
  'callbacks': {
    // Called when the user has been successfully signed in.
    'signInSuccessWithAuthResult': function(authResult, redirectUrl) {
      if (authResult.user) {
        handleSignedInUser(authResult.user);
      }
      return false;
    },
  },
};

const database = firebase.database();
const firestore = firebase.firestore();

let profile;

/**
 * Displays the UI for a signed in user.
 * @param {!firebase.User} user
 */
function handleSignedInUser (user) {
  // TODO (nli) show menu instead of logout button
  document.getElementById('popup').style.display = 'none';
  document.getElementById('loginBtn').style.display = 'none';
  document.getElementById('logoutBtn').style.display = 'block';
  document.getElementById('logoutIcon').src = user.photoURL;

  firestore.collection("profiles").doc(user.uid)
    .onSnapshot(doc => {
      profile = doc.data();
  });
};

/**
 * Displays the UI for a signed out user.
 */
function handleSignedOutUser () {
  document.getElementById('loginBtn').style.display = 'block';
  document.getElementById('logoutBtn').style.display = 'none';
};

let map;
function initMap() {
  map = new google.maps.Map(document.getElementById('map'), {
    zoom: 2.7,
    center: new google.maps.LatLng(24, 0),
    disableDefaultUI: true
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

  window.matchMedia("(prefers-color-scheme: dark)").matches ?
    map.mapTypes.set('styled_map', darkMapType) :
    map.mapTypes.set('styled_map', lightMapType);
  map.setMapTypeId('styled_map');

  database.ref('geo').once('value', services => showServices(services));
}

function showServices(services) {
  let markers = [];
  services.forEach(service => {
    let values = service.key.split(';');
    if(values.length !== 12) return;

    let latLng = new google.maps.LatLng(service.val().l[0], service.val().l[1]);
    let noxbox = parseService(values);

    let marker = new google.maps.Marker({
      position: latLng,
      map: map,
      icon: {
          url: 'img/services/' + noxbox.type + '_' + noxbox.role + '.png',
          scaledSize: new google.maps.Size(36, 36),
          origin: new google.maps.Point(0,0),
          anchor: new google.maps.Point(0, 36)
      }
    });
    marker.addListener('click', () =>
      showNoxboxDetails(noxbox, service.val().l[0], service.val().l[1]));
    markers.push(marker);
  });

  let clusterStyles = [
    {
      textColor: 'white',
      url: 'img/logo.png',
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

function parseService(values) {
  let service = {};
  let index = 0;
  service.id = values[index++];
  service.owner = {id : values[index++]};
  service.role = values[index++];
  service.type = values[index++];
  service.price = values[index++].replace(",", ".");
  service.owner.travelMode = values[index++];
  service.owner.host = values[index++];
  service.owner.filters = { allowNovices : values[index++] };
  let rating = { receivedLikes : Math.max(0, values[index++]),
                    receivedDislikes : Math.max(0, values[index++]) };

  service.owner.ratings = { suppliesRating : {}, demandRating : {}};
  service.role === 'supply' ? service.owner.ratings.suppliesRating[service.type] = rating :
    service.owner.ratings.demandRating[service.type] = rating;

  return service;
}

function showNoxboxDetails(noxbox, lat, lng) {
  document.getElementById('authProviders').style.display = 'none';
  document.getElementById('popupContent').style.backgroundImage =
    "url('img/services/" + noxbox.type + ".svg')";
  document.getElementById('popupContent').style.width = '90%';
  document.getElementById('popupContent').style.height = '90%';

  // TODO set address, price, rating, sertificates
  document.getElementById('serviceDetails').style.display = 'block';
  document.getElementById('popup').style.display = 'block';
}

/**
 * Initializes the app.
 */
function initApp () {
  // TODO (nli) filter button and add button
  document.getElementById('logoutBtn').onclick = () => firebase.auth().signOut();

  let popup = document.getElementById('popup');
  document.getElementById('loginBtn').onclick = () => {
    document.getElementById('authProviders').style.display = 'flex';
    document.getElementById('serviceDetails').style.display = 'none';
    let contentStyle = document.getElementById('popupContent').style;
    contentStyle.backgroundImage = '';
    contentStyle.backgroundColor = window.matchMedia("(prefers-color-scheme: dark)").matches ?
      '#242F3E' : 'white';
    contentStyle.width = '10vmin';
    contentStyle.height = '10vmin';

    popup.style.display = 'block';
    ui.start('#authProviders', authConfig);
  };
  document.getElementById("closePopup").onclick = () => popup.style.display = 'none';
  window.onclick = event => event.target == popup ? popup.style.display = 'none' : '';
};

// Initialize the FirebaseUI Widget using Firebase.
const ui = new firebaseui.auth.AuthUI(firebase.auth());
// Disable auto-sign in.
ui.disableAutoSignIn();

// Listen to change in auth state so it displays the correct UI for when
// the user is signed in or not.
firebase.auth().onAuthStateChanged(function(user) {
  user ? handleSignedInUser(user) : handleSignedOutUser();
});

window.addEventListener('load', initApp);
