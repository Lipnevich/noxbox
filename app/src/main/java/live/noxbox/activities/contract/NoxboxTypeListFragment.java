package live.noxbox.activities.contract;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import live.noxbox.R;
import live.noxbox.model.NoxboxType;

import static live.noxbox.database.AppCache.executeUITasks;
import static live.noxbox.database.AppCache.profile;

public class NoxboxTypeListFragment extends DialogFragment {
    public static final int MAP_CODE = 1010;
    public static final int CONTRACT_CODE = 1011;
    public static final int PROFILE_CODE = 1012;
    public static final String TAG = NoxboxTypeListFragment.class.getName();

    private int key;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        key = getArguments().getInt("key");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_fragment_noxbox_type_list, container, false);
        if (key == MAP_CODE) {
            view.findViewById(R.id.itemLayout).setVisibility(View.VISIBLE);
            ((ImageView) view.findViewById(R.id.itemLayout).findViewById(R.id.noxboxTypeImage)).setImageResource(R.drawable.noxbox);
            ((TextView) view.findViewById(R.id.itemLayout).findViewById(R.id.noxboxTypeName)).setText(R.string.showAll);
            view.findViewById(R.id.itemLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    executeAllInTheMap();
                }
            });
        } else {
            view.findViewById(R.id.itemLayout).setVisibility(View.GONE);
        }
        List<NoxboxType> noxboxTypes = new ArrayList<>(Arrays.asList(NoxboxType.values()));
        RecyclerView noxboxTypeList = view.findViewById(R.id.listOfServices);
        noxboxTypeList.setHasFixedSize(true);
        noxboxTypeList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        noxboxTypeList.setAdapter(new NoxboxTypeListAdapter(noxboxTypes, getActivity(), this, key));

        return view;
    }

    private void executeAllInTheMap(){
        for (NoxboxType type : NoxboxType.values()) {
            profile().getFilters().getTypes().put(type.name(), true);
        }
        dismiss();
        executeUITasks();
    }
}
