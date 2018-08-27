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

    private List<String> imageUrlList;
    private ProfilePerformerActivity activity;
    private final RecyclerView imageList;
    private final NoxboxType type;
    private final ImageType imageType;
    private RecyclerTouchListener recyclerTouchListener;

    public ImageListAdapter(final List<String> imageUrlList, final ProfilePerformerActivity activity, final RecyclerView imageList, final ImageType imageType, final NoxboxType type) {
        this.imageUrlList = imageUrlList == null ? new ArrayList<String>() : imageUrlList;
        this.activity = activity;
        this.imageList = imageList;
        this.imageType = imageType;
        this.type = type;

        this.recyclerTouchListener = new RecyclerTouchListener(activity.getApplicationContext(), imageList, new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("photos", (Serializable) imageUrlList);
                bundle.putInt("position", position);
                bundle.putSerializable("type", type);
                bundle.putSerializable("imageType", imageType);


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
