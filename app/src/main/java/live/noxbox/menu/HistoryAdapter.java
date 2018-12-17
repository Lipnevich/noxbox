package live.noxbox.menu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;

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

import static live.noxbox.tools.DateTimeFormatter.date;

/**
 * Created by nicolay.lipnevich on 22/06/2017.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context ctx;
    private String profileId;
    private List<Noxbox> historyItems;
    private Set<Noxbox> uniqueValue = new HashSet<>();
    private int lastVisibleItem, totalItemCount;
    private static final int AMOUNT_PER_LOAD = 10;


    public HistoryAdapter(Context context, final List<Noxbox> historyItems, RecyclerView currentRecyclerView) {
        this.ctx = context;
        this.profileId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.historyItems = historyItems;

        long startFrom = historyItems.isEmpty() ?
                Long.MAX_VALUE :
                historyItems.get(historyItems.size() - 1).getTimeCompleted();

        final Task<Collection<Noxbox>> uploadingTask = new Task<Collection<Noxbox>>() {
            @Override
            public void execute(Collection<Noxbox> items) {
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
            }
        };
        Firestore.readHistory(startFrom, AMOUNT_PER_LOAD, MarketRole.supply, uploadingTask);

        if (currentRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) currentRecyclerView.getLayoutManager();
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
                        // TODO (vl) use MarketRole.demand for payer's activity_history
                        Firestore.readHistory(startFrom, AMOUNT_PER_LOAD, MarketRole.supply, uploadingTask);
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
            viewHolder = new ItemViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_history, parent, false));
        } else {
            viewHolder = new ProgressViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_progress, parent, false));
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder viewHolder, int position) {
        if (viewHolder instanceof ProgressViewHolder) {
            viewHolder.progressBar.setIndeterminate(true);
            return;
        }

        final Noxbox noxbox = historyItems.get(position);

        if (noxbox.getTimeCompleted() != null) {
            viewHolder.time.setText(date(noxbox.getTimeCompleted()));
            viewHolder.date.setText(date(noxbox.getTimeCompleted()));
        } else if (noxbox.getTimeCanceledByParty() != null) {
            viewHolder.time.setText(date(noxbox.getTimeCanceledByParty()));
            viewHolder.date.setText(date(noxbox.getTimeCanceledByParty()));
        } else if (noxbox.getTimeCanceledByOwner() != null) {
            viewHolder.time.setText(date(noxbox.getTimeCanceledByOwner()));
            viewHolder.date.setText(date(noxbox.getTimeCanceledByOwner()));
        } else if (noxbox.getTimeTimeout() != null) {
            viewHolder.date.setText(date(noxbox.getTimeTimeout()));
            viewHolder.date.setText(date(noxbox.getTimeTimeout()));
        }

        viewHolder.price.setText(noxbox.getPrice());
        viewHolder.performerName.setText(noxbox.getNotMe(profileId).getName());

        //viewHolder.mapView.onCreate(null);
        //viewHolder.mapView.onResume();
//        viewHolder.mapView.getMapAsync(new OnMapReadyCallback() {
//            @Override
//            public void onMapReady(GoogleMap googleMap) {
//                MapsInitializer.initialize(ctx);
//                GroundOverlayOptions newarkMap = new GroundOverlayOptions()
//                        .image(BitmapDescriptorFactory.fromResource(noxbox.getType().getImage()))
//                        .position(noxbox.getPosition().toLatLng(), 48, 48)
//                        .anchor(0.5f, 1)
//                        .zIndex(1000);
//                GroundOverlay marker = googleMap.addGroundOverlay(newarkMap);
//                marker.setDimensions(960, 960);
//
//                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noxbox.getPosition().toLatLng(), 11));
//            }
//        });

        Glide.with(ctx)
                .load(noxbox.getNotMe(profileId).getPhoto())
                .apply(RequestOptions.circleCropTransform())
                .into(viewHolder.performerPhoto);

        boolean isLiked = isLiked(noxbox);
        showRating(viewHolder.rateNoxbox, isLiked);
        if (isLiked) {
            viewHolder.rateNoxbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.NoxboxAlertDialogStyle);
                    builder.setTitle(ctx.getResources().getString(R.string.dislikePrompt));
                    builder.setPositiveButton(ctx.getResources().getString(R.string.dislike),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    view.setOnClickListener(null);
                                    showRating((ImageView) view, false);
//                                    dislikeNoxbox(profileId, noxbox);
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.show();
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        //ItemViewHolder
        TextView date;
        TextView time;
        TextView price;
        ImageView performerPhoto;
        TextView performerName;
        //MapView mapView;
        ImageView rateNoxbox;

        //ProgressViewHolder
        ProgressBar progressBar;

        public HistoryViewHolder(@NonNull View layout) {
            super(layout);
        }
    }

    static class ItemViewHolder extends HistoryViewHolder {

        public ItemViewHolder(@NonNull View layout) {
            super(layout);
            date = layout.findViewById(R.id.dateText);
            time = layout.findViewById(R.id.timeText);
            price = layout.findViewById(R.id.priceText);
            performerPhoto = layout.findViewById(R.id.performerImage);
            performerName = layout.findViewById(R.id.performerName);
            //mapView = layout.findViewById(R.id.map);
            rateNoxbox = layout.findViewById(R.id.rateBox);
        }
    }

    static class ProgressViewHolder extends HistoryViewHolder {


        public ProgressViewHolder(@NonNull View layout) {
            super(layout);
            progressBar = layout.findViewById(R.id.progressBar);
        }
    }

    private boolean isLiked(Noxbox noxbox) {
        if (profileId.equals(noxbox.getOwner().getId())) {
            return noxbox.getTimeOwnerDisliked() == null;
        } else {
            return noxbox.getTimePartyDisliked() == null;
        }
    }

    private void showRating(ImageView view, boolean isLiked) {
        if (isLiked) {
            view.setImageResource(R.drawable.like);
        } else {
            view.setImageResource(R.drawable.dislike);
        }
    }
}
