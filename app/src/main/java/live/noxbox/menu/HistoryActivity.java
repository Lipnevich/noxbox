package live.noxbox.menu;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.model.Noxbox;

public class HistoryActivity extends BaseActivity {

    public static final int CODE = 1002;
    private List<Noxbox> historyItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle(R.string.history);

//        int size = 384;
//        progressView = findViewById(R.id.progressView);
//        Glide.with(this).asGif()
//                .load(R.drawable.progress_cat)
//                .apply(RequestOptions.overrideOf(size, size))
//                .into(progressView);

        // TODO (vl) use MarketRole.demand for payer's activity_history
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter( new HistoryAdapter(this, historyItems, recyclerView));

    }

}
