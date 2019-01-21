package live.noxbox.menu.settings;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import live.noxbox.R;
import live.noxbox.activities.contract.NoxboxTypeListFragment;
import live.noxbox.database.AppCache;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Arrays.asList;

public class NoxboxTypeSelectionFragment extends DialogFragment {
    public static final int SETTINGS_CODE = 1013;
    public static final String TAG = NoxboxTypeListFragment.class.getName();

    private int key;
    private Integer totalChecked = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        key = getArguments().getInt("key");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_fragment_noxboxes_selection, container, false);

        AppCache.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                ((TextView)view.findViewById(R.id.title)).setText(getResources().getString(R.string.service).substring(0, 1).toUpperCase().concat(getResources().getString(R.string.service).substring(1)));

                boolean[] typesChecked = new boolean[NoxboxType.values().length];
                String[] typesName = new String[NoxboxType.values().length];

                for (NoxboxType type : NoxboxType.values()) {
                    typesName[type.getId()] = getResources().getString(type.getName());
                    typesChecked[type.getId()] = firstNonNull(profile.getFilters().getTypes().get(type.name()), true);
                    if (typesChecked[type.getId()]) {
                        totalChecked++;
                    }
                }

                NoxboxesSelectionAdapter adapter = new NoxboxesSelectionAdapter(asList(NoxboxType.values()), getActivity(), profile, typesChecked, totalChecked);
                RecyclerView noxboxTypeList = view.findViewById(R.id.listNoxboxes);
                noxboxTypeList.setHasFixedSize(true);
                noxboxTypeList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                noxboxTypeList.setAdapter(adapter);


                view.findViewById(R.id.positiveButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (NoxboxType type : NoxboxType.values()) {
                            profile.getFilters().getTypes().put(type.name(), typesChecked[type.getId()]);
                        }
                        if (totalChecked == 0) {
                            totalChecked++;
                            typesChecked[0] = true;
                            profile.getFilters().getTypes().put(NoxboxType.byId(0).name(), true);
                        }
                        dismiss();
                    }
                });

                view.findViewById(R.id.negativeButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (NoxboxType type : NoxboxType.values()) {
                            typesChecked[type.getId()] = firstNonNull(profile.getFilters().getTypes().get(type.name()), true);
                        }
                        dismiss();
                    }
                });

                view.findViewById(R.id.neutralButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (NoxboxType type : NoxboxType.values()) {
                            typesChecked[type.getId()] = true;
                            profile.getFilters().getTypes().put(type.name(), true);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        return view;
    }
}
