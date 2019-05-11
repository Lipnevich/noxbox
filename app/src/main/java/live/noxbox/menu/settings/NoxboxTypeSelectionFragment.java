package live.noxbox.menu.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import live.noxbox.R;
import live.noxbox.model.NoxboxType;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Arrays.asList;
import static live.noxbox.database.AppCache.profile;

public class NoxboxTypeSelectionFragment extends DialogFragment {
    public static final int SETTINGS_CODE = 1013;
    public static final String TAG = NoxboxTypeSelectionFragment.class.getName();

    private boolean initiated = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_fragment_noxboxes_selection, container, false);

        ((TextView)view.findViewById(R.id.title)).setText(getResources().getString(R.string.service).substring(0, 1).toUpperCase().concat(getResources().getString(R.string.service).substring(1)));

        boolean[] typesChecked = new boolean[NoxboxType.values().length];

        for (NoxboxType type : NoxboxType.values()) {
            typesChecked[type.getId()] = firstNonNull(profile().getFilters().getTypes().get(type.name()), true);
        }

        NoxboxesSelectionAdapter adapter = new NoxboxesSelectionAdapter(asList(NoxboxType.values()), getActivity(), typesChecked);
        RecyclerView noxboxTypeList = view.findViewById(R.id.listNoxboxes);
        noxboxTypeList.setHasFixedSize(true);
        noxboxTypeList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        noxboxTypeList.setAdapter(adapter);


        view.findViewById(R.id.positiveButton).setOnClickListener(v -> {
            for (NoxboxType type : NoxboxType.values()) {
                profile().getFilters().getTypes().put(type.name(), typesChecked[type.getId()]);
            }
            if (!initiated) {
                typesChecked[0] = true;
                profile().getFilters().getTypes().put(NoxboxType.byId(0).name(), true);

                initiated = true;
            }
            dismiss();
        });

        view.findViewById(R.id.negativeButton).setOnClickListener(v -> {
            for (NoxboxType type : NoxboxType.values()) {
                typesChecked[type.getId()] = firstNonNull(profile().getFilters().getTypes().get(type.name()), true);
            }
            dismiss();
        });

        view.findViewById(R.id.neutralButton).setOnClickListener(v -> {
            for (NoxboxType type : NoxboxType.values()) {
                typesChecked[type.getId()] = true;
                profile().getFilters().getTypes().put(type.name(), true);
            }
            adapter.notifyDataSetChanged();
        });
        return view;
    }
}
