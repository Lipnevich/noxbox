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
import java.util.List;

import live.noxbox.R;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageViewHolder> {

    private List<String> imageUrlList;
    private final ProfilePerformerActivity activity;
    private RecyclerView imageList;

    public ImageListAdapter(List<String> imageUrlList, ProfilePerformerActivity activity, RecyclerView imageList) {
        if (imageUrlList.size() == 0) {
            imageUrlList.add("");
        }
        this.imageUrlList = imageUrlList;
        this.activity = activity;
        this.imageList = imageList;
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


        if (position == imageUrlList.size() - 1 && !imageUrlList.get(0).equals("")) {
            setOnItemCertificateClickListener(imageList, imageUrlList);
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

    private <T extends String> void setOnItemCertificateClickListener(RecyclerView recyclerView, final List<T> imageUrlList) {
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(activity.getApplicationContext(), recyclerView, new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("photos", (Serializable) imageUrlList);
                bundle.putInt("position", position);

                FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment slideShowFragment = SlideshowDialogFragment.newInstance();
                slideShowFragment.setArguments(bundle);
                slideShowFragment.show(fragmentTransaction, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }
}
