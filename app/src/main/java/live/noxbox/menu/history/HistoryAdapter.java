package live.noxbox.menu.history;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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
import com.google.android.gms.maps.MapView;
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

import static live.noxbox.tools.DateTimeFormatter.date;
import static live.noxbox.tools.DateTimeFormatter.time;
import static live.noxbox.tools.DateTimeFormatter.year;

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
        showYearDivider(viewHolder, position);

        viewHolder.performerName.setText(noxbox.getNotMe(profileId).getName());
        viewHolder.noxboxType.setText(noxbox.getType().getName());

        Glide.with(activity)
                .load(noxbox.getNotMe(profileId).getPhoto())
                .apply(RequestOptions.circleCropTransform())
                .into(viewHolder.performerPhoto);

        viewHolder.rootHistoryLayout.setOnClickListener(view1 -> onClick(viewHolder, noxbox));
    }

    private void onClick(HistoryAdapter.HistoryViewHolder viewHolder, Noxbox noxbox) {
        HistoryFullScreenFragment.currentItemHistory = noxbox;
        DialogFragment dialog = new HistoryFullScreenFragment();
        Bundle bundle = new Bundle();
        bundle.putString("profileId", profileId);
        dialog.setArguments(bundle);
        dialog.show(activity.getSupportFragmentManager(), HistoryFullScreenFragment.TAG);
    }

    private void showYearDivider(HistoryViewHolder viewHolder, int position) {
        //TODO (vl) когда прокручиваем снизу вверх появляется год для текущего года
        if (position > 0 && position < historyItems.size() && !year(historyItems.get(position - 1).getTimeCompleted()).equals(year(historyItems.get(position).getTimeCompleted()))) {
            viewHolder.layoutYearDivider.setVisibility(View.VISIBLE);
            viewHolder.textYear.setText(year(historyItems.get(position).getTimeCompleted()));
        }
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


}
