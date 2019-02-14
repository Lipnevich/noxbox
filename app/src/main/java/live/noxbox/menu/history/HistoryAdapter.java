package live.noxbox.menu.history;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import live.noxbox.R;
import live.noxbox.database.Firestore;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.tools.Task;

import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.DateTimeFormatter.date;
import static live.noxbox.tools.DateTimeFormatter.time;

/**
 * Created by nicolay.lipnevich on 22/06/2017.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private HistoryActivity activity;
    private String profileId;
    private List<Noxbox> historyItems;

    private Set<Noxbox> uniqueValue = new HashSet<>();
    private int lastVisibleItem, totalItemCount;
    private static final int AMOUNT_PER_LOAD = 10;


    public HistoryAdapter(HistoryActivity activity, final List<Noxbox> historyItems, RecyclerView currentRecyclerView, final MarketRole role) {
        this.activity = activity;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        this.profileId = user == null ? "" : user.getUid();
        this.historyItems = historyItems;

        long startFrom = historyItems.isEmpty() ?
                Long.MAX_VALUE :
                historyItems.get(historyItems.size() - 1).getTimeCompleted();

        final Task<Collection<Noxbox>> uploadingTask = items -> {
            List<Noxbox> noxboxes = new ArrayList<>();
            for (Noxbox noxbox : items) {
                if (!uniqueValue.contains(noxbox)) {
                    uniqueValue.add(noxbox);
                    noxboxes.add(noxbox);
                }
            }


            if (noxboxes.size() > 0) {
                historyItems.addAll(noxboxes);
                notifyDataSetChanged();
            }
        };
        Firestore.readHistory(startFrom, AMOUNT_PER_LOAD, role, uploadingTask);

        if (currentRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) currentRecyclerView.getLayoutManager();
            currentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (totalItemCount <= (lastVisibleItem + AMOUNT_PER_LOAD)) {

                        long startFrom = historyItems.isEmpty() ?
                                Long.MAX_VALUE :
                                historyItems.get(historyItems.size() - 1).getTimeCompleted();
                        Firestore.readHistory(startFrom, AMOUNT_PER_LOAD, role, uploadingTask);
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return historyItems.get(position) != null ? 1 : 0;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HistoryViewHolder viewHolder;
        if (viewType == 1) {
            viewHolder = new ItemViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_history, parent, false));
        } else {
            viewHolder = new ProgressViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_progress, parent, false));
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final HistoryViewHolder viewHolder, int position) {
        if (viewHolder instanceof ProgressViewHolder) {
            viewHolder.progressBar.setIndeterminate(true);
            return;
        }

        Noxbox noxbox = historyItems.get(position);

        viewHolder.time.setText(time(noxbox.getTimeCompleted()));
        viewHolder.date.setText(date(noxbox.getTimeCompleted()) + ",");

        viewHolder.performerName.setText(noxbox.getNotMe(profileId).getName());
        viewHolder.noxboxType.setText(noxbox.getType().getName());

        viewHolder.price.setText(noxbox.getTotal());

        Glide.with(activity)
                .load(noxbox.getNotMe(profileId).getPhoto())
                .apply(RequestOptions.circleCropTransform())
                .into(viewHolder.performerPhoto);

        viewHolder.rootHistoryLayout.setOnClickListener(view1 -> onClick(viewHolder, noxbox));
    }


    private LinearLayout showedExpandableLayout;

    private void onClick(HistoryAdapter.HistoryViewHolder viewHolder, Noxbox noxbox) {
        if (showedExpandableLayout != null && showedExpandableLayout.equals(viewHolder.expandableMapLayout)) {
            showedExpandableLayout.setVisibility(View.GONE);
            showedExpandableLayout = null;
            return;
        }
        if (showedExpandableLayout != null) {
            showedExpandableLayout.setVisibility(View.GONE);
        }
        showedExpandableLayout = viewHolder.expandableMapLayout;
        viewHolder.expandableMapLayout.setVisibility(View.VISIBLE);
        attachMapView(viewHolder.mapView, noxbox);
        attachRating(viewHolder.rateNoxbox, isLiked(noxbox, profileId));
    }


    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rootHistoryLayout;
        //ItemViewHolder
        TextView date;
        TextView time;
        TextView noxboxType;
        TextView price;
        ImageView performerPhoto;
        TextView performerName;
        LinearLayout expandableMapLayout;
        MapView mapView;
        ImageView rateNoxbox;

        //ProgressViewHolder
        ProgressBar progressBar;

        public HistoryViewHolder(@NonNull View layout) {
            super(layout);
        }
    }

    class ItemViewHolder extends HistoryViewHolder {

        public ItemViewHolder(@NonNull View layout) {
            super(layout);
            rootHistoryLayout = layout.findViewById(R.id.rootHistoryLayout);
            date = layout.findViewById(R.id.dateText);
            time = layout.findViewById(R.id.timeText);
            price = layout.findViewById(R.id.priceText);
            noxboxType = layout.findViewById(R.id.noxboxType);
            performerPhoto = layout.findViewById(R.id.performerImage);
            performerName = layout.findViewById(R.id.performerName);
            expandableMapLayout = layout.findViewById(R.id.expandableMapLayout);
            mapView = layout.findViewById(R.id.map);
            rateNoxbox = layout.findViewById(R.id.rateBox);

        }
    }

    class ProgressViewHolder extends HistoryViewHolder {


        public ProgressViewHolder(@NonNull View layout) {
            super(layout);
            progressBar = layout.findViewById(R.id.progressBar);
        }
    }

    private void attachMapView(MapView mapView, final Noxbox noxbox) {
        mapView.onCreate(null);
        mapView.onResume();
        mapView.getMapAsync(googleMap -> {
            MapsInitializer.initialize(activity);
            GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(noxbox.getType().getImage()))
                    .position(noxbox.getPosition().toLatLng(), 48, 48)
                    .anchor(0.5f, 1)
                    .zIndex(1000);
            GroundOverlay marker = googleMap.addGroundOverlay(newarkMap);
            marker.setDimensions(960, 960);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noxbox.getPosition().toLatLng(), 11));

            googleMap.getUiSettings().setAllGesturesEnabled(false);
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);
        });
    }

    private void attachRating(ImageView view, boolean isLiked) {
        if (isLiked) {
            view.setImageResource(R.drawable.like);
        } else {
            view.setImageResource(R.drawable.dislike);
        }
    }

    private boolean isLiked(Noxbox noxbox, String profileId) {
        if (profileId.equals(noxbox.getOwner().getId())) {
            return isNullOrZero(noxbox.getTimeOwnerDisliked());
        } else {
            return isNullOrZero(noxbox.getTimePartyDisliked());
        }
    }


}
