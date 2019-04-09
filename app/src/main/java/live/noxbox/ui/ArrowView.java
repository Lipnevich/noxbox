package live.noxbox.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import live.noxbox.R;

import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;

/**
 * Created by Vladislaw Kravchenok on 03.04.2019.
 */
public class ArrowView extends RelativeLayout {
    private final static double ARROW_ANGLE = Math.PI / 6;
    private final static double ARROW_SIZE = 50;

    public static final int TOP_START = 1;
    public static final int TOP_CENTER = 2;
    public static final int TOP_END = 3;
    public static final int CENTER_START = 4;
    public static final int CENTER_END = 5;
    public static final int BOTTOM_START = 6;
    public static final int BOTTOM_CENTER = 7;
    public static final int BOTTOM_END = 8;

    private View startView;
    private View endView;

    private int startPointFlag;
    private int endPointFlag;

    //[0]: X
    //[1]: Y
    float[] startPoint = new float[2];
    float[] endPoint = new float[2];

    int curveRadius = 35;

    private Paint paint;

    public ArrowView(Context context, TextView startView, View endView) {
        super(context);
        this.startView = startView;
        this.endView = endView;
        init();
    }

    public ArrowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        setAttrs(context, attrs);
    }

    public ArrowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        setAttrs(context, attrs);
    }

    public ArrowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        setAttrs(context, attrs);
    }

    private void init() {


        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStrokeMiter(8);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dpToPx(1));
    }

    private void setAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ArrowView,
                0, 0);
        try {
            startView = findViewById(a.getInt(R.styleable.ArrowView_start_view, 0));
            endView = findViewById(a.getInt(R.styleable.ArrowView_end_view, 0));
        } finally {
            a.recycle();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (startView == null && endView == null)
            return;
        definePoints();

        if (startPoint[0] < endPoint[0] && startPoint[1] < endPoint[1]) {
            drawCurveAbove(startPoint[0], startPoint[1], endPoint[0], endPoint[1], curveRadius, canvas);
        } else if (startPoint[0] > endPoint[0] && startPoint[1] > endPoint[1]) {
            drawCurveAbove(startPoint[0], startPoint[1], endPoint[0], endPoint[1], curveRadius, canvas);
        } else if (startPoint[0] > endPoint[0] && startPoint[1] < endPoint[1]) {
            drawCurveBelow(startPoint[0], startPoint[1], endPoint[0], endPoint[1], curveRadius, canvas);
        } else if (startPoint[0] < endPoint[0] && startPoint[1] > endPoint[1]) {
            drawCurveBelow(startPoint[0], startPoint[1], endPoint[0], endPoint[1], curveRadius, canvas);
        }

        drawTriangleAtTheEnd(startPoint[0], startPoint[1], endPoint[0], endPoint[1], canvas);

    }

    private void drawTriangleAtTheEnd(float startX, float startY, float endX, float endY, Canvas canvas) {
        Paint fillPaintForTriangle = new Paint();
        fillPaintForTriangle.setColor(Color.WHITE);
        fillPaintForTriangle.setStyle(Paint.Style.FILL_AND_STROKE);
        fillPaintForTriangle.setAntiAlias(true);

        float[] middleAnglePoint = getMiddleAnglePoints(startX, startY, endX, endY);
        //float[] trianglePoint = getTrianglePoint(middleAnglePoint[0], middleAnglePoint[1], endX, endY); //fix point at the end of arrow, but bring another problem
        float[] trianglePoint = new float[]{endX,endY};

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        path.moveTo(trianglePoint[0], trianglePoint[1]);

        path.lineTo(trianglePoint[0] - 20, trianglePoint[1] + 30);
        path.lineTo(trianglePoint[0] + 20, trianglePoint[1] + 30);
        path.lineTo(trianglePoint[0], trianglePoint[1]);
        path.close();

        float[] points = getMiddleAnglePoints(startX, startY, endX, endY);

        float angleH = getAngleOfPath(points[0], points[1], endX, endY);
        Matrix mMatrix = new Matrix();
        //mMatrix.postRotate(angleH, endX, endY);
        mMatrix.setRotate(angleH, trianglePoint[0], trianglePoint[1]);
        path.transform(mMatrix);
        canvas.drawPath(path, fillPaintForTriangle);
    }

    float extension;

    private float[] getTrianglePoint(float midX, float midY, float endX, float endY) {
        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            extension = 500;
        } else {
            extension = 100;
        }
        float x = 0;
        float y = 0;
        float diffX = midX > endX ? midX - endX : endX - midX;
        float diffY = midY > endY ? midY - endY : endY - midY;

        float maxValue = Math.max(diffX, diffY);

        float xPercent = diffX * 100 / maxValue;
        float yPercent = diffY * 100 / maxValue;
        float generalPercent = Math.max(xPercent, yPercent);

        if (midX > endX) {
            x = endX - (extension / yPercent);
        } else {
            x = endX + (extension / yPercent);
        }

        if (midY > endY) {
            y = endY - (extension / xPercent);
        } else {
            y = endY + (extension / xPercent);
        }

        return new float[]{x, y};
    }

    private float[] getMiddleAnglePoints(float startX, float startY, float endX, float endY) {
        float pointX;
        float pointY;
        float midX = startX + ((endX - startX) / 2);
        float midY = startY + ((endY - startY) / 2);
        float xDiff = midX - startX;
        float yDiff = midY - startY;
        float angle = (float) (Math.atan2(yDiff, xDiff) * (180 / Math.PI)) - 90;
        float angleRadians = (float) Math.toRadians(angle);
        if ((startPoint[0] < endPoint[0] && startPoint[1] < endPoint[1])
                || startPoint[0] > endPoint[0] && startPoint[1] > endPoint[1]) {
            pointX = (float) (midX + 30 * Math.cos(angleRadians));
            pointY = (float) (midY + 30 * Math.sin(angleRadians));
        } else {
            pointX = (float) (midX - 30 * Math.cos(angleRadians));
            pointY = (float) (midY - 30 * Math.sin(angleRadians));
        }
        return new float[]{pointX, pointY};
    }

    //angle from one point to another
    private float getAngleOfPath(float startX, float startY, float endX, float endY) {
        float midX = startX + ((endX - startX) / 2);
        float midY = startY + ((endY - startY) / 2);
        float xDiff = midX - startX;
        float yDiff = midY - startY;
        float angle = (float) (Math.atan2(yDiff, xDiff) * (180 / Math.PI)) - 270;
        return angle;
    }

    public void drawCurveAbove(float startX, float startY, float endX, float endY, int curveRadius, Canvas canvas) {

        final Path path = new Path();
        float midX = startX + ((endX - startX) / 2);
        float midY = startY + ((endY - startY) / 2);
        float xDiff = midX - startX;
        float yDiff = midY - startY;
        double angle = (Math.atan2(yDiff, xDiff) * (180 / Math.PI)) - 90;
        double angleRadians = Math.toRadians(angle);
        float pointX = (float) (midX + curveRadius * Math.cos(angleRadians));
        float pointY = (float) (midY + curveRadius * Math.sin(angleRadians));

        path.moveTo(startX, startY);
        path.cubicTo(startX, startY, pointX, pointY, endX, endY);

        canvas.drawPath(path, paint);

    }

    public void drawCurveBelow(float startX, float startY, float endX, float endY, int curveRadius, Canvas canvas) {
        final Path path = new Path();
        float midX = startX + ((endX - startX) / 2);
        float midY = startY + ((endY - startY) / 2);
        float xDiff = midX - startX;
        float yDiff = midY - startY;
        double angle = (Math.atan2(yDiff, xDiff) * (180 / Math.PI)) - 90;
        double angleRadians = Math.toRadians(angle);
        float pointX = (float) (midX - curveRadius * Math.cos(angleRadians));
        float pointY = (float) (midY - curveRadius * Math.sin(angleRadians));

        path.moveTo(startX, startY);
        path.cubicTo(startX, startY, pointX, pointY, endX, endY);
        canvas.drawPath(path, paint);
    }


    public View getStartView() {
        return startView;
    }

    public ArrowView setStartView(View startView) {
        this.startView = startView;
        return this;
    }

    public View getEndView() {
        return endView;
    }

    public ArrowView setEndView(View endView) {
        this.endView = endView;
        return this;
    }


    private float[] getBottomRightPointOfView(View view) {
        float x = view.getX() + view.getWidth();
        float y = view.getY() + view.getHeight();
        return new float[]{x, y};
    }

    private float[] getBottomLeftPointOfView(View view) {
        float x = view.getX();
        float y = view.getY() + view.getHeight();
        return new float[]{x, y};
    }

    private float[] getTopRightPointOfView(View view) {
        float x = view.getX() + view.getWidth();
        float y = view.getY();
        return new float[]{x, y};
    }

    private float[] getTopLeftPointOfView(View view) {
        float x = view.getX();
        float y = view.getY();
        return new float[]{x, y};
    }

    private float[] getTopCenterPointOfView(View view) {
        float x = view.getX() + (view.getWidth() / 2);
        float y = view.getY();
        return new float[]{x, y};
    }

    private float[] getStartCenterPointOfView(View view) {
        float x = view.getX();
        float y = view.getY() + (view.getHeight() / 2);
        return new float[]{x, y};
    }

    private float[] getEndCenterPointOfView(View view) {
        float x = view.getX() + view.getWidth();
        float y = view.getY() + (view.getHeight() / 2);
        return new float[]{x, y};
    }

    private float[] getBottomCenterPointOfView(View view) {
        float x = view.getX() + (view.getWidth() / 2);
        float y = view.getY() + view.getHeight();
        return new float[]{x, y};
    }

    private void definePoints() {
        switch (startPointFlag) {
            case TOP_START:
                startPoint = getTopLeftPointOfView(startView);
                break;
            case TOP_CENTER:
                startPoint = getTopCenterPointOfView(startView);
                break;
            case TOP_END:
                startPoint = getTopRightPointOfView(startView);
                break;
            case CENTER_START:
                startPoint = getStartCenterPointOfView(startView);
                break;
            case CENTER_END:
                startPoint = getEndCenterPointOfView(startView);
                break;
            case BOTTOM_START:
                startPoint = getBottomLeftPointOfView(startView);
                break;
            case BOTTOM_CENTER:
                startPoint = getBottomCenterPointOfView(startView);
                break;
            case BOTTOM_END:
                startPoint = getBottomRightPointOfView(startView);
                break;
        }

        switch (endPointFlag) {
            case TOP_START:
                endPoint = getTopLeftPointOfView(endView);
                break;
            case TOP_CENTER:
                endPoint = getTopCenterPointOfView(endView);
                break;
            case TOP_END:
                endPoint = getTopRightPointOfView(endView);
                break;
            case CENTER_START:
                endPoint = getStartCenterPointOfView(endView);
                break;
            case CENTER_END:
                endPoint = getEndCenterPointOfView(endView);
                break;
            case BOTTOM_START:
                endPoint = getBottomLeftPointOfView(endView);
                break;
            case BOTTOM_CENTER:
                endPoint = getBottomCenterPointOfView(endView);
                break;
            case BOTTOM_END:
                endPoint = getBottomRightPointOfView(endView);
                break;
        }
    }


    public void invalidate(int startPoint, int endPoint) {
        startPointFlag = startPoint;
        endPointFlag = endPoint;
        invalidate();
    }
}
