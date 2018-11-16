package live.noxbox.state.cluster;

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
import live.noxbox.detailed.DetailedActivity;
import live.noxbox.model.MarketRole;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.tools.Router;

public class ClusterAdapter extends RecyclerView.Adapter<ClusterAdapter.ClusterViewHolder> {

    private List<NoxboxMarker> clusterItems;
    private Activity activity;
    private Profile profile;

    public ClusterAdapter(List<NoxboxMarker> clusterItems, Activity activity, Profile profile) {
        this.clusterItems = clusterItems;
        this.activity = activity;
        this.profile = profile;
    }


    @NonNull
    @Override

    public ClusterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cluster, parent, false);
        return new ClusterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClusterViewHolder clusterViewHolder, final int i) {
        String price = clusterItems.get(i).getNoxbox().getPrice();
        NoxboxType type = clusterItems.get(i).getNoxbox().getType();
        for (NoxboxType noxboxType : NoxboxType.values()) {
            if (noxboxType == type) {
                clusterViewHolder.icon.setImageResource(noxboxType.getImage());
            }
        }

        String rating;
        if (clusterItems.get(i).getNoxbox().getRole() == MarketRole.supply) {
            rating =  String.valueOf(Profile.ratingToPercentage(
                    clusterItems.get(i).getNoxbox().getOwner().getSuppliesRating().get(type.name()).getReceivedLikes(),
                    clusterItems.get(i).getNoxbox().getOwner().getSuppliesRating().get(type.name()).getReceivedDislikes()));
        } else {
            rating =  String.valueOf(Profile.ratingToPercentage(
                    clusterItems.get(i).getNoxbox().getOwner().getDemandsRating().get(type.name()).getReceivedLikes(),
                    clusterItems.get(i).getNoxbox().getOwner().getDemandsRating().get(type.name()).getReceivedDislikes()));
        }


        clusterViewHolder.rating.setText(rating.concat("% ").concat(activity.getResources().getString(R.string.rating)));
        clusterViewHolder.type.setText(type.getName());
        clusterViewHolder.price.setText(price);

        clusterViewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.setViewed(clusterItems.get(i).getNoxbox());
                Router.startActivity(activity, DetailedActivity.class);
                activity.finish();
            }
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

        public ClusterViewHolder(@NonNull View layout) {
            super(layout);
            rootView = layout.findViewById(R.id.rootView);
            icon = layout.findViewById(R.id.icon);
            type = layout.findViewById(R.id.type);
            price = layout.findViewById(R.id.price);
            rating = layout.findViewById(R.id.rating);

        }
    }
}
