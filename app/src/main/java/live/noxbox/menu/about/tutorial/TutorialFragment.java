package live.noxbox.menu.about.tutorial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import live.noxbox.R;

public class TutorialFragment extends android.support.v4.app.Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);

        ((TextView) view.findViewById(R.id.text)).setText(getArguments().getString("text"));
        ((ImageView) view.findViewById(R.id.image)).setImageResource(getArguments().getInt("image"));

        return view;
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