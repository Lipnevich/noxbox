package live.noxbox.menu;


import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import live.noxbox.BaseActivity;
import live.noxbox.R;
import live.noxbox.database.Firestore;
import live.noxbox.model.Noxbox;
import live.noxbox.tools.Task;

public class HistoryActivity extends BaseActivity {

    public static final int CODE = 1002;
    private static final int AMOUNT_PER_LOAD = 20;
    private int requestedAmount;

    private HistoryAdapter historyAdapter;
    private List<Noxbox> historyItems = new ArrayList<>();
    private ListView listView;
    private ImageView progressView;

    private Task<Collection<Noxbox>> uploadingTask = new Task<Collection<Noxbox>>() {
        @Override
        public void execute(Collection<Noxbox> items) {
            if(items.size() > AMOUNT_PER_LOAD) {
                requestedAmount = requestedAmount + items.size() - AMOUNT_PER_LOAD;
            }
            historyItems.addAll(items);
            progressView.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            historyAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
        setTitle(R.string.history);

        int size = 384;
        progressView = findViewById(R.id.progressView);
        Glide.with(this).asGif()
                .load(R.drawable.progress_cat)
                .apply(RequestOptions.overrideOf(size, size))
                .into(progressView);

        listView = findViewById(R.id.historyView);
        historyAdapter = new HistoryAdapter(HistoryActivity.this, historyItems);
        listView.setAdapter(historyAdapter);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem >= requestedAmount - visibleItemCount * 2) {
                    Firestore.readHistory(requestedAmount, AMOUNT_PER_LOAD, uploadingTask);
                    requestedAmount += AMOUNT_PER_LOAD;
                }
            }
        });
    }

}
