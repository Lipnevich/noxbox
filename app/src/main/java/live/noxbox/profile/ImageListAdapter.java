package live.noxbox.profile;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;

import java.util.List;

import live.noxbox.R;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageViewHolder> {

    private List<String> imageUrlList;
    private final ProfileActivity activity;

    public ImageListAdapter(List<String> imageUrlList, ProfileActivity activity) {
        this.imageUrlList = imageUrlList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        final ImageButton imageButton = holder.image;

        Glide.with(activity)
                .asDrawable()
                .load(imageUrlList.get(position))
                .into(imageButton);

    }

    @Override
    public int getItemCount() {
        return imageUrlList.size();
    }


    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageButton image;


        ImageViewHolder(View layout) {
            super(layout);
            image = layout.findViewById(R.id.image);
        }
    }
}
