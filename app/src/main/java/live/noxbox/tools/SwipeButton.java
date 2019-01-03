package live.noxbox.tools;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import live.noxbox.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class SwipeButton extends RelativeLayout {
    private ImageView slidingButton;
    private float initialX;
    private boolean active;
    private int initialButtonWidth;
    private TextView centerText;

    private Drawable disabledDrawable;
    private Drawable enabledDrawable;
    private RelativeLayout background;

    public SwipeButton(Context context) {
        super(context);

        init(null, "", context);
    }

    public SwipeButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(null, "", context);
    }

    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(null, "", context);
    }

    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(null, "", context);
    }

    public void setParametrs(Drawable customDrawable, String text, Context context) {
        this.enabledDrawable = customDrawable;
        init(customDrawable, text, getContext());
    }


    private void init(Drawable customDrawable, String text, Context context) {
        removeAllViews();
        background = new RelativeLayout(context);

        LayoutParams layoutParamsView = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                WRAP_CONTENT);

        layoutParamsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        background.setBackground(ContextCompat.getDrawable(context, R.drawable.swipe_rounded_layout));

        addView(background, layoutParamsView);

        final TextView centerText = new TextView(context);
        this.centerText = centerText;

        centerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

        LayoutParams layoutParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        centerText.setText(text);
        centerText.setTextColor(context.getResources().getColor(R.color.google_text));
        centerText.setPadding(35, 35, 35, 35);
        background.addView(centerText, layoutParams);

        final ImageView swipeButton = new ImageView(context);
        this.slidingButton = swipeButton;

        enabledDrawable = ContextCompat.getDrawable(getContext(), R.drawable.yes);
        disabledDrawable = ContextCompat.getDrawable(getContext(), R.drawable.yes);

        if (customDrawable != null) {
            enabledDrawable = customDrawable;
            disabledDrawable = customDrawable;
        }
        enabledDrawable.setTint(Color.WHITE);
        slidingButton.setImageDrawable(disabledDrawable);
        slidingButton.getLayoutParams();
        slidingButton.setPadding(35, 35, 35, 35);

        LayoutParams layoutParamsButton = new LayoutParams(
                WRAP_CONTENT,
                WRAP_CONTENT);

        layoutParamsButton.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        layoutParamsButton.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        swipeButton.requestLayout();
        swipeButton.setBackground(ContextCompat.getDrawable(context, R.drawable.swipe_button_shape));
        swipeButton.setImageDrawable(disabledDrawable);
        addView(swipeButton, layoutParamsButton);
    }

    public void setText(String text) {
        this.centerText.setText(text);
    }

    public String getText() {
        if (this.centerText.getText() != null) {
            return (String) this.centerText.getText();
        }
        return null;
    }

    public OnTouchListener getButtonTouchListener(final Task<Object> task) {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:// нажатие
                        Log.d("SwipeButton", "ACTION_DOWN");
                        if(event.getX() > slidingButton.getWidth()){
                            return false;
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:// движение
                        if (initialX == 0) {
                            initialX = slidingButton.getX();
                        }
                        if (event.getX() > initialX + slidingButton.getWidth() / 2 &&
                                event.getX() + slidingButton.getWidth() / 2 < getWidth()) {
                            slidingButton.setX(event.getX() - slidingButton.getWidth() / 2);
                            centerText.setAlpha(1 - 1.3f * (slidingButton.getX() + slidingButton.getWidth()) / getWidth());
                        }

                        if (event.getX() + slidingButton.getWidth() / 2 > getWidth() &&
                                slidingButton.getX() + slidingButton.getWidth() / 2 < getWidth()) {
                            slidingButton.setX(getWidth() - slidingButton.getWidth());
                        }

                        if (event.getX() < slidingButton.getWidth() / 2 &&
                                slidingButton.getX() > 0) {
                            slidingButton.setX(0);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:// отпускание

                        if (active) {
                            //cancel operation if need
                            //collapseButton();
                        } else {
                            initialButtonWidth = slidingButton.getWidth();
                            if (slidingButton.getX() + slidingButton.getWidth() > getWidth() * 0.85) {

                                //accept confirmation
                                task.execute(null);

                                expandButton();

                            } else {
                                moveButtonBack();
                            }
                        }


                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        return true;

                }

                return false;
            }
        };
    }

    private void expandButton() {
        final ValueAnimator positionAnimator =
                ValueAnimator.ofFloat(slidingButton.getX(), 0);
        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (Float) positionAnimator.getAnimatedValue();
                slidingButton.setX(x);
            }
        });


        final ValueAnimator widthAnimator = ValueAnimator.ofInt(
                slidingButton.getWidth(),
                getWidth());

        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams params = slidingButton.getLayoutParams();
                params.width = (Integer) widthAnimator.getAnimatedValue();
                slidingButton.setLayoutParams(params);
            }
        });


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                active = true;
                slidingButton.setImageDrawable(enabledDrawable);
            }
        });

        animatorSet.playTogether(positionAnimator, widthAnimator);
        animatorSet.start();
    }

    private void moveButtonBack() {
        final ValueAnimator positionAnimator =
                ValueAnimator.ofFloat(slidingButton.getX(), 0);
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (Float) positionAnimator.getAnimatedValue();
                slidingButton.setX(x);
            }
        });

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                centerText, "alpha", 1);

        positionAnimator.setDuration(200);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator, positionAnimator);
        animatorSet.start();
    }
}