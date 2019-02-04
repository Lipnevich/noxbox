package live.noxbox.menu.about.tutorial;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import live.noxbox.R;

public class TutorialFragment extends android.support.v4.app.Fragment {

    private static final String TEXT = "text";
    private static final String PAGE = "page";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String text = getResources().getString(getArguments().getInt(TEXT));
        int page = getArguments().getInt(PAGE);

        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
        ImageView mainImage = view.findViewById(R.id.image);
        switch (page) {
            case 1:
                setImage(mainImage, R.drawable.tutorial_page_one);
                break;
            case 2:
                setImage(mainImage, R.drawable.tutorial_page_two);
                break;
            case 3:
                setImage(mainImage, R.drawable.tutorial_page_three);
                break;
            case 4:
                setImage(mainImage, R.drawable.tutorial_page_four);
                break;
        }

        TextView informationText = view.findViewById(R.id.text);
        informationText.setText(text);

        return view;
    }

    private void setImage(ImageView view, int image) {
        Glide.with(this).asBitmap()
                .load(image)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        view.setImageBitmap(resource);
                    }
                });
    }

    public static TutorialFragment newInstance(int text, int page) {
        TutorialFragment tutorialFragment = new TutorialFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TEXT, text);
        bundle.putInt(PAGE, page);

        tutorialFragment.setArguments(bundle);

        return tutorialFragment;
    }
}