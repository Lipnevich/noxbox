package live.noxbox.cluster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import live.noxbox.R;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap;
import static live.noxbox.model.MarketRole.supply;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;

public class IconGenerator {
    private static final int[] CLUSTER_ICON_BUCKETS = {10, 20, 50, 100, 500, 1000, 5000, 10000, 20000};

    private final Context context;
    private BitmapDescriptor mClusterItemIcon;

    private final SparseArray<BitmapDescriptor> clusterIcons = new SparseArray<>();
    private static Map<String, BitmapDescriptor> itemIcons = new ConcurrentHashMap<>();

    public IconGenerator(@NonNull Context context) {
        this.context = context;
    }


    @NonNull
    private BitmapDescriptor createClusterIcon(int clusterBucket) {
        @SuppressLint("InflateParams")
        TextView clusterIconView = (TextView) LayoutInflater.from(context)
                .inflate(R.layout.map_cluster_icon, null);

        int size = 85;
        if (clusterBucket >= 20000) {
            size += 70;
        } else if (clusterBucket >= 10000) {
            size += 55;
        } else if (clusterBucket >= 5000) {
            size += 50;
        } else if (clusterBucket >= 1000) {
            size += 40;
        } else if (clusterBucket >= 500) {
            size += 30;
        } else if (clusterBucket >= 100) {
            size += 20;
        } else if (clusterBucket >= 50) {
            size += 10;
        } else if (clusterBucket >= 10) {
            size += 5;
        }

        Drawable dr = context.getResources().getDrawable(R.drawable.noxbox);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable d = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, size, size, true));

        clusterIconView.setBackground(d);

        clusterIconView.setTextColor(context.getResources().getColor(R.color.secondary));

        clusterIconView.setText(getClusterIconText(clusterBucket));

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
        BitmapDescriptor clusterIcon = clusterIcons.get(clusterBucket);

        if (clusterIcon == null) {
            clusterIcon = createClusterIcon(clusterBucket);
            clusterIcons.put(clusterBucket, clusterIcon);
        }

        return clusterIcon;
    }

    public BitmapDescriptor getClusterItemIcon(@NonNull NoxboxMarker clusterItem) {
        return createClusterItemIcon(clusterItem);
    }

    @NonNull
    private BitmapDescriptor createClusterItemIcon(NoxboxMarker point) {
        if (point.getNoxbox().getRole() == supply) {
            return createItemIcon(point, R.color.colorAccent);
        } else {
            return createItemIcon(point, R.color.primary);
        }
    }

    private BitmapDescriptor createItemIcon(NoxboxMarker point, int color) {
        String key = point.getNoxbox().getType().name().concat(point.getNoxbox().getRole().name());
        if (itemIcons.containsKey(key)) {
            return itemIcons.get(key);
        }
        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                context.getResources(),
                point.getNoxbox().getType().getImage()),
                dpToPx(56),
                dpToPx(56),
                true);
        BitmapDescriptor bitmapDescriptor = fromBitmap(tintImage(bitmap, color));
        itemIcons.put(key, bitmapDescriptor);

        return bitmapDescriptor;
    }

    private static Bitmap tintImage(Bitmap bitmap, int color) {
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResult);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bitmapResult;
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
//        return (clusterIconBucket < CLUSTER_ICON_BUCKETS[0]) ?
//                String.valueOf(clusterIconBucket) : String.valueOf(clusterIconBucket) + "+";
        return String.valueOf(clusterIconBucket);
    }
}
