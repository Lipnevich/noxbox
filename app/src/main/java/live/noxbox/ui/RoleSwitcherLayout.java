package live.noxbox.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import live.noxbox.R;

import static live.noxbox.database.AppCache.profile;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;
import static live.noxbox.ui.CircleView.BOTH_FLAG;
import static live.noxbox.ui.CircleView.DEMAND_FLAG;
import static live.noxbox.ui.CircleView.SUPPLY_FLAG;

/**
 * Created by Vladislaw Kravchenok on 11.02.2019.
 */
public class RoleSwitcherLayout extends RelativeLayout {

    private RelativeLayout background;
    private CircleView supply;
    private CircleView both;
    private CircleView demand;
    private TextView text;


    public RoleSwitcherLayout(Context context) {
        super(context);
        init(context, null, -1, -1);
    }


    public RoleSwitcherLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1, -1);
    }

    public RoleSwitcherLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, -1);
    }

    public RoleSwitcherLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        background = new RelativeLayout(context);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        background.setBackground(ContextCompat.getDrawable(context, R.drawable.switcher_layout_background));
        addView(background, layoutParams);


        supply = new CircleView(context, SUPPLY_FLAG);
        both = new CircleView(context, BOTH_FLAG);
        demand = new CircleView(context, DEMAND_FLAG);

        refresh();

        supply.setId(View.generateViewId());
        both.setId(View.generateViewId());
        demand.setId(View.generateViewId());

        LayoutParams supplyParams = new LayoutParams(
                dpToPx(36),
                dpToPx(36));
        LayoutParams bothParams = new LayoutParams(
                dpToPx(36),
                dpToPx(36));
        LayoutParams demandParams = new LayoutParams(
                dpToPx(36),
                dpToPx(36));


        supplyParams.setMargins(dpToPx(8), dpToPx(8), 0, dpToPx(8));
        bothParams.setMargins(dpToPx(42), dpToPx(8), 0, dpToPx(8));
        demandParams.setMargins(dpToPx(42), dpToPx(8), dpToPx(8), dpToPx(8));


        bothParams.addRule(RelativeLayout.END_OF, supply.getId());
        demandParams.addRule(RelativeLayout.END_OF, both.getId());

        background.addView(supply, supplyParams);
        background.addView(both, bothParams);
        background.addView(demand, demandParams);

        drawNameOfRole(context);

        initOnClickListeners(context);
    }

    public void refresh() {
        if (profile().getFilters().getDemand() && profile().getFilters().getSupply()) {
            supply.setChecked(false);
            demand.setChecked(false);
            both.setChecked(true);
        } else if (profile().getFilters().getSupply()) {
            supply.setChecked(true);
            demand.setChecked(false);
            both.setChecked(false);
        } else {
            supply.setChecked(false);
            demand.setChecked(true);
            both.setChecked(false);
        }
    }

    private void drawNameOfRole(Context context) {
        if (text != null) background.removeView(text);

        LayoutParams textParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.addRule(RelativeLayout.CENTER_VERTICAL);

        text = new TextView(context);
        text.setId(View.generateViewId());
        text.setTextSize(14);

        if (supply.isChecked()) {
            textParams.addRule(RelativeLayout.END_OF, supply.getId());
            textParams.setMargins(12, 0, 0, 0);
            text.setText(R.string.supplyuppercase);
        } else if (both.isChecked()) {
            textParams.addRule(RelativeLayout.END_OF, both.getId());
            textParams.setMargins(12, 0, 0, 0);
            text.setText(R.string.all);
        } else if (demand.isChecked()) {
            textParams.addRule(RelativeLayout.ALIGN_END, demand.getId());
            textParams.setMargins(0, 0, dpToPx(48), 0);
            text.setText(R.string.demanduppercase);
        }

        background.addView(text, textParams);

    }

    private void initOnClickListeners(final Context context) {
        supply.setOnClickListener(v -> {
            if (!supply.isChecked()) {
                supply.setChecked(true);
                both.setChecked(false);
                demand.setChecked(false);
                drawNameOfRole(context);
                profile().getFilters().setSupply(true);
                profile().getFilters().setDemand(false);
            }
        });

        both.setOnClickListener(v -> {
            if (!both.isChecked()) {
                both.setChecked(true);
                demand.setChecked(false);
                supply.setChecked(false);
                drawNameOfRole(context);
                profile().getFilters().setSupply(true);
                profile().getFilters().setDemand(true);
            }
        });


        demand.setOnClickListener(v -> {
            if (!demand.isChecked()) {
                demand.setChecked(true);
                both.setChecked(false);
                supply.setChecked(false);
                drawNameOfRole(context);
                profile().getFilters().setSupply(false);
                profile().getFilters().setDemand(true);
            }
        });
    }
}
