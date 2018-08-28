package live.noxbox.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

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

    private List<String> imageUrlList;
    private ProfilePerformerActivity activity;
    private final RecyclerView imageList;
    private RecyclerTouchListener recyclerTouchListener;

    public ImageListAdapter(final List<String> imageUrlList, final ProfilePerformerActivity activity, final RecyclerView imageList, final ImageType imageType, final NoxboxType type) {
        this.imageUrlList = imageUrlList == null ? new ArrayList<String>() : imageUrlList;
        this.activity = activity;
        this.imageList = imageList;

        this.recyclerTouchListener = new RecyclerTouchListener(activity.getApplicationContext(), imageList, new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(PHOTOS_KEY, (Serializable) imageUrlList);
                bundle.putInt(POSITION_KEY, position);
                bundle.putSerializable(TYPE_KEY, type);
                bundle.putSerializable(IMAGE_TYPE_KEY, imageType);

                FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment slideShowFragment = SlideshowDialogFragment.newInstance();
                slideShowFragment.setArguments(bundle);
                slideShowFragment.show(fragmentTransaction, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) { }
        });
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
        if (position == imageUrlList.size() - 1){
            imageList.addOnItemTouchListener(recyclerTouchListener);
        }

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
