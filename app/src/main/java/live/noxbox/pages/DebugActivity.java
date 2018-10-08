package live.noxbox.pages;

import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import live.noxbox.BuildConfig;
import live.noxbox.R;
import live.noxbox.menu.MenuActivity;
import live.noxbox.model.Comment;
import live.noxbox.model.Filters;
import live.noxbox.model.ImageType;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Portfolio;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.Rating;
import live.noxbox.model.TravelMode;
import live.noxbox.model.Wallet;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.Task;

public class DebugActivity extends MenuActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if (true || BuildConfig.DEBUG) {
            ProfileStorage.readProfile(new Task<Profile>() {
                @Override
                public void execute(final Profile profile) {
                    profile.setPhoto("https://i.ytimg.com/vi/ZjZqWUp2Y90/maxresdefault.jpg");
                    Rating rating = new Rating();
                    rating.setReceivedLikes(ThreadLocalRandom.current().nextInt(900, 1000));
                    rating.setReceivedDislikes(ThreadLocalRandom.current().nextInt(rating.getReceivedLikes() / 10));
                    rating.getComments().put("0", new Comment("0", "Очень занятный молодой человек, и годный напарник!", System.currentTimeMillis(), true));
                    rating.getComments().put("1", new Comment("1", "Добротный паренёк!", System.currentTimeMillis(), true));
                    rating.getComments().put("2", new Comment("2", "Выносливость бы повысить, слишком быстро выдыхается во время кросса.", System.currentTimeMillis(), false));

                    Map<String, Rating> ratingList = new HashMap<>();
                    Map<String, Boolean> filterTypesList = new HashMap<>();
                    for (NoxboxType type : NoxboxType.values()) {
                        filterTypesList.put(type.name(), true);
                        ratingList.put(type.name(),rating);
                    }

                    List<String> certificatesList = new ArrayList<>();
                    certificatesList.add("https://i.pinimg.com/736x/1d/ba/a1/1dbaa1fb5b2f64e54010cf6aae72b8b1.jpg");
                    certificatesList.add("http://4u-professional.com/assets/images/sert/gel-lak.jpg");
                    certificatesList.add("https://www.hallyuuk.com/wp-content/uploads/2018/06/reiki-master-certificate-template-inspirational-reiki-certificate-templates-idealstalist-of-reiki-master-certificate-template.jpg");
                    certificatesList.add("http://www.childminder.ng/blog_pics/1479134810.jpg");

                    List<String> workSampleList = new ArrayList<>();
                    workSampleList.add("http://coolmanicure.com/media/k2/items/cache/stilnyy_manikur_so_strazami_XL.jpg");
                    workSampleList.add("http://rosdesign.com/design_materials3/img_materials3/kopf/kopf1.jpg");
                    workSampleList.add("http://vmirevolos.ru/wp-content/uploads/2015/12/61.jpg");

                    Map<String, List<String>> images = new HashMap<>();
                    images.put(ImageType.samples.name(), new ArrayList<String>(workSampleList));
                    images.put(ImageType.certificates.name(), new ArrayList<String>(certificatesList));


                    Map<String, Portfolio> portfolioMap = new HashMap<>();
                    portfolioMap.put(NoxboxType.haircut.name(), new Portfolio(new HashMap<String, List<String>>(images)));
                    portfolioMap.put(NoxboxType.manicure.name(), new Portfolio(new HashMap<String, List<String>>(images)));

                    profile.setHost(true)
                            .setWallet(new Wallet().setBalance("555").setAddress("3PA1KvFfq9VuJjg45p2ytGgaNjrgnLSgf4r"))
                            .setDemandsRating(ratingList)
                            .setSuppliesRating(ratingList)
                            .setFilters(new Filters(true, true, "0", filterTypesList))
                            .setTravelMode(TravelMode.driving)
                            .setPortfolio(portfolioMap);

                    DebugActivity.this.findViewById(R.id.debugLayout).setVisibility(View.VISIBLE);

//                    findViewById(R.id.debugGenerateNoxboxes).setVisibility(View.VISIBLE);
//                    findViewById(R.id.debugGenerateNoxboxes).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            for (Noxbox noxbox : NoxboxExamples.generateNoxboxes(new Position().setLongitude(27.569018).setLatitude(53.871399), 150, profile)) {
//                                // TODO (nli) создать AvailableServicesStorage, добавить туда слушателя, добавить ноксбоксы и зажечь
////                                createMarker(profile, noxbox);
//                            }
//                        }
//                    });

                    findViewById(R.id.debugRequest).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null && profile.getCurrent().getOwner().getId().equals(profile.getId())
                                    && profile.getCurrent().getTimeCreated() != null
                                    && profile.getCurrent().getTimeRequested() == null) {

                                profile.getCurrent().setParty(new Profile().setWallet(new Wallet().setBalance("20")).setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399)).setTravelMode(TravelMode.driving).setHost(false).setName("Granny Smith").setId("12321").setPhoto("http://fit4brain.com/wp-content/uploads/2014/06/zelda.jpg"));
                                // TODO (vl) сгенерировать коменты, сертификаты, примеры работ
                                profile.getCurrent().setTimeRequested(System.currentTimeMillis());
                                ProfileStorage.fireProfile();
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to request");
                            }

                        }
                    });

                    findViewById(R.id.debugAccept).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null && !profile.getCurrent().getOwner().getId().equals(profile.getId())
                                    && profile.getCurrent().getTimeCreated() != null
                                    && profile.getCurrent().getTimeRequested() != null
                                    && profile.getCurrent().getTimeAccepted() == null) {
                                profile.getCurrent().getOwner().setPhoto("http://fit4brain.com/wp-content/uploads/2014/06/zelda.jpg");
                                profile.getCurrent().setTimeAccepted(System.currentTimeMillis());
                                ProfileStorage.fireProfile();
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to accept");
                            }
                        }
                    });

                    findViewById(R.id.debugPhotoReject).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null
                                    && profile.getCurrent().getTimeCreated() != null
                                    && profile.getCurrent().getTimeRequested() != null
                                    && profile.getCurrent().getTimeAccepted() != null
                                    && profile.getCurrent().getTimeCompleted() == null) {
                                if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                                    profile.getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
                                } else {
                                    profile.getCurrent().setTimeCanceledByOwner(System.currentTimeMillis());
                                }
                                profile.getCurrent().clean();
                                ProfileStorage.fireProfile();

                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to reject");
                            }
                        }
                    });

                    findViewById(R.id.debugPhotoVerify).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null
                                    && profile.getCurrent().getTimeCreated() != null
                                    && profile.getCurrent().getTimeRequested() != null
                                    && profile.getCurrent().getTimeAccepted() != null
                                    && profile.getCurrent().getTimeCompleted() == null) {
                                if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                                    profile.getCurrent().setTimePartyVerified(System.currentTimeMillis());
                                } else {
                                    profile.getCurrent().setTimeOwnerVerified(System.currentTimeMillis());
                                }
                                if (profile.getCurrent().getTimeOwnerVerified() != null &&
                                        profile.getCurrent().getTimePartyVerified() != null) {
                                    profile.getCurrent().setTimeStartPerforming(System.currentTimeMillis());
                                }

                                ProfileStorage.fireProfile();
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to verify");
                            }

                        }
                    });
                    findViewById(R.id.debugComplete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null
                                    && profile.getCurrent().getTimeCreated() != null
                                    && profile.getCurrent().getTimeRequested() != null
                                    && profile.getCurrent().getTimeAccepted() != null
                                    && profile.getCurrent().getTimeCompleted() == null) {
                                profile.getCurrent().setTimeCompleted(System.currentTimeMillis());
                                profile.getCurrent().clean();
                                ProfileStorage.fireProfile();
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to complete");
                            }
                        }
                    });
                }
            });
        }


    }

}
