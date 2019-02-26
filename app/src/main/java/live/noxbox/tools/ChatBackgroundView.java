package live.noxbox.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by Vladislaw Kravchenok on 26.02.2019.
 */
public class ChatBackgroundView extends AppCompatImageView {

    public ChatBackgroundView(Context context) {
        super(context);
    }

    public ChatBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
