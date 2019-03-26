package live.noxbox.activities.contract;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import live.noxbox.R;
import live.noxbox.model.TravelMode;

import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.activities.contract.TravelModeListFragment.CONTRACT_CODE;
import static live.noxbox.database.AppCache.executeUITasks;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.model.TravelMode.none;
import static live.noxbox.tools.location.LocationOperator.isLocationPermissionGranted;
import static live.noxbox.tools.location.LocationOperator.startLocationPermissionRequest;

/**
 * Created by Vladislaw Kravchenok on 26.03.2019.
 */
public class TravelModeListAdapter extends RecyclerView.Adapter<TravelModeListAdapter.ViewHolder> {

    private List<TravelMode> travelModes;
    private Activity activity;
    private DialogFragment travelModeListFragment;
    private int key;

    public TravelModeListAdapter(List<TravelMode> travelModes, Activity activity, DialogFragment travelModeListFragment, int key) {
        this.travelModes = travelModes;
        this.activity = activity;
        this.travelModeListFragment = travelModeListFragment;
        this.key = key;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new TravelModeListAdapter.ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_travel_mode, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.travelModeImage.setImageResource(travelModes.get(position).getImage());
        viewHolder.travelModeName.setText(travelModes.get(position).getName());
        viewHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (key == CONTRACT_CODE) {
                    TravelMode travelMode = TravelMode.byId(travelModes.get(position).getId());
                    profile().setTravelMode(travelMode);
                    profile().getContract().getOwner().setTravelMode(travelMode);

                    if (travelMode == none) {
                        profile().setHost(true);
                        profile().getContract().getOwner().setHost(true);
                    } else {
                        if (!isLocationPermissionGranted(activity.getApplicationContext())) {
                            startLocationPermissionRequest(activity, LOCATION_PERMISSION_REQUEST_CODE);
                        }
                    }
                    executeUITasks();
                    travelModeListFragment.dismiss();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return travelModes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemLayout;
        View divider;
        TextView travelModeName;
        ImageView travelModeImage;

        public ViewHolder(@NonNull View layout) {
            super(layout);
            itemLayout = layout.findViewById(R.id.itemLayout);
            divider = layout.findViewById(R.id.dividerFullWidth);
            travelModeName = layout.findViewById(R.id.travelModeName);
            travelModeImage = layout.findViewById(R.id.travelModeImage);
        }
    }
}
