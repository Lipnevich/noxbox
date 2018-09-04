package live.noxbox.pages;

import android.app.Activity;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import live.noxbox.R;
import live.noxbox.model.Comment;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Profile;
import live.noxbox.state.State;

public class Estimating implements State {

    private Activity activity;
    private String comment;
    private LinearLayout estimatingView;

    public Estimating(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void draw(final Profile profile) {
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        activity.findViewById(R.id.floatingButton).setVisibility(View.GONE);
        estimatingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_estimating, null);
        estimatingView.addView(child);

        ((TextView) estimatingView.findViewById(R.id.finalSum)).setText(profile.getCurrent().getPrice() + activity.getResources().getString(R.string.currency));

        ((ImageView) estimatingView.findViewById(R.id.like)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).getReceivedLikes() == 0) {
                    ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(Color.GREEN);
                    ((ImageView) estimatingView.findViewById(R.id.dislike)).setEnabled(false);
                    profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).setReceivedLikes(1);
                } else {
                    ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
                    ((ImageView) estimatingView.findViewById(R.id.dislike)).setEnabled(true);
                    profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).setReceivedLikes(0);
                }

            }
        });

        ((ImageView) estimatingView.findViewById(R.id.dislike)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).getReceivedDislikes() == 0) {
                    ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(Color.RED);
                    ((ImageView) estimatingView.findViewById(R.id.like)).setEnabled(false);
                    profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).setReceivedDislikes(1);
                    profile.getDarkList().put(profile.getCurrent().getOwner().getId(), true);
                } else {
                    ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
                    ((ImageView) estimatingView.findViewById(R.id.like)).setEnabled(true);
                    profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).setReceivedDislikes(0);
                    profile.getDarkList().put(profile.getCurrent().getOwner().getId(), false);
                }
            }
        });

        ((EditText) estimatingView.findViewById(R.id.editComment)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                comment = s.toString();
                if (s.length() > 0) {
                    estimatingView.findViewById(R.id.send).setEnabled(true);
                    estimatingView.findViewById(R.id.send).setBackground(activity.getDrawable(R.drawable.button_corner));
                } else {
                    estimatingView.findViewById(R.id.send).setEnabled(false);
                    estimatingView.findViewById(R.id.send).setBackground(activity.getDrawable(R.drawable.button_corner_disabled));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                comment = s.toString();
            }
        });
        estimatingView.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getCurrent().getOwner() == profile.getCurrent().getMe(profile.getId())) {
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        profile.getCurrent().getParty().getDemandsRating().get(profile.getCurrent().getType().name()).getComments().put(profile.getCurrent().getId(), new Comment().setText(comment).setTime(System.currentTimeMillis()));
                        profile.getCurrent().setCommentForDemand(comment);
                    } else {
                        profile.getCurrent().getParty().getSuppliesRating().get(profile.getCurrent().getType().name()).getComments().put(profile.getCurrent().getId(), new Comment().setText(comment).setTime(System.currentTimeMillis()));
                        profile.getCurrent().setCommentForSupply(comment);
                    }
                } else {
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        profile.getCurrent().getOwner().getDemandsRating().get(profile.getCurrent().getType().name()).getComments().put(profile.getCurrent().getId(), new Comment().setText(comment).setTime(System.currentTimeMillis()));
                        profile.getCurrent().setCommentForDemand(comment);
                    } else {
                        profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).getComments().put(profile.getCurrent().getId(), new Comment().setText(comment).setTime(System.currentTimeMillis()));
                        profile.getCurrent().setCommentForSupply(comment);
                    }
                }
            }
        });

    }

    @Override
    public void clear() {
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.floatingButton).setVisibility(View.VISIBLE);
        estimatingView.removeAllViews();
    }
}
