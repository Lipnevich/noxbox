package live.noxbox.tools;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import live.noxbox.R;

/**
 * Created by Vladislaw Kravchenok on 27.02.2019.
 */
public class ProgressDialogFragment extends DialogFragment {
    public static final String MAP_KEY = "MAP_PROGRESS";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_progress, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView progress = view.findViewById(R.id.progress);
        Glide.with(this)
                .asGif()
                .load(R.drawable.progress_cat)
                .into(progress);
        return view;
    }
}
