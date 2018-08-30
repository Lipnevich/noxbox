package live.noxbox.pages;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;

import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.PathFinder;

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
                DebugMessage.popup(activity,"DRAW PATH WITH PADDING");
                PathFinder.createRequestPoints(profile.getCurrent(), googleMap, activity);
            }
        });

        ((FloatingActionButton) activity.findViewById(R.id.floatingButton)).setImageResource(R.drawable.eye);
        ((FloatingActionButton) activity.findViewById(R.id.floatingButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Glide.with(activity).asDrawable().load(profile.getPhoto()).into((ImageView) activity.findViewById(R.id.photoScreenImage));
                activity.findViewById(R.id.photoScreen).setVisibility(View.VISIBLE);

                activity.findViewById(R.id.photoScreenClose).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.findViewById(R.id.photoScreen).setVisibility(View.GONE);

                    }
                });
                SwipeButton swipeButton = activity.findViewById(R.id.swipeButton);
                swipeButton.setOnTouchListener(swipeButton.getButtonTouchListener());
            }
        });
        activity.findViewById(R.id.chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.getCurrent().setTimeAccepted(null);
                profile.getCurrent().setTimeRequested(null);
                ProfileStorage.fireProfile();
            }
        });

        PathFinder.createRequestPoints(profile.getCurrent(), googleMap, activity);
    }

    @Override
    public void clear() {
        ((FloatingActionButton) activity.findViewById(R.id.floatingButton)).setVisibility(View.GONE);
        ((FloatingActionButton) activity.findViewById(R.id.floatingButton)).setImageResource(R.drawable.add);
        activity.findViewById(R.id.timeLayout).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.chat).setVisibility(View.GONE);
        activity.findViewById(R.id.pathButton).setVisibility(View.GONE);

    }
}
