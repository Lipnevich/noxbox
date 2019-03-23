package live.noxbox.cluster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import live.noxbox.R;
import live.noxbox.debug.TimeLogger;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;

public class IconGenerator {

    private final Context context;
    private BitmapDrawable mClusterItemIcon;

    private static Map<String, BitmapDescriptor> itemIcons = new ConcurrentHashMap<>();

    public IconGenerator(@NonNull Context context) {
        this.context = context;
    }


    @NonNull
    private BitmapDescriptor createClusterIcon(int clusterBucket) {
        @SuppressLint("InflateParams")
        TextView clusterIconView = (TextView) LayoutInflater.from(context)
                .inflate(R.layout.map_cluster_icon, null);

        if(mClusterItemIcon == null) {
            Drawable dr = context.getResources().getDrawable(R.drawable.noxbox);
            Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
            mClusterItemIcon = new BitmapDrawable(context.getResources(),
                Bitmap.createScaledBitmap(bitmap, dpToPx(64), dpToPx(64), true));
        }

        clusterIconView.setBackground(mClusterItemIcon);

        clusterIconView.setTextColor(context.getResources().getColor(R.color.secondary));

        clusterIconView.setTextSize(36);
        clusterIconView.setText(String.valueOf(clusterBucket));

        clusterIconView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        clusterIconView.layout(0, 0, clusterIconView.getMeasuredWidth(),
                clusterIconView.getMeasuredHeight());

        Bitmap iconBitmap = Bitmap.createBitmap(clusterIconView.getMeasuredWidth(),
                clusterIconView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(iconBitmap);
        clusterIconView.draw(canvas);

        return fromBitmap(iconBitmap);
    }

    @NonNull
    public BitmapDescriptor getClusterIcon(@NonNull Cluster<NoxboxMarker> cluster) {
        int clusterBucket = getClusterIconBucket(cluster);
        TimeLogger timeLogger = new TimeLogger();
        BitmapDescriptor clusterIcon = createClusterIcon(clusterBucket);
        timeLogger.makeLog("Create cluster icon");
        return clusterIcon;
    }

    public BitmapDescriptor getClusterItemIcon(@NonNull NoxboxMarker clusterItem) {
        return createClusterItemIcon(clusterItem);
    }

    @NonNull
    private BitmapDescriptor createClusterItemIcon(NoxboxMarker point) {
        return createItemIcon(point);
    }

    private BitmapDescriptor createItemIcon(NoxboxMarker point) {
        String key = point.getNoxbox().getType().name().concat(point.getNoxbox().getRole().name());
        if (itemIcons.containsKey(key)) {
            return itemIcons.get(key);
        }


        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), point.getNoxbox().getIcon());

        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap,
                dpToPx(56),
                dpToPx(56),
                true);
        BitmapDescriptor bitmapDescriptor = fromBitmap(newBitmap);
        itemIcons.put(key, bitmapDescriptor);

        return bitmapDescriptor;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private int getClusterIconBucket(@NonNull Cluster<NoxboxMarker> cluster) {
//        int itemCount = cluster.getItems().size();
//        if (itemCount <= CLUSTER_ICON_BUCKETS[0]) {
//            return itemCount;
//        }
//
//        for (int i = 0; i < CLUSTER_ICON_BUCKETS.length - 1; i++) {
//            if (itemCount < CLUSTER_ICON_BUCKETS[i + 1]) {
//                return CLUSTER_ICON_BUCKETS[i];
//            }
//        }
        //return CLUSTER_ICON_BUCKETS[CLUSTER_ICON_BUCKETS.length - 1];
        return cluster.getItems().size();
    }

    @NonNull
    private String getClusterIconText(int clusterIconBucket) {
        return String.valueOf(clusterIconBucket);
    }
}
