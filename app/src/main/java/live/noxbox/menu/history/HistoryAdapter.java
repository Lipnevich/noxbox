package live.noxbox.menu.history;

import android.app.AlertDialog;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.analitics.BusinessEvent;
import live.noxbox.database.Firestore;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.tools.AddressManager;
import live.noxbox.tools.Task;

import static live.noxbox.database.AppCache.profile;
import static live.noxbox.database.AppCache.showPriceInUsd;
import static live.noxbox.menu.history.HistoryActivity.isHistoryEmpty;
import static live.noxbox.menu.history.HistoryActivity.isHistoryThere;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.DateTimeFormatter.date;
import static live.noxbox.tools.DateTimeFormatter.getFormatTimeFromMillis;
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

    public HistoryAdapter(HistoryActivity activity, RecyclerView currentRecyclerView, final MarketRole role) {
        this.activity = activity;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        this.profileId = user == null ? "" : user.getUid();
        this.historyItems = new ArrayList<>();

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
                isHistoryThere.execute(null);
                if(role == MarketRole.supply){
                    HistoryActivity.isSupplyHistoryEmpty = false;
                }else{
                    HistoryActivity.isDemandHistoryEmpty = false;
                }
            } else {
                if(historyItems.size() == 0){
                    if(role == MarketRole.supply){
                        HistoryActivity.isSupplyHistoryEmpty = true;
                    }else{
                        HistoryActivity.isDemandHistoryEmpty = true;
                    }
                    isHistoryEmpty.execute(null);
                }
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

        viewHolder.noxboxTypeImage.setImageResource(noxbox.getType().getImage());
        viewHolder.time.setText(time(noxbox.getTimeCompleted()));
        viewHolder.date.setText(date(noxbox.getTimeCompleted()) + ",");
        long timeStartPerforming = noxbox.getTimePartyVerified() > noxbox.getTimeOwnerVerified() ? noxbox.getTimePartyVerified() : noxbox.getTimeOwnerVerified();
        viewHolder.noxboxType.setText(activity.getResources().getString(noxbox.getType().getName()).concat(", "
                .concat(getFormatTimeFromMillis(timeStartPerforming, noxbox.getTimeCompleted(), activity.getResources()))));
        viewHolder.price.setText(noxbox.getTotal().concat(" " + showPriceInUsd(activity.getResources().getString(R.string.currency), noxbox.getTotal())));


        viewHolder.rootHistoryLayout.setOnClickListener(view1 -> onClick(viewHolder, noxbox));
    }


    private LinearLayout showedExpandableLayout;

    private void onClick(HistoryAdapter.HistoryViewHolder viewHolder, Noxbox noxbox) {
        if (showedExpandableLayout != null && showedExpandableLayout.equals(viewHolder.expandableLayout)) {
            showedExpandableLayout.setVisibility(View.GONE);
            showedExpandableLayout = null;
            return;
        }
        if (showedExpandableLayout != null) {
            showedExpandableLayout.setVisibility(View.GONE);
        }
        showExpandableLayout(viewHolder, noxbox);
    }

    private void showExpandableLayout(HistoryAdapter.HistoryViewHolder viewHolder, Noxbox noxbox) {
        showedExpandableLayout = viewHolder.expandableLayout;
        showedExpandableLayout.setVisibility(View.VISIBLE);

        new Handler().post(() -> viewHolder.address.setText(AddressManager.provideAddressByPosition(activity, noxbox.getPosition())));
        attachMapView(viewHolder.mapView, noxbox);

        Glide.with(activity)
                .load(noxbox.getNotMe(profileId).getPhoto())
                .apply(RequestOptions.circleCropTransform())
                .into(viewHolder.participantPhoto);

        viewHolder.participantName.setText(noxbox.getNotMe(profileId).getName());

        if (noxbox.getPerformer().equals(profile())) {
            viewHolder.profession.setText(R.string.customer);
        } else {
            viewHolder.profession.setText(noxbox.getType().getProfession());
        }

        attachRating(viewHolder.like, viewHolder.dislike, isNotDisliked(noxbox));
        viewHolder.like.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.NoxboxAlertDialogStyle);
            builder.setTitle(activity.getResources().getString(R.string.likePrompt));
            builder.setPositiveButton(activity.getResources().getString(R.string.like),
                    (dialog, which) -> {
                        Noxbox clearedDislikNoxbox = new Noxbox();
                        clearedDislikNoxbox.copy(noxbox);

                        clearDislikeNoxbox(clearedDislikNoxbox);

                        updateNoxbox(clearedDislikNoxbox, object -> {
                            BusinessActivity.businessEvent(BusinessEvent.like,
                                    noxbox.getId(),
                                    noxbox.getType().name(),
                                    noxbox.getPrice());
                            noxbox.copy(clearedDislikNoxbox);
                            viewHolder.like.setOnClickListener(null);
                            viewHolder.dislike.setOnClickListener(null);
                            if (showedExpandableLayout.equals(viewHolder.expandableLayout)) {
                                showExpandableLayout(viewHolder, noxbox);
                            }
                        }, object -> onClick(viewHolder, noxbox));

                    });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();

        });
        viewHolder.dislike.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.NoxboxAlertDialogStyle);
            builder.setTitle(activity.getResources().getString(R.string.dislikePrompt));
            builder.setPositiveButton(activity.getResources().getString(R.string.dislike),
                    (dialog, which) -> {
                        Noxbox dislikedNoxbox = new Noxbox();
                        dislikedNoxbox.copy(noxbox);

                        dislikeNoxbox(dislikedNoxbox);

                        updateNoxbox(dislikedNoxbox, object -> {
                            BusinessActivity.businessEvent(BusinessEvent.dislike,
                                    noxbox.getId(),
                                    noxbox.getType().name(),
                                    noxbox.getPrice());
                            noxbox.copy(dislikedNoxbox);
                            viewHolder.like.setOnClickListener(null);
                            viewHolder.dislike.setOnClickListener(null);
                            if (showedExpandableLayout.equals(viewHolder.expandableLayout)) {
                                showExpandableLayout(viewHolder, noxbox);
                            }
                        }, object -> onClick(viewHolder, noxbox));
                    });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        });

        attachCommentView(viewHolder, noxbox, isNotCommented(noxbox));
        attachMyComment(viewHolder, noxbox);
        viewHolder.send.setOnClickListener(v -> {
            if (viewHolder.comment.getText().toString().length() > 0) {
                Noxbox commentedNoxbox = new Noxbox();
                commentedNoxbox.copy(noxbox);

                commentNoxbox(viewHolder, commentedNoxbox);

                updateNoxbox(commentedNoxbox, object -> {
                    noxbox.copy(commentedNoxbox);
                    if (showedExpandableLayout.equals(viewHolder.expandableLayout)) {
                        showExpandableLayout(viewHolder, noxbox);
                    }
                }, object -> onClick(viewHolder, noxbox));
            }
        });

        viewHolder.divider.setVisibility(View.VISIBLE);
    }

    private void attachMyComment(HistoryViewHolder viewHolder, Noxbox noxbox) {
        if (profileId.equals(noxbox.getOwner().getId())) {
            viewHolder.commentText.setText(noxbox.getOwnerComment());
        } else {
            viewHolder.commentText.setText(noxbox.getPartyComment());
        }
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
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
            marker.setDimensions(2020, 2020);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noxbox.getPosition().toLatLng(), 11));

            googleMap.getUiSettings().setAllGesturesEnabled(false);
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);
        });
    }

    private void attachRating(ImageView like, ImageView dislike, boolean isNotDisliked) {
        if (isNotDisliked) {
            like.setColorFilter(activity.getResources().getColor(R.color.primary));
            dislike.setColorFilter(activity.getResources().getColor(R.color.divider));
        } else {
            like.setColorFilter(activity.getResources().getColor(R.color.divider));
            dislike.setColorFilter(activity.getResources().getColor(R.color.low_rating_color));
        }
    }

    private void attachCommentView(HistoryAdapter.HistoryViewHolder viewHolder, Noxbox noxbox, boolean isNotComment) {
        if (isNotComment) {
            viewHolder.commentView.setVisibility(View.GONE);
            viewHolder.comment.setVisibility(View.VISIBLE);
            viewHolder.send.setVisibility(View.VISIBLE);

        } else {
            viewHolder.comment.setVisibility(View.GONE);
            viewHolder.send.setVisibility(View.GONE);
            viewHolder.commentView.setVisibility(View.VISIBLE);
            viewHolder.commenterName.setText("\"" + noxbox.getMe(profileId).getName() + "\"");
        }

    }

    private boolean isNotDisliked(Noxbox noxbox) {
        if (profileId.equals(noxbox.getOwner().getId())) {
            return isNullOrZero(noxbox.getTimeOwnerDisliked());
        } else {
            return isNullOrZero(noxbox.getTimePartyDisliked());
        }
    }

    private boolean isNotCommented(Noxbox noxbox) {
        if (profileId.equals(noxbox.getOwner().getId())) {
            return noxbox.getOwnerComment().length() == 0;
        } else {
            return noxbox.getPartyComment().length() == 0;
        }
    }

    private void commentNoxbox(HistoryAdapter.HistoryViewHolder viewHolder, Noxbox noxbox) {
        if (profileId.equals(noxbox.getOwner().getId())) {
            noxbox.setOwnerComment(viewHolder.comment.getText().toString());
        } else {
            noxbox.setPartyComment(viewHolder.comment.getText().toString());
        }
    }

    private void dislikeNoxbox(Noxbox noxbox) {
        if (noxbox.getOwner().getId().equals(profileId)) {
            noxbox.setTimeOwnerDisliked(System.currentTimeMillis());
        } else {
            noxbox.setTimePartyDisliked(System.currentTimeMillis());
        }
    }

    private void clearDislikeNoxbox(Noxbox noxbox) {
        if (noxbox.getOwner().getId().equals(profileId)) {
            noxbox.setTimeOwnerDisliked(0L);
        } else {
            noxbox.setTimePartyDisliked(0L);
        }
    }


    private void updateNoxbox(Noxbox noxbox, Task<String> success, Task<Exception> failure) {
        Firestore.writeNoxbox(noxbox, success, failure);
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout rootHistoryLayout;

        //ItemViewHolder
        ImageView noxboxTypeImage;
        TextView date;
        TextView time;
        TextView noxboxType;
        TextView price;

        LinearLayout expandableLayout;
        TextView address;
        MapView mapView;
        ImageView participantPhoto;
        TextView participantName;
        TextView profession;
        ImageView like;
        ImageView dislike;

        EditText comment;
        ImageView send;
        //these or
        TextView commenterName;
        LinearLayout commentView;
        TextView commentText;

        View divider;

        LinearLayout missingHistoryLayout;
        Button chooseService;

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

            noxboxTypeImage = layout.findViewById(R.id.noxboxTypeImage);
            date = layout.findViewById(R.id.date);
            time = layout.findViewById(R.id.time);
            noxboxType = layout.findViewById(R.id.noxboxType);
            price = layout.findViewById(R.id.price);

            expandableLayout = layout.findViewById(R.id.expandableLayout);
            address = layout.findViewById(R.id.address);
            mapView = layout.findViewById(R.id.map);
            participantPhoto = layout.findViewById(R.id.participantPhoto);
            participantName = layout.findViewById(R.id.participantName);
            profession = layout.findViewById(R.id.profession);
            like = layout.findViewById(R.id.like);
            dislike = layout.findViewById(R.id.dislike);

            comment = layout.findViewById(R.id.comment);
            send = layout.findViewById(R.id.send);
            //or
            commenterName = layout.findViewById(R.id.commenterName);
            commentView = layout.findViewById(R.id.commentView);
            commentText = layout.findViewById(R.id.commentText);

            divider = layout.findViewById(R.id.dividerFullWidth);

            //missing history
            missingHistoryLayout = layout.findViewById(R.id.missingHistoryLayout);
            chooseService = layout.findViewById(R.id.chooseService);

        }
    }

    class ProgressViewHolder extends HistoryViewHolder {


        public ProgressViewHolder(@NonNull View layout) {
            super(layout);
            progressBar = layout.findViewById(R.id.progressBar);
        }
    }
}
