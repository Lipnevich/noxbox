package live.noxbox.cluster;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

class ClusterTextView extends TextView {

    public ClusterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        //noinspection SuspiciousNameCombination
        setMeasuredDimension(measuredWidth, measuredWidth);
    }
}
