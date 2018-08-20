package live.noxbox.tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;


public class ImageManager {

    public static void createCircleImageFromUrl(Activity activity, String url, ImageView image){
        Glide.with(activity)
                .asDrawable()
                .load(url)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .apply(RequestOptions.circleCropTransform())
                .into(image)
        ;
    }

    public static void createCircleImageFromBitmap(Activity activity, Bitmap bitmap, ImageView image){
        Glide.with(activity)
                .asDrawable()
                .load(bitmap)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .apply(RequestOptions.circleCropTransform())
                .into(image);
    }
}
