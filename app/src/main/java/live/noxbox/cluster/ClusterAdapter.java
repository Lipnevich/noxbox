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
import live.noxbox.model.MarketRole;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
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

    @NonNull
    @Override
    public ClusterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cluster, parent, false);
        return new ClusterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClusterViewHolder clusterViewHolder, final int position) {
        NoxboxType type = clusterItems.get(position).getNoxbox().getType();

        clusterViewHolder.icon.setImageResource(clusterItems.get(position).getNoxbox().getIcon());

        String rating;

        if (clusterItems.get(position).getNoxbox().getRole() == MarketRole.supply) {
            Rating supplyRating = clusterItems.get(position).getNoxbox().getOwner().getSuppliesRating().get(type.name());
            rating = String.valueOf(Profile.ratingToPercentage(supplyRating.getReceivedLikes(),
                    supplyRating.getReceivedDislikes()));
        } else {
            Rating demandRating = clusterItems.get(position).getNoxbox().getOwner().getDemandsRating().get(type.name());
            rating = String.valueOf(Profile.ratingToPercentage(
                    demandRating.getReceivedLikes(),
                    demandRating.getReceivedDislikes()));
        }

        int travelModeImage = clusterItems.get(position).getNoxbox().getOwner().getTravelMode().getImage();

        String role;
        if (clusterItems.get(position).getNoxbox().getRole() == MarketRole.supply) {
            role = activity.getResources().getString(R.string.worker);
        } else {
            role = activity.getResources().getString(R.string.costumer);
        }

        clusterViewHolder.rating.setText(rating.concat("% ").concat(activity.getResources().getString(R.string.rating)));
        clusterViewHolder.type.setText(type.getName());
        clusterViewHolder.price.setText(clusterItems.get(position).getNoxbox().getPrice());
        clusterViewHolder.travelModeImage.setImageResource(travelModeImage);
        clusterViewHolder.role.setText(role);

        clusterViewHolder.rootView.setOnClickListener(v -> {
            profile().setViewed(clusterItems.get(position).getNoxbox());
            profile().getViewed().setParty(profile().publicInfo());
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
