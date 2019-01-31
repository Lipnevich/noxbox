package live.noxbox.states;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.model.Comment;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Profile;
import live.noxbox.model.Rating;

import static live.noxbox.model.Noxbox.isNullOrZero;

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
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        estimatingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_estimating, null);
        estimatingView.addView(child);

        ((TextView) estimatingView.findViewById(R.id.finalSum)).setText(profile.getCurrent().getPrice() + " " + activity.getString(R.string.currency));

        estimatingView.findViewById(R.id.like).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlDisplayRate(profile);
            }
        });

        estimatingView.findViewById(R.id.dislike).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                    profile.getCurrent().setTimePartyDisliked(System.currentTimeMillis());
                } else {
                    profile.getCurrent().setTimeOwnerDisliked(System.currentTimeMillis());
                }
                AppCache.updateNoxbox();
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


        if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
            if (profile.getCurrent().getRole() == MarketRole.supply) {
                if (profile.getCurrent().getParty().getDemandsRating().size() != 0) {
                    estimatingView.findViewById(R.id.successfullyLayout).setVisibility(View.VISIBLE);
                    estimatingView.findViewById(R.id.commentLayout).setVisibility(View.INVISIBLE);
                }
            } else {
                if (profile.getCurrent().getParty().getSuppliesRating().size() != 0) {
                    estimatingView.findViewById(R.id.successfullyLayout).setVisibility(View.VISIBLE);
                    estimatingView.findViewById(R.id.commentLayout).setVisibility(View.INVISIBLE);
                }
            }
        } else {
            if (profile.getCurrent().getRole() == MarketRole.supply) {
                if (profile.getCurrent().getOwner().getDemandsRating().size() != 0) {
                    estimatingView.findViewById(R.id.successfullyLayout).setVisibility(View.VISIBLE);
                    estimatingView.findViewById(R.id.commentLayout).setVisibility(View.INVISIBLE);
                }
            } else {
                if (profile.getCurrent().getOwner().getSuppliesRating().size() != 0) {
                    estimatingView.findViewById(R.id.successfullyLayout).setVisibility(View.VISIBLE);
                    estimatingView.findViewById(R.id.commentLayout).setVisibility(View.INVISIBLE);
                }
            }
        }
        estimatingView.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getCurrent().getOwner() == profile.getCurrent().getMe(profile.getId())) {
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        profile.getCurrent()
                                .getParty()
                                .getDemandsRating().put(profile.getCurrent().getType().name(), new Rating().setComments(new HashMap<String, Comment>() {{
                            put(profile.getId(), new Comment().setText(comment).setTime(System.currentTimeMillis()));
                        }}));
                        profile.getCurrent().setCommentForDemand(comment);
                        AppCache.updateNoxbox();
                    } else {
                        profile.getCurrent()
                                .getParty()
                                .getSuppliesRating().put(profile.getCurrent().getType().name(), new Rating().setComments(new HashMap<String, Comment>() {{
                            put(profile.getId(), new Comment().setText(comment).setTime(System.currentTimeMillis()));
                        }}));
                        profile.getCurrent().setCommentForSupply(comment);
                        AppCache.updateNoxbox();
                    }
                } else {
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        profile.getCurrent()
                                .getOwner()
                                .getDemandsRating().put(profile.getCurrent().getType().name(), new Rating().setComments(new HashMap<String, Comment>() {{
                            put(profile.getId(), new Comment().setText(comment).setTime(System.currentTimeMillis()));
                        }}));
                        profile.getCurrent().setCommentForDemand(comment);
                        AppCache.updateNoxbox();
                    } else {
                        profile.getCurrent()
                                .getOwner()
                                .getSuppliesRating().put(profile.getCurrent().getType().name(), new Rating().setComments(new HashMap<String, Comment>() {{
                            put(profile.getId(), new Comment().setText(comment).setTime(System.currentTimeMillis()));
                        }}));
                        profile.getCurrent().setCommentForSupply(comment);
                        AppCache.updateNoxbox();
                    }
                }
                estimatingView.findViewById(R.id.successfullyLayout).setVisibility(View.VISIBLE);
                estimatingView.findViewById(R.id.commentLayout).setVisibility(View.INVISIBLE);

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
            if (!isNullOrZero(profile.getCurrent().getTimePartyDisliked())) {
                ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.primary));
                ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
            } else {
                ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.primary));
                ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
            }
        } else {
            if (!isNullOrZero(profile.getCurrent().getTimeOwnerDisliked())) {
                ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.primary));
                ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
            } else {
                ((ImageView) estimatingView.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.primary));
                ((ImageView) estimatingView.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
            }
        }
    }


    @Override
    public void clear() {
        displayHiddenViews();
        estimatingView.removeAllViews();
    }


    private void displayHiddenViews() {
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
    }
}
