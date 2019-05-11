package live.noxbox.tools;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;


public class PanoramaImageView extends AppCompatImageView {

    // Image's width and height
    private int mDrawableWidth;
    private int mDrawableHeight;

    // View's width and height
    private int mWidth;
    private int mHeight;

    // Image's offset from initial state(center in the view).
    private float mMaxOffsetX;
    private float mMaxOffsetY;

    // The scroll progress.
    private float mProgressX;
    private float mProgressY;

    public PanoramaImageView(Context context) {
        this(context, null);
    }

    public PanoramaImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanoramaImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setScaleType(ScaleType.CENTER_CROP);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, com.gjiazhe.panoramaimageview.R.styleable.PanoramaImageView);
        typedArray.recycle();
    }

    public void updateProgress(float progressX, float progressY) {
        mProgressX = progressX;
        mProgressY = progressY;
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        mHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null || isInEditMode()) {
            super.onDraw(canvas);
            return;
        }
        float currentOffsetX = 0;
        float currentOffsetY = 0;

        if (getDrawable() != null) {
            mDrawableWidth = getDrawable().getIntrinsicWidth();
            mDrawableHeight = getDrawable().getIntrinsicHeight();

            float imgScaleX = (float) mHeight / (float) mDrawableHeight;
            float imgScaleY = (float) mWidth / (float) mDrawableWidth;
            if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
                mMaxOffsetY = Math.abs((mDrawableWidth * imgScaleX - mWidth) * 0.1f);
                mMaxOffsetX = Math.abs((mDrawableHeight * imgScaleY - mHeight) * 0.5f);
                currentOffsetX = mMaxOffsetX * mProgressY;
                currentOffsetY = mMaxOffsetY * mProgressX;
            } else {
                mMaxOffsetY = Math.abs((mDrawableHeight * imgScaleY - mHeight) * 0.5f);
                mMaxOffsetX = Math.abs((mDrawableWidth * imgScaleX - mWidth) * 0.1f);
                currentOffsetX = mMaxOffsetX * mProgressX;
                currentOffsetY = mMaxOffsetY * mProgressY;
            }

        }

        canvas.save();
        canvas.translate(currentOffsetX, currentOffsetY);
        super.onDraw(canvas);
        canvas.restore();
    }


    @Override
    public void setScaleType(ScaleType scaleType) {
        /**
         * Do nothing because PanoramaImageView only
         * supports {@link scaleType.CENTER_CROP}
         */
    }

}
