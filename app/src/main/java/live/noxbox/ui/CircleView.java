package live.noxbox.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import live.noxbox.R;

import static live.noxbox.App.context;

public class CircleView extends View {

    public static final int SUPPLY_FLAG = 51;
    public static final int BOTH_FLAG = 52;
    public static final int DEMAND_FLAG = 53;

    private int flag;

    private Paint blue;
    private Paint orange;

    private boolean checked;

    private int defaultBarWidth = context().getResources().getDimensionPixelSize(R.dimen.default_bar_width);
    private int defaultBarHeight = context().getResources().getDimensionPixelSize(R.dimen.default_bar_height);

    private int width;
    private int height;

    public CircleView(Context context, int flag) {
        super(context);
        this.flag = flag;
        setWillNotDraw(false);
    }

    public CircleView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

        float centerX = getWidth() >> 1;
        float centerY = getHeight() >> 1;
        float radius = height >> 1;

        if (flag == BOTH_FLAG) {
            if (checked) {
                blue.setColor(context().getResources().getColor(R.color.blueON));
                orange.setColor(context().getResources().getColor(R.color.orangeON));
            } else {
                blue.setColor(context().getResources().getColor(R.color.blueOFF));
                orange.setColor(context().getResources().getColor(R.color.orangeOFF));
            }

            canvas.drawCircle(width >> 1, height >> 1, height >> 1, blue);

            RectF rectF = new RectF();
            rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);


            canvas.drawArc(rectF, 315, 90, true, orange);
            canvas.drawArc(rectF, 45, 90, true, orange);

            canvas.drawArc(rectF, 135, 90, true, blue);
            canvas.drawArc(rectF, 225, 90, true, blue);

        } else if (flag == SUPPLY_FLAG) {
            if (checked) {
                blue.setColor(context().getResources().getColor(R.color.blueON));
            } else {
                blue.setColor(context().getResources().getColor(R.color.blueOFF));
            }

            canvas.drawCircle(width >> 1, height >> 1, height >> 1, blue);

        } else if (flag == DEMAND_FLAG) {
            if (checked) {
                orange.setColor(context().getResources().getColor(R.color.orangeON));
            } else {
                orange.setColor(context().getResources().getColor(R.color.orangeOFF));
            }

            canvas.drawCircle(width >> 1, height >> 1, height >> 1, orange);
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
