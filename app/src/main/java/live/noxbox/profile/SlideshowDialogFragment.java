package live.noxbox.profile;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.model.ImageType;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.tools.DialogBuilder;
import live.noxbox.tools.Task;

import static live.noxbox.profile.ImageListAdapter.IMAGE_TYPE_KEY;
import static live.noxbox.profile.ImageListAdapter.PHOTOS_KEY;
import static live.noxbox.profile.ImageListAdapter.POSITION_KEY;
import static live.noxbox.profile.ImageListAdapter.TYPE_KEY;
import static live.noxbox.tools.ImageManager.deleteImage;

public class SlideshowDialogFragment extends DialogFragment {
    private List<String> photos;
    private ViewPager viewPager;
    private ViewPagerAdapter myViewPagerAdapter;
    private TextView lblCount;
    private int selectedPosition = 0;
    private int currentIndex;
    private NoxboxType type;
    private ImageType imageType;


    static SlideshowDialogFragment newInstance() {
        return new SlideshowDialogFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_slider, container, false);
        viewPager = v.findViewById(R.id.viewpager);
        lblCount = v.findViewById(R.id.itemCount);

        photos = (ArrayList<String>) getArguments().getSerializable(PHOTOS_KEY);
        selectedPosition = getArguments().getInt(POSITION_KEY);
        type = (NoxboxType) getArguments().getSerializable(TYPE_KEY);
        imageType = (ImageType) getArguments().getSerializable(IMAGE_TYPE_KEY);

        myViewPagerAdapter = new ViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(selectedPosition);

        v.findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        v.findViewById(R.id.deleteImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCache.readProfile(new Task<Profile>() {
                    @Override
                    public void execute(final Profile profile) {
                        DialogBuilder.createSimpleAlertDialog(getActivity(), R.string.wantDeleteImage, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                profile.getPortfolio().get(type.name()).getImages().get(imageType.name()).remove(currentIndex);
                                deleteImage(type, currentIndex, imageType);
                                dismiss();
                                AppCache.fireProfile();
                            }
                        });
                    }
                });
            }
        });

        return v;
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
        displayMetaInfo(selectedPosition);

    }


    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    private void displayMetaInfo(int position) {
        currentIndex = position;
        lblCount.setText((position + 1) + " of " + photos.size());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }


    public class ViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public ViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.item_image_fullscreen, container, false);

            ImageView imageViewPreview = view.findViewById(R.id.imagePreview);

            Glide.with(getActivity())
                    .load(photos.get(position))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(imageViewPreview);

            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
