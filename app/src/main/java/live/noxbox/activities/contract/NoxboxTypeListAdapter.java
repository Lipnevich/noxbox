package live.noxbox.activities.contract;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Portfolio;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static live.noxbox.activities.contract.NoxboxTypeListFragment.CONTRACT_CODE;
import static live.noxbox.activities.contract.NoxboxTypeListFragment.MAP_CODE;
import static live.noxbox.activities.contract.NoxboxTypeListFragment.PROFILE_CODE;
import static live.noxbox.database.GeoRealtime.stopListenAvailableNoxboxes;
import static live.noxbox.tools.PlayMarketManager.openApplicationMarketPage;

public class NoxboxTypeListAdapter extends RecyclerView.Adapter<NoxboxTypeListAdapter.ViewHolder> {

    private List<NoxboxType> noxboxTypes;
    private Activity activity;
    private DialogFragment rootFragment;
    private Profile profile;
    private int key;

    public NoxboxTypeListAdapter(List<NoxboxType> noxboxTypes, Activity activity, DialogFragment rootFragment, int key) {
        this.noxboxTypes = noxboxTypes;
        this.activity = activity;
        this.rootFragment = rootFragment;
        this.key = key;

        AppCache.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile object) {
                profile = object;
            }
        });
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

        Glide.with(activity)
                .load(noxboxTypes.get(position).getImage())
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable drawable, @Nullable Transition<? super Drawable> transition) {
                        viewHolder.noxboxTypeImage.setImageDrawable(drawable);
                    }
                });

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
        AppCache.fireProfile();
        rootFragment.dismiss();
    }

    private void executeInTheMap(int position) {
        for (NoxboxType type : NoxboxType.values()) {
            profile.getFilters().getTypes().put(type.name(), false);
        }
        profile.getFilters().getTypes().put(noxboxTypes.get(position).name(), true);

    }

    private void executeInTheProfile(int position) {
        profile.getPortfolio().put(noxboxTypes.get(position).name(), new Portfolio());
    }

    private void executeInTheContract(int position) {
        profile.getCurrent().setType(noxboxTypes.get(position));
    }
}
