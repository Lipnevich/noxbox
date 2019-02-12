package live.noxbox.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import live.noxbox.R;

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
        supply.setChecked(false);
        supply.setId(View.generateViewId());
        LayoutParams supplyParams = new LayoutParams(
                dpToPx(36),
                dpToPx(36));
        supplyParams.setMargins(dpToPx(8), dpToPx(8), 0, dpToPx(8));
        background.addView(supply, supplyParams);

        both = new CircleView(context, BOTH_FLAG);
        both.setChecked(true);
        both.setId(View.generateViewId());
        LayoutParams bothParams = new LayoutParams(
                dpToPx(36),
                dpToPx(36));
        bothParams.setMargins(dpToPx(42), dpToPx(8), 0, dpToPx(8));
        bothParams.addRule(RelativeLayout.END_OF, supply.getId());
        background.addView(both, bothParams);


        demand = new CircleView(context, DEMAND_FLAG);
        demand.setChecked(false);
        demand.setId(View.generateViewId());
        LayoutParams demandParams = new LayoutParams(
                dpToPx(36),
                dpToPx(36));
        demandParams.setMargins(dpToPx(42), dpToPx(8), dpToPx(8), dpToPx(8));
        demandParams.addRule(RelativeLayout.END_OF, both.getId());
        background.addView(demand, demandParams);

        drawNameOfRole(context);

        initOnClickListeners(context);
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
            text.setText(R.string.supply);
        } else if (both.isChecked()) {
            textParams.addRule(RelativeLayout.END_OF, both.getId());
            textParams.setMargins(12, 0, 0, 0);
            text.setText(R.string.all);
        } else if (demand.isChecked()) {
            textParams.addRule(RelativeLayout.ALIGN_END, demand.getId());
            textParams.setMargins(0, 0, dpToPx(48), 0);
            text.setText(R.string.demand);
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
            }
        });

        both.setOnClickListener(v -> {
            if (!both.isChecked()) {
                both.setChecked(true);
                demand.setChecked(false);
                supply.setChecked(false);
                drawNameOfRole(context);
            }
        });


        demand.setOnClickListener(v -> {
            if (!demand.isChecked()) {
                demand.setChecked(true);
                both.setChecked(false);
                supply.setChecked(false);
                drawNameOfRole(context);
            }
        });
    }
}
