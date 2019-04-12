package live.noxbox.cluster;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import live.noxbox.R;
import live.noxbox.activities.detailed.DetailedActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.ProfileRatings;
import live.noxbox.model.Rating;
import live.noxbox.tools.Router;

import static live.noxbox.database.AppCache.profile;

public class ClusterAdapter extends RecyclerView.Adapter<ClusterAdapter.ClusterViewHolder> {

    private List<NoxboxMarker> clusterItems;
    private Activity activity;

    public ClusterAdapter(List<NoxboxMarker> clusterItems, Activity activity) {
        this.clusterItems = clusterItems;
        this.activity = activity;
    }

    public void add(NoxboxMarker noxboxMarker, int position) {
        clusterItems.add(position, noxboxMarker);
        notifyItemInserted(position);
    }

    public void remove(int position){
        clusterItems.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ClusterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cluster, parent, false);
        return new ClusterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClusterViewHolder clusterViewHolder, final int position) {
        Noxbox noxbox = clusterItems.get(position).getNoxbox();
        if (!AppCache.availableNoxboxes.containsKey(noxbox.getId()))
            return;

        NoxboxType type = noxbox.getType();

        clusterViewHolder.icon.setImageResource(noxbox.getIcon());

        String rating;

        if (noxbox.getRole() == MarketRole.supply) {
            Rating supplyRating = noxbox.getOwner().getRatings().getSuppliesRating().get(type.name());
            if (supplyRating == null) {
                supplyRating = new Rating();
            }
            rating = String.valueOf(ProfileRatings.ratingToPercentage(supplyRating.getReceivedLikes(),
                    supplyRating.getReceivedDislikes()));
        } else {
            Rating demandRating = noxbox.getOwner().getRatings().getDemandsRating().get(type.name());
            if (demandRating == null) {
                demandRating = new Rating();
            }
            rating = String.valueOf(ProfileRatings.ratingToPercentage(
                    demandRating.getReceivedLikes(),
                    demandRating.getReceivedDislikes()));
        }

        int travelModeImage = noxbox.getOwner().getTravelMode().getImage();

        String role;
        if (noxbox.getRole() == MarketRole.supply) {
            role = activity.getResources().getString(R.string.worker);
        } else {
            role = activity.getResources().getString(R.string.costumer);
        }

        clusterViewHolder.rating.setText(rating.concat("% ").concat(activity.getResources().getString(R.string.rating)));
        clusterViewHolder.type.setText(type.getName());
        clusterViewHolder.price.setText(noxbox.getPrice());
        clusterViewHolder.travelModeImage.setImageResource(travelModeImage);
        clusterViewHolder.role.setText(role);

        clusterViewHolder.rootView.setOnClickListener(v -> {
            profile().setViewed(noxbox);
            Router.startActivity(activity, DetailedActivity.class);
        });
    }

    @Override
    public int getItemCount() {
        return clusterItems.size();
    }

    class ClusterViewHolder extends RecyclerView.ViewHolder {
        CardView rootView;
        ImageView icon;
        TextView type;
        TextView price;
        TextView rating;

        ImageView travelModeImage;
        TextView role;

        ClusterViewHolder(@NonNull View layout) {
            super(layout);
            rootView = layout.findViewById(R.id.rootView);
            icon = layout.findViewById(R.id.icon);
            type = layout.findViewById(R.id.type);
            price = layout.findViewById(R.id.price);
            rating = layout.findViewById(R.id.rating);

            travelModeImage = layout.findViewById(R.id.travelModeImage);
            role = layout.findViewById(R.id.role);

        }
    }
}
