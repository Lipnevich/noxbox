package live.noxbox.menu.profile;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import live.noxbox.R;
import live.noxbox.model.NoxboxType;

public class PortfolioNoxboxTypeAdapter extends RecyclerView.Adapter<PortfolioNoxboxTypeAdapter.PortfolioNoxboxTypeViewHolder> {

    private List<NoxboxType> typeList;
    private final ProfileActivity activity;

    public PortfolioNoxboxTypeAdapter(List<NoxboxType> typeList, ProfileActivity activity) {
        this.typeList = typeList;
        this.activity = activity;
    }


    @NonNull
    @Override
    public PortfolioNoxboxTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_portfolio_noxbox_type, parent, false);
        return new PortfolioNoxboxTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PortfolioNoxboxTypeViewHolder holder, final int position) {
        holder.noxboxTypeImage.setImageResource(typeList.get(position).getImage());
        holder.noxboxTypeName.setText(typeList.get(position).getName());
        holder.noxboxTypeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ProfilePerformerActivity.class);
                intent.putExtra(ProfileActivity.class.getName(), typeList.get(position).getId());
                activity.startActivityForResult(intent,ProfilePerformerActivity.CODE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return typeList.size();
    }

    static class PortfolioNoxboxTypeViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout noxboxTypeLayout;
        ImageView noxboxTypeImage;
        TextView noxboxTypeName;


        PortfolioNoxboxTypeViewHolder(View layout) {
            super(layout);
            noxboxTypeLayout = layout.findViewById(R.id.noxboxTypeLayout);
            noxboxTypeImage = layout.findViewById(R.id.noxboxTypeImage);
            noxboxTypeName = layout.findViewById(R.id.noxboxTypeName);



        }
    }
}
