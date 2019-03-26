package live.noxbox.activities.contract;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import live.noxbox.R;
import live.noxbox.model.TravelMode;

/**
 * Created by Vladislaw Kravchenok on 26.03.2019.
 */
public class TravelModeListFragment extends DialogFragment {
    public static final int CONTRACT_CODE = 1021;
    public static final int PROFILE_CODE = 1022;
    public static final String TAG = TravelModeListFragment.class.getName();

    private int key;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        key = getArguments().getInt("key");
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_fragment_travelmode_list, container, false);

        List<TravelMode> travelModes = new ArrayList<>(Arrays.asList(TravelMode.values()));
        RecyclerView travelModesList = view.findViewById(R.id.listOfTravelMode);
        travelModesList.setHasFixedSize(true);
        travelModesList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        travelModesList.setAdapter(new TravelModeListAdapter(travelModes, getActivity(), this, key));

        return view;
    }
}
