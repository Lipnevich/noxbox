package live.noxbox.state.cluster;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import live.noxbox.BaseActivity;
import live.noxbox.R;

public class ClusterItemsActivity extends BaseActivity {

    private RecyclerView recyclerView;
    public static final List<NoxboxMarker> noxboxes = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster_items);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerView.setAdapter(new ClusterAdapter(noxboxes,this));
    }


}
