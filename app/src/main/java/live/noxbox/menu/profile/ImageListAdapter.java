package live.noxbox.menu.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;
import live.noxbox.model.ImageType;
import live.noxbox.model.NoxboxType;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageViewHolder> {

    static final String PHOTOS_KEY = "photos";
    static final String POSITION_KEY = "position";
    static final String TYPE_KEY = "type";
    static final String IMAGE_TYPE_KEY = "imageType";
    static final String EDITABLE_KEY = "editable";

    private List<String> imageUrlList;
    private AppCompatActivity activity;
    private final ImageType imageType;
    private final NoxboxType type;
    private final boolean editable;

    public ImageListAdapter(final List<String> imageUrlList, final AppCompatActivity activity, final ImageType imageType, final NoxboxType type, final boolean editable) {
        this.imageUrlList = imageUrlList == null ? new ArrayList<String>() : imageUrlList;
        this.activity = activity;
        this.imageType = imageType;
        this.type = type;
        this.editable = editable;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, int position) {

        Glide.with(activity)
                .asDrawable()
                .load(imageUrlList.get(position))
                .into(holder.image);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(EDITABLE_KEY, editable);
                bundle.putSerializable(PHOTOS_KEY, (Serializable) imageUrlList);
                bundle.putInt(POSITION_KEY, holder.getAdapterPosition());
                bundle.putSerializable(TYPE_KEY, type);
                bundle.putSerializable(IMAGE_TYPE_KEY, imageType);

                FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment slideShowFragment = SlideshowDialogFragment.newInstance();
                slideShowFragment.setArguments(bundle);
                slideShowFragment.show(fragmentTransaction, imageType.name());
            }
        });
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
