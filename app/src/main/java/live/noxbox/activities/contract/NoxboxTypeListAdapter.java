package live.noxbox.activities.contract;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import live.noxbox.R;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Portfolio;

import static live.noxbox.database.AppCache.executeUITasks;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.database.GeoRealtime.stopListenAvailableNoxboxes;
import static live.noxbox.tools.PlayMarketManager.openApplicationMarketPage;

public class NoxboxTypeListAdapter extends RecyclerView.Adapter<NoxboxTypeListAdapter.ViewHolder> {
    public static final int MAP_CODE = 1010;
    public static final int CONTRACT_CODE = 1011;
    public static final int PROFILE_CODE = 1012;
    private List<NoxboxType> noxboxTypes;
    private Activity activity;
    private int key;

    public NoxboxTypeListAdapter(List<NoxboxType> noxboxTypes, Activity activity, int key) {
        this.noxboxTypes = noxboxTypes;
        this.activity = activity;
        this.key = key;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_filter_service, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {


        if (position == noxboxTypes.size() - 1) {
            viewHolder.divider.setVisibility(View.GONE);
        } else {
            viewHolder.divider.setVisibility(View.VISIBLE);
        }

        viewHolder.noxboxTypeName.setText(noxboxTypes.get(position).getName());


        viewHolder.noxboxTypeImage.setImageResource(noxboxTypes.get(position).getImageDemand());
        viewHolder.itemLayout.setOnClickListener(view -> onClick(position));
    }

    @Override
    public int getItemCount() {
        return noxboxTypes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemLayout;
        View divider;
        TextView noxboxTypeName;
        ImageView noxboxTypeImage;

        public ViewHolder(@NonNull View layout) {
            super(layout);
            itemLayout = layout.findViewById(R.id.itemLayout);
            divider = layout.findViewById(R.id.dividerFullWidth);
            noxboxTypeName = layout.findViewById(R.id.noxboxTypeName);
            noxboxTypeImage = layout.findViewById(R.id.noxboxTypeImage);
        }
    }

    private void onClick(int position) {
        if (noxboxTypes.get(position) == NoxboxType.redirect) {
            openApplicationMarketPage(activity);
        } else {
            switch (key) {
                case MAP_CODE: {
                    executeInTheMap(position);
                    break;
                }
                case CONTRACT_CODE: {
                    executeInTheContract(position);
                    break;
                }
                case PROFILE_CODE: {
                    executeInTheProfile(position);
                    break;
                }
            }
        }


       // availableNoxboxes.clear();
        stopListenAvailableNoxboxes();
        executeUITasks();
        activity.findViewById(R.id.noxboxTypeListLayout).setVisibility(View.GONE);
    }

    private void executeInTheMap(int position) {
        for (NoxboxType type : NoxboxType.values()) {
            profile().getFilters().getTypes().put(type.name(), false);
        }
        profile().getFilters().getTypes().put(noxboxTypes.get(position).name(), true);
    }

    private void executeInTheProfile(int position) {
        profile().getPortfolio().put(noxboxTypes.get(position).name(), new Portfolio(System.currentTimeMillis()));
    }

    private void executeInTheContract(int position) {
        profile().getContract().setType(noxboxTypes.get(position));
        executeUITasks();
    }
}
