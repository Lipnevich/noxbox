package live.noxbox.menu.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import live.noxbox.R;
import live.noxbox.model.MarketRole;

import static live.noxbox.menu.history.HistoryActivity.KEY_COMPLETE;
import static live.noxbox.model.Noxbox.isNullOrZero;

public class HistorySupplyFragment extends Fragment {
    private LinearLayoutManager linearLayoutManager;
    private long lastNoxboxTimeCompleted;

    public HistorySupplyFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        if (getArguments() != null && !isNullOrZero(getArguments().getLong(KEY_COMPLETE))) {
            lastNoxboxTimeCompleted = getArguments().getLong(KEY_COMPLETE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        RecyclerView listItems = view.findViewById(R.id.recyclerView);
        listItems.setLayoutManager(linearLayoutManager);
        HistoryAdapter adapter = new HistoryAdapter((HistoryActivity) getActivity(), listItems, MarketRole.supply, lastNoxboxTimeCompleted);
        container.findViewById(R.id.chooseService);
        listItems.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return view;
    }
}

