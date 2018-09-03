package live.noxbox.pages;

import android.app.Activity;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import live.noxbox.R;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Profile;
import live.noxbox.state.State;

public class Estimating implements State {

    private Activity activity;
    private String comment;

    public Estimating(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void draw(final Profile profile) {
        activity.findViewById(R.id.estimatingScreen).setVisibility(View.VISIBLE);
        ((TextView) activity.findViewById(R.id.finalSum)).setText(profile.getCurrent().getPrice() + activity.getResources().getString(R.string.currency));

        ((ImageView) activity.findViewById(R.id.like)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).getReceivedLikes() == 0) {
                    ((ImageView) activity.findViewById(R.id.like)).setColorFilter(Color.GREEN);
                    ((ImageView) activity.findViewById(R.id.dislike)).setEnabled(false);
                    profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).setReceivedLikes(1);
                } else {
                    ((ImageView) activity.findViewById(R.id.like)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
                    ((ImageView) activity.findViewById(R.id.dislike)).setEnabled(true);
                    profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).setReceivedLikes(0);
                }

            }
        });

        ((ImageView) activity.findViewById(R.id.dislike)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).getReceivedDislikes() == 0) {
                    ((ImageView) activity.findViewById(R.id.dislike)).setColorFilter(Color.RED);
                    ((ImageView) activity.findViewById(R.id.like)).setEnabled(false);
                    profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).setReceivedDislikes(1);
                    profile.getDarkList().put(profile.getCurrent().getOwner().getId(), true);
                } else {
                    ((ImageView) activity.findViewById(R.id.dislike)).setColorFilter(activity.getResources().getColor(R.color.text_color_secondary));
                    ((ImageView) activity.findViewById(R.id.like)).setEnabled(true);
                    profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).setReceivedDislikes(0);
                    profile.getDarkList().put(profile.getCurrent().getOwner().getId(), false);
                }
            }
        });

        ((EditText) activity.findViewById(R.id.editComment)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                comment = s.toString();
                if (s.length() > 0) {
                    activity.findViewById(R.id.send).setEnabled(true);
                    activity.findViewById(R.id.send).setBackgroundColor(activity.getResources().getColor(R.color.primary));
                    ((Button) activity.findViewById(R.id.send)).setTextColor(activity.getResources().getColor(R.color.secondary));
                } else {
                    activity.findViewById(R.id.send).setEnabled(false);
                    activity.findViewById(R.id.send).setBackgroundColor(activity.getResources().getColor(R.color.translucent));
                    ((Button) activity.findViewById(R.id.send)).setTextColor(activity.getResources().getColor(R.color.text_color_secondary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                comment = s.toString();
            }
        });
        activity.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getCurrent().getOwner() == profile.getCurrent().getMe(profile.getId())) {
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        //profile.getCurrent().getParty().getDemandsRating().get(profile.getCurrent().getType().name()).getComments().put(profile.getCurrent())
                    } else {
                        //profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).getComments().put(profile.getCurrent())
                    }
                } else {
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        //profile.getCurrent().getOwner().getDemandsRating().get(profile.getCurrent().getType().name()).getComments().put(profile.getCurrent())
                    } else {
                        //profile.getCurrent().getOwner().getSuppliesRating().get(profile.getCurrent().getType().name()).getComments().put(profile.getCurrent())
                    }
                }
            }
        });

    }

    @Override
    public void clear() {
        activity.findViewById(R.id.estimatingScreen).setVisibility(View.GONE);
    }
}
