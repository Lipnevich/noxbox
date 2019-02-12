package live.noxbox.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import live.noxbox.R;

/**
 * Created by Vladislaw Kravchenok on 11.02.2019.
 */
public class CircleView extends View {

    public static final int SUPPLY_FLAG = 51;
    public static final int BOTH_FLAG = 52;
    public static final int DEMAND_FLAG = 53;

    private int flag;

    private Paint blue;
    private Paint orange;

    private boolean checked;

    private int defaultBarWidth;
    private int defaultBarHeight;

    private int width;
    private int height;

    public CircleView(Context context, int flag) {
        super(context);
        this.flag = flag;
        this.defaultBarWidth = context.getResources().getDimensionPixelSize(R.dimen.default_bar_width);
        this.defaultBarHeight = context.getResources().getDimensionPixelSize(R.dimen.default_bar_height);
        setWillNotDraw(false);
    }

    public CircleView(Context context) {
        super(context);
        this.defaultBarWidth = context.getResources().getDimensionPixelSize(R.dimen.default_bar_width);
        this.defaultBarHeight = context.getResources().getDimensionPixelSize(R.dimen.default_bar_height);
        setWillNotDraw(false);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.defaultBarWidth = context.getResources().getDimensionPixelSize(R.dimen.default_bar_width);
        this.defaultBarHeight = context.getResources().getDimensionPixelSize(R.dimen.default_bar_height);
        setWillNotDraw(false);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.defaultBarWidth = context.getResources().getDimensionPixelSize(R.dimen.default_bar_width);
        this.defaultBarHeight = context.getResources().getDimensionPixelSize(R.dimen.default_bar_height);
        setWillNotDraw(false);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.defaultBarWidth = context.getResources().getDimensionPixelSize(R.dimen.default_bar_width);
        this.defaultBarHeight = context.getResources().getDimensionPixelSize(R.dimen.default_bar_height);
        setWillNotDraw(false);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
            case MeasureSpec.AT_MOST:
                width = defaultBarWidth;
                break;
            case MeasureSpec.UNSPECIFIED:
                width = defaultBarWidth;
                break;
            default:
                width = defaultBarWidth;
        }

        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.AT_MOST:
                height = defaultBarHeight;
                break;
            case MeasureSpec.UNSPECIFIED:
                height = defaultBarHeight;
                break;
            default:
                height = defaultBarHeight;
        }

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        blue = new Paint();
        orange = new Paint();

        float centerX = width / 2;
        float centerY = height / 2;
        float radius = height / 2;

        if (flag == BOTH_FLAG) {
            if (checked) {
                blue.setColor(getResources().getColor(R.color.blueON));
                orange.setColor(getResources().getColor(R.color.orangeON));
            } else {
                blue.setColor(getResources().getColor(R.color.blueOFF));
                orange.setColor(getResources().getColor(R.color.orangeOFF));
            }

            canvas.drawCircle(width / 2, height / 2, height / 2, blue);

            RectF rectF = new RectF();
            rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);


            canvas.drawArc(rectF, 315, 90, true, orange);
            canvas.drawArc(rectF, 45, 90, true, orange);

            canvas.drawArc(rectF, 135, 90, true, blue);
            canvas.drawArc(rectF, 225, 90, true, blue);

        } else if (flag == SUPPLY_FLAG) {
            if (checked) {
                blue.setColor(getResources().getColor(R.color.blueON));
            } else {
                blue.setColor(getResources().getColor(R.color.blueOFF));
            }

            canvas.drawCircle(width / 2, height / 2, height / 2, blue);

        } else if (flag == DEMAND_FLAG) {
            if (checked) {
                orange.setColor(getResources().getColor(R.color.orangeON));
            } else {
                orange.setColor(getResources().getColor(R.color.orangeOFF));
            }

            canvas.drawCircle(width / 2, height / 2, height / 2, orange);
        }
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        invalidate();
    }
}

