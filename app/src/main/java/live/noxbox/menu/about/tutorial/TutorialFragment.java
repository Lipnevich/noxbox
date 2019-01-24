package live.noxbox.menu.about.tutorial;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import live.noxbox.R;

public class TutorialFragment extends android.support.v4.app.Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
        String text = getArguments().getString("text");
        int mainImageResource = getArguments().getInt("image");

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float displayWidth = displayMetrics.widthPixels / displayMetrics.density;
        float displayHeight = displayMetrics.heightPixels / displayMetrics.density;

        ImageView mainImage = view.findViewById(R.id.image);
        float heightDp = (float) (displayWidth * 1.43);//todo (vl) 143% value need to use for define a height of the image (we are using 230% at the moment)
        LinearLayout.LayoutParams mainImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (heightDp));
        Drawable mainImageDrawable = getResources().getDrawable(mainImageResource);
        Bitmap mainImageBitmap = drawableToBitmap(mainImageDrawable);
        mainImage.setImageBitmap(cropBitmapTransparency(mainImageBitmap));
        mainImage.setLayoutParams(mainImageParams);

        ImageView tapeImage = view.findViewById(R.id.tape);
        RelativeLayout.LayoutParams tapeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 72);
        tapeParams.setMargins(24, 0, 24, 0);
        tapeImage.setLayoutParams(tapeParams);

        TextView informationText = view.findViewById(R.id.text);
        informationText.setText(text);
        RelativeLayout.LayoutParams informationParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        informationParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        informationText.setLayoutParams(informationParams);

        LinearLayout underRootLayout = view.findViewById(R.id.underRootLayout);
        RelativeLayout.LayoutParams underRootLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        underRootLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        underRootLayout.setLayoutParams(underRootLayoutParams);


        return view;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private Bitmap cropBitmapTransparency(Bitmap sourceBitmap) {
        int minX = sourceBitmap.getWidth();
        int minY = sourceBitmap.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < sourceBitmap.getHeight(); y++) {
            for (int x = 0; x < sourceBitmap.getWidth(); x++) {
                int alpha = (sourceBitmap.getPixel(x, y) >> 24) & 255;
                if (alpha > 0) {
                    if (x < minX)
                        minX = x;
                    if (x > maxX)
                        maxX = x;
                    if (y < minY)
                        minY = y;
                    if (y > maxY)
                        maxY = y;
                }
            }
        }
        if ((maxX < minX) || (maxY < minY))
            return null;

        return Bitmap.createBitmap(sourceBitmap, minX, minY, (maxX - minX) + 1, (maxY - minY) + 1);
    }

    public static TutorialFragment newInstance(String text, int image) {
        TutorialFragment tutorialFragment = new TutorialFragment();
        Bundle bundle = new Bundle();
        bundle.putString("text", text);
        bundle.putInt("image", image);

        tutorialFragment.setArguments(bundle);

        return tutorialFragment;
    }
}