package live.noxbox.menu.about.tutorial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import live.noxbox.R;

public class TutorialFragment extends android.support.v4.app.Fragment {

    private static final String PAGE = "page";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);

        switchImage(view, getArguments().getInt(PAGE));
        switchText(view, getArguments().getInt(PAGE));

        return view;
    }

    private void switchText(View view, int page) {
        TextView text = view.findViewById(R.id.text);
        switch (page) {
            case 1: text.setText(getResources().getString(R.string.tutorial_one)); break;
            case 2: text.setText(getResources().getString(R.string.tutorial_two)); break;
            case 3: text.setText(getResources().getString(R.string.tutorial_three)); break;
            case 4: text.setText(getResources().getString(R.string.tutorial_four)); break;
        }
    }

    private void switchImage(View view, int page) {
        view.findViewById(R.id.tutorial_one).setVisibility(page == 1 ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.tutorial_two).setVisibility(page == 2 ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.tutorial_three).setVisibility(page == 3 ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.tutorial_four).setVisibility(page == 4 ? View.VISIBLE : View.GONE);
    }

    public static TutorialFragment newInstance(int page) {
        TutorialFragment tutorialFragment = new TutorialFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PAGE, page);

        tutorialFragment.setArguments(bundle);

        return tutorialFragment;
    }
}