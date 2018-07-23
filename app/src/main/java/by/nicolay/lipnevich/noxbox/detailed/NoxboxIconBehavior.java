package by.nicolay.lipnevich.noxbox.detailed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;

import by.nicolay.lipnevich.noxbox.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class NoxboxIconBehavior extends CoordinatorLayout.Behavior<CircleImageView> {


    private Context mContext;

    private float mCustomFinalYPosition;
    private float mCustomStartXPosition;
    private float mCustomStartToolbarPosition;
    private float mCustomStartHeight;
    private float mCustomFinalHeight;

    private float mAvatarMaxSize;
    private float mFinalLeftAvatarPadding;
    private float mStartPosition;
    private int mStartXPosition;
    private float mStartToolbarPosition;
    private int mStartYPosition;
    private int mFinalYPosition;
    private int mStartHeight;
    private int mFinalXPosition;
    private float mChangeBehaviorPoint;

    public NoxboxIconBehavior(Context context, AttributeSet attrs) {
        mContext = context;

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NoxboxIconBehavior);
            mCustomFinalYPosition = typedArray.getDimension(R.styleable.NoxboxIconBehavior_finalYPosition, 0);
            mCustomStartXPosition = typedArray.getDimension(R.styleable.NoxboxIconBehavior_startXPosition, 0);
            mCustomStartToolbarPosition = typedArray.getDimension(R.styleable.NoxboxIconBehavior_startToolbarPosition, 0);
            mCustomStartHeight = typedArray.getDimension(R.styleable.NoxboxIconBehavior_startHeight, 0);
            mCustomFinalHeight = typedArray.getDimension(R.styleable.NoxboxIconBehavior_finalHeight, 0);

            typedArray.recycle();
        }

        bindDimensions();

        mFinalLeftAvatarPadding = context.getResources().getDimension(
                R.dimen.spacing_normal);
    }



    private void bindDimensions() {
        mAvatarMaxSize = mContext.getResources().getDimension(R.dimen.image_width);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, CircleImageView child, View dependency) {
        return dependency instanceof Toolbar;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, CircleImageView child, View dependency) {
        maybeInitProperties(child, dependency);

        final int maxScrollDistance = (int) (mStartToolbarPosition);
        float expandedPercentageFactor = dependency.getY() / maxScrollDistance;

        if (expandedPercentageFactor < mChangeBehaviorPoint) {
            float heightFactor = (mChangeBehaviorPoint - expandedPercentageFactor) / mChangeBehaviorPoint;

            float distanceXToSubtract = ((mStartXPosition - mFinalXPosition)
                    * heightFactor) + (child.getHeight() / 2);
            float distanceYToSubtract = ((mStartYPosition - mFinalYPosition)
                    * (1f - expandedPercentageFactor)) + (child.getHeight() / 2);

            child.setX(mStartXPosition - distanceXToSubtract);
            child.setY(mStartYPosition - distanceYToSubtract);

            float heightToSubtract = ((mStartHeight - mCustomFinalHeight) * heightFactor);

            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            layoutParams.width = (int) (mStartHeight - heightToSubtract);
            layoutParams.height = (int) (mStartHeight - heightToSubtract);
            child.setLayoutParams(layoutParams);
        } else {
            float distanceYToSubtract = ((mStartYPosition - mFinalYPosition)
                    * (1f - expandedPercentageFactor)) + (mStartHeight / 2);

            child.setX(mStartXPosition - child.getWidth() / 2);
            child.setY(mStartYPosition - distanceYToSubtract);

            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            layoutParams.width = (int) (mStartHeight);
            layoutParams.height = (int) (mStartHeight);
            child.setLayoutParams(layoutParams);
        }
        return true;
    }

    @SuppressLint("PrivateResource")
    private void maybeInitProperties(CircleImageView child, View dependency) {
        if (mStartYPosition == 0)
            mStartYPosition = (int) (dependency.getY());

        if (mFinalYPosition == 0)
            mFinalYPosition = (dependency.getHeight() / 2);

        if (mStartHeight == 0)
            mStartHeight = child.getHeight();

        if (mStartXPosition == 0)
            mStartXPosition = (int) (child.getX() + (child.getWidth() / 2));

        if (mFinalXPosition == 0)
            mFinalXPosition = mContext.getResources().getDimensionPixelOffset(R.dimen.abc_action_bar_content_inset_material) + ((int) mCustomFinalHeight / 2);

        if (mStartToolbarPosition == 0)
            mStartToolbarPosition = dependency.getY();

        if (mChangeBehaviorPoint == 0) {
            mChangeBehaviorPoint = (child.getHeight() - mCustomFinalHeight) / (2f * (mStartYPosition - mFinalYPosition));
        }
    }

}