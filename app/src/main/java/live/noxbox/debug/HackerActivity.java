package live.noxbox.debug;

import live.noxbox.menu.MenuActivity;

public class HackerActivity extends MenuActivity{

//    private List<NotificationType> photoPushes = Arrays.asList(NotificationType.values());
//    private Iterator<NotificationType> iterator = photoPushes.iterator();
//    private Profile party = new Profile()
//            .setId("12321")
//            .setWallet(new Wallet().setBalance("1000000"))
//            .setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399))
//            .setTravelMode(TravelMode.bicycling)
//            .setHost(true)
//            .setName("Granny Smith")
//            .setPhoto(NoxboxExamples.PHOTO_MOCK);
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (true || !BuildConfig.DEBUG) return;
//
//        AppCache.listenProfile(HackerActivity.class.getName(), i -> {
//            HackerActivity.this.findViewById(R.id.debugNotify).setVisibility(View.GONE);
//            HackerActivity.this.findViewById(R.id.debugGenerateNoxboxes).setVisibility(View.GONE);
//            HackerActivity.this.findViewById(R.id.debugRequest).setVisibility(View.GONE);
//            HackerActivity.this.findViewById(R.id.debugAccept).setVisibility(View.GONE);
//            HackerActivity.this.findViewById(R.id.debugPhotoVerify).setVisibility(View.GONE);
//            HackerActivity.this.findViewById(R.id.debugPhotoReject).setVisibility(View.GONE);
//            HackerActivity.this.findViewById(R.id.debugComplete).setVisibility(View.GONE);
//
//            NoxboxState currentState = NoxboxState.getState(profile().getCurrent(), profile());
//            switch (currentState) {
//                case initial:
//                    HackerActivity.this.findViewById(R.id.debugNotify).setVisibility(View.VISIBLE);
//                    setOnClickListener(R.id.debugNotify, o -> {
//                        Map<String, String> data = new HashMap<>();
//                        if (!iterator.hasNext()) iterator = photoPushes.iterator();
//                        data.put("type", iterator.next().name());
//                        data.put("progress", "" + 50);
//                        data.put("price", "" + 555);
//                        data.put("timeAccepted", "" + System.currentTimeMillis());
//                        data.put("time", "" + System.currentTimeMillis());
//                        data.put("name", "Long Long Long Party Name");
//                        data.put("noxboxType", NoxboxType.photographer.name());
//                        data.put("message", "Let me speak from my heart");
//                        data.put("id", "0pEHvCumSPbOCFSLFWIA");
//                        NotificationFactory.buildNotification(HackerActivity.this, AppCache.profile(), data).show();
//                    });
//
//                    findViewById(R.id.debugGenerateNoxboxes).setVisibility(View.VISIBLE);
//                    setOnClickListener(R.id.debugGenerateNoxboxes, o -> {
//                        FirebaseFirestore db = FirebaseFirestore.getInstance();
//                        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().build());
//
//                        db.collection("profiles").document(profile().getId()).set(Firestore.objectToMap(profile()), SetOptions.merge())
//                            .addOnCompleteListener(task -> {
//                                if (task.getException() != null) {
//                                    popup(getApplicationContext(), task.getException().getMessage());
//                                } else {
//                                    popup(getApplicationContext(), "GOOD JOB");
//                                }
//                        });
//                    });
//                    break;
//                case created:
//                    HackerActivity.this.findViewById(R.id.debugRequest).setVisibility(View.VISIBLE);
//                    setOnClickListener(R.id.debugRequest, o -> {
//                        profile().getCurrent().setTimeRequested(System.currentTimeMillis());
//                        profile().getCurrent().setParty(party.setNoxboxId(profile().getCurrent().getId()));
//                        AppCache.updateNoxbox();
//                    });
//                    break;
//                case requesting:
//                    HackerActivity.this.findViewById(R.id.debugAccept).setVisibility(View.VISIBLE);
//                    setOnClickListener(R.id.debugAccept, o -> {
//                        profile().getCurrent().getOwner().setPhoto(NoxboxExamples.PHOTO_MOCK);
//                        profile().getCurrent().getOwner().setId("" + ThreadLocalRandom.current().nextInt(100000));
//                        profile().getCurrent().getOwner().setName("Моя бабушка курит трубку");
//                        profile().getCurrent().setTimeAccepted(System.currentTimeMillis());
//                        AppCache.updateNoxbox();
//                    });
//                    break;
//                case accepting:
//                    break;
//                case moving:
//                    if (isNullOrZero(profile().getCurrent().getTimeOwnerVerified()) &&
//                            isNullOrZero(profile().getCurrent().getTimePartyVerified()))
//                        break;
//
//                    HackerActivity.this.findViewById(R.id.debugPhotoReject).setVisibility(View.VISIBLE);
//                    setOnClickListener(R.id.debugPhotoReject, o -> {
//                        if (profile().equals(profile().getCurrent().getOwner())) {
//                            profile().getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
//                        } else {
//                            profile().getCurrent().setTimeCanceledByOwner(System.currentTimeMillis());
//                            profile().getCurrent().setTimeRatingUpdated((System.currentTimeMillis()));
//                        }
//                        AppCache.updateNoxbox();
//                    });
//
//                    HackerActivity.this.findViewById(R.id.debugPhotoVerify).setVisibility(View.VISIBLE);
//                    setOnClickListener(R.id.debugPhotoVerify, o -> {
//                        if (profile().equals(profile().getCurrent().getOwner())) {
//                            profile().getCurrent().setTimePartyVerified(System.currentTimeMillis());
//                        } else {
//                            profile().getCurrent().setTimeOwnerVerified(System.currentTimeMillis());
//                        }
//                        AppCache.updateNoxbox();
//                    });
//                    break;
//                case performing:
//                    HackerActivity.this.findViewById(R.id.debugComplete).setVisibility(View.VISIBLE);
//                    setOnClickListener(R.id.debugComplete, o -> {
//                        profile().getCurrent().setTimeCompleted(System.currentTimeMillis());
//                        AppCache.updateNoxbox();
//                    });
//                }
//            });
//    }
//
//    private void setOnClickListener(int button, final Task<Void> task) {
//        findViewById(button).setOnLongClickListener(v -> {
//            task.execute(null);
//            return true;
//        });
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        AppCache.stopListen(HackerActivity.class.getName());
//    }
}
