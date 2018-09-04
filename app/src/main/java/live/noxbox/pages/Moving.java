package live.noxbox.pages;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;

import live.noxbox.R;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.PathFinder;
import live.noxbox.tools.Task;

import static live.noxbox.MapActivity.dpToPx;

public class Moving implements State {

    private GoogleMap googleMap;
    private Activity activity;

    public Moving(GoogleMap googleMap, Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
    }

    @Override
    public void draw(final Profile profile) {
        activity.findViewById(R.id.floatingButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.timeLayout).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        activity.findViewById(R.id.chat).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.pathButton).setVisibility(View.VISIBLE);


        activity.findViewById(R.id.pathButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugMessage.popup(activity, "way and points");
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder().include(profile.getCurrent().getParty().getPosition().toLatLng()).include(profile.getCurrent().getPosition().toLatLng()).build(), dpToPx(68)));
            }
        });

        ((FloatingActionButton) activity.findViewById(R.id.floatingButton)).setImageResource(R.drawable.eye);
        ((FloatingActionButton) activity.findViewById(R.id.floatingButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getCurrent().getMe(profile.getId()).equals(profile.getCurrent().getOwner())) {
                    Glide.with(activity).asDrawable().load(profile.getCurrent().getParty().getPhoto()).into((ImageView) activity.findViewById(R.id.photoScreenImage));
                } else {
                    Glide.with(activity).asDrawable().load(profile.getCurrent().getOwner().getPhoto()).into((ImageView) activity.findViewById(R.id.photoScreenImage));
                }
                activity.findViewById(R.id.photoScreen).setVisibility(View.VISIBLE);

                activity.findViewById(R.id.photoScreenClose).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.findViewById(R.id.photoScreen).setVisibility(View.GONE);
                    }
                });
                final SwipeButton buttonConformity = activity.findViewById(R.id.swipeButtonConformity);
                buttonConformity.setText(activity.getString(R.string.notLikeThat));
                buttonConformity.setEnabledDrawable(activity.getDrawable(R.drawable.no), activity);
                buttonConformity.setOnTouchListener(buttonConformity.getButtonTouchListener(new Task<Object>() {
                    @Override
                    public void execute(Object object) {
                        if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                            if (profile.getCurrent().getRole() == MarketRole.supply) {
                                profile.getCurrent().setTimeCanceledBySupplier(System.currentTimeMillis());
                            } else {
                                profile.getCurrent().setTimeCanceledByDemander(System.currentTimeMillis());
                            }
                        } else {
                            if (profile.getCurrent().getRole() == MarketRole.supply) {
                                profile.getCurrent().setTimeCanceledBySupplier(System.currentTimeMillis());
                            } else {
                                profile.getCurrent().setTimeCanceledByDemander(System.currentTimeMillis());
                            }
                        }
                        googleMap.clear();
                        activity.findViewById(R.id.photoScreen).setVisibility(View.GONE);
                        profile.setCurrent(ProfileStorage.noxbox());
                        ProfileStorage.fireProfile();
                    }
                }));

                final SwipeButton buttonConfirm = activity.findViewById(R.id.swipeButtonConfirm);
                buttonConfirm.setText(activity.getResources().getString(R.string.confirm));
                buttonConfirm.setEnabledDrawable(activity.getDrawable(R.drawable.yes),activity);
                buttonConfirm.setOnTouchListener(buttonConfirm.getButtonTouchListener(new Task<Object>() {
                    @Override
                    public void execute(Object object) {
                        if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                            if (profile.getCurrent().getRole() == MarketRole.supply) {
                                profile.getCurrent().setTimeSupplyVerified(System.currentTimeMillis());
                            } else {
                                profile.getCurrent().setTimeDemandVerified(System.currentTimeMillis());
                            }
                        } else {
                            if (profile.getCurrent().getRole() == MarketRole.supply) {
                                profile.getCurrent().setTimeSupplyVerified(System.currentTimeMillis());
                            } else {
                                profile.getCurrent().setTimeDemandVerified(System.currentTimeMillis());
                            }
                        }
                        //TODO (vl) simulate accepting for both participants
                        profile.getCurrent().setTimeSupplyVerified(System.currentTimeMillis());
                        profile.getCurrent().setTimeDemandVerified(System.currentTimeMillis());
                        if (profile.getCurrent().getTimeDemandVerified() != null && profile.getCurrent().getTimeSupplyVerified() != null) {
                            profile.getCurrent().setTimeStartPerforming(System.currentTimeMillis());
                        }
                        buttonConformity.setVisibility(View.GONE);
                        ProfileStorage.fireProfile();
                    }
                }));


            }
        });
        activity.findViewById(R.id.chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO (vl) on click opens chat activity
                activity.startActivity(new Intent(activity, ChatActivity.class));
            }
        });

        PathFinder.createRequestPoints(profile.getCurrent(), googleMap, activity);
    }


    @Override
    public void clear() {
        googleMap.clear();
        ((FloatingActionButton) activity.findViewById(R.id.floatingButton)).setVisibility(View.GONE);
        ((FloatingActionButton) activity.findViewById(R.id.floatingButton)).setImageResource(R.drawable.add);
        activity.findViewById(R.id.timeLayout).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.chat).setVisibility(View.GONE);
        activity.findViewById(R.id.pathButton).setVisibility(View.GONE);
        if (activity.findViewById(R.id.photoScreen).getVisibility() == View.VISIBLE) {
            activity.findViewById(R.id.photoScreen).setVisibility(View.GONE);
        }

    }
}
