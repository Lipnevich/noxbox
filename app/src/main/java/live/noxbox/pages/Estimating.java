package live.noxbox.pages;

import android.app.Activity;
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
                if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        profile.getCurrent().setTimeDemandDisliked(null);
                    } else {
                        profile.getCurrent().setTimeSupplyDisliked(null);
                    }
                } else {
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        profile.getCurrent().setTimeDemandDisliked(null);
                    } else {
                        profile.getCurrent().setTimeSupplyDisliked(null);
                    }
                }
                controlDisplayRate(profile);
            }
        });

        ((ImageView) estimatingView.findViewById(R.id.dislike)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        profile.getCurrent().setTimeDemandDisliked(System.currentTimeMillis());
                    } else {
                        profile.getCurrent().setTimeSupplyDisliked(System.currentTimeMillis());
                    }
                } else {
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        profile.getCurrent().setTimeDemandDisliked(System.currentTimeMillis());
                    } else {
                        profile.getCurrent().setTimeSupplyDisliked(System.currentTimeMillis());
                    }
                }
                controlDisplayRate(profile);
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

        estimatingView.findViewById(R.id.estimatingScreenClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayHiddenViews();
                estimatingView.removeAllViews();
            }
        });
    }

    private void controlDisplayRate(final Profile profile) {
        if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
            if (profile.getCurrent().getRole() == MarketRole.supply) {
                if (profile.getCurrent().getTimeDemandDisliked() != null) {
                    ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.primary));
                    ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
                } else {
                    ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.primary));
                    ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
                }
            } else {
                if (profile.getCurrent().getTimeSupplyDisliked() != null) {
                    ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.primary));
                    ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
                } else {
                    ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.primary));
                    ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
                }
            }
        } else {
            if (profile.getCurrent().getRole() == MarketRole.supply) {
                if (profile.getCurrent().getTimeDemandDisliked() != null) {
                    ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.primary));
                    ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
                } else {
                    ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.primary));
                    ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
                }
            } else {
                if (profile.getCurrent().getTimeSupplyDisliked() != null) {
                    ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.primary));
                    ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
                } else {
                    ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.primary));
                    ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
                }
            }
        }
    }

    @Override
    public void clear() {
        displayHiddenViews();
        estimatingView.removeAllViews();
    }
    private void displayHiddenViews(){
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.floatingButton).setVisibility(View.VISIBLE);
    }
}
