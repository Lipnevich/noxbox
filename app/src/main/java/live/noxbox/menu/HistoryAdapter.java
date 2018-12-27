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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import live.noxbox.BuildConfig;
import live.noxbox.R;
import live.noxbox.database.Firestore;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.tools.Task;

import static live.noxbox.tools.DateTimeFormatter.date;
import static live.noxbox.tools.DateTimeFormatter.time;
import static live.noxbox.tools.DateTimeFormatter.year;

/**
 * Created by nicolay.lipnevich on 22/06/2017.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private String profileId;
    private List<Noxbox> historyItems;
    private Set<Noxbox> uniqueValue = new HashSet<>();
    private int lastVisibleItem, totalItemCount;
    private static final int AMOUNT_PER_LOAD = 10;


    public HistoryAdapter(Context context, final List<Noxbox> historyItems, RecyclerView currentRecyclerView, final MarketRole role) {
        this.context = context;
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

                //for debbuging yearsBorder
                if (BuildConfig.DEBUG) {
                    Long[] datesInMillis = new Long[]{1505117471000L, 1507709471000L, 1510387871000L, 1473581471000L, 1476173471000L, 1478851871000L, 1481443871000L, 1441959071000L, 1410419471000L};
                    if (!noxboxes.isEmpty()) {
                        for (int i = 0; i < datesInMillis.length; i++) {
                            Noxbox noxbox = new Noxbox().setId("" + ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
                            noxbox.setParty(noxboxes.get(0).getParty());
                            noxbox.setOwner(noxboxes.get(0).getOwner());
                            noxbox.setType(noxboxes.get(0).getType());
                            noxbox.setPosition(noxboxes.get(0).getPosition());
                            noxbox.setPrice(noxboxes.get(0).getPrice());
                            noxbox.setRole(noxboxes.get(0).getRole());
                            noxbox.setWorkSchedule(noxboxes.get(0).getWorkSchedule());
                            noxbox.setTimeCompleted(datesInMillis[i]);

                            uniqueValue.add(noxbox);
                            noxboxes.add(noxbox);
                        }
                    }
                }

                if (noxboxes.size() > 0) {
                    historyItems.addAll(noxboxes);
                    notifyDataSetChanged();
                }
            }
        };
        Firestore.readHistory(startFrom, AMOUNT_PER_LOAD, role, uploadingTask);

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
            viewHolder = new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.item_history, parent, false));
        } else {
            viewHolder = new ProgressViewHolder(LayoutInflater.from(context).inflate(R.layout.item_progress, parent, false));
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final HistoryViewHolder viewHolder, int position) {
        if (viewHolder instanceof ProgressViewHolder) {
            viewHolder.progressBar.setIndeterminate(true);
            return;
        }

        final Noxbox noxbox = historyItems.get(position);

        viewHolder.time.setText(time(noxbox.getTimeCompleted()));
        viewHolder.date.setText(date(noxbox.getTimeCompleted()) + ",");
        showYearDivider(viewHolder, position);


        viewHolder.price.setText(noxbox.getPrice() + " " + context.getResources().getString(R.string.currency));
        viewHolder.performerName.setText(noxbox.getNotMe(profileId).getName());
        viewHolder.noxboxType.setText(noxbox.getType().getName());

        Glide.with(context)
                .load(noxbox.getNotMe(profileId).getPhoto())
                .apply(RequestOptions.circleCropTransform())
                .into(viewHolder.performerPhoto);

        boolean isLiked = isLiked(noxbox);
        showRating(viewHolder.rateNoxbox, isLiked);
        if (isLiked) {
            viewHolder.rateNoxbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.NoxboxAlertDialogStyle);
                    builder.setTitle(context.getResources().getString(R.string.dislikePrompt));
                    builder.setPositiveButton(context.getResources().getString(R.string.dislike),
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

        viewHolder.rootHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.expandableHistoryLayout.getVisibility() == View.GONE) {
                    showMapView(viewHolder, noxbox);
                } else {
                    viewHolder.expandableHistoryLayout.setVisibility(View.GONE);
                    viewHolder.mapView.removeAllViews();
                }

            }
        });
    }

    private void showYearDivider(HistoryViewHolder viewHolder, int position) {
        if (position > 0 && position < historyItems.size() && !year(historyItems.get(position - 1).getTimeCompleted()).equals(year(historyItems.get(position).getTimeCompleted()))) {
            viewHolder.layoutYearDivider.setVisibility(View.VISIBLE);
            viewHolder.textYear.setText(year(historyItems.get(position).getTimeCompleted()));
        }
    }


    private void showMapView(HistoryViewHolder viewHolder, final Noxbox noxbox) {
        viewHolder.expandableHistoryLayout.setVisibility(View.VISIBLE);
        viewHolder.mapView.onCreate(null);
        viewHolder.mapView.onResume();
        viewHolder.mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsInitializer.initialize(context);
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
            }
        });
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
        MapView mapView;
        ImageView rateNoxbox;
        LinearLayout expandableHistoryLayout;

        //include part
        LinearLayout layoutYearDivider;
        TextView textYear;

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
            mapView = layout.findViewById(R.id.map);
            rateNoxbox = layout.findViewById(R.id.rateBox);
            expandableHistoryLayout = layout.findViewById(R.id.expandableHistoryLayout);

            layoutYearDivider = layout.findViewById(R.id.layoutYearDivider);
            textYear = layout.findViewById(R.id.textYear);
        }
    }

    class ProgressViewHolder extends HistoryViewHolder {


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
