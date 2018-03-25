package by.nicolay.lipnevich.noxbox.pages;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.payer.massage.R;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.Task;

public class HistoryPage extends AppCompatActivity {

    private HistoryAdapter historyAdapter;
    private List<Noxbox> historyItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.history);
        setTitle(R.string.history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int size = 384;
        Glide.with(this).asGif()
                .load(R.drawable.progress_cat)
                .apply(RequestOptions.overrideOf(size, size))
                .into((ImageView) findViewById(R.id.progressView));

        ListView listView = (ListView) findViewById(R.id.listView);
        historyAdapter = new HistoryAdapter(HistoryPage.this, historyItems);
        listView.setAdapter(historyAdapter);

        loadHistory();
    }

    private void loadHistory() {
        Firebase.loadHistory(new Task<Collection<Noxbox>>() {
            @Override
            public void execute(Collection<Noxbox> noxboxes) {
                historyItems.addAll(noxboxes);
                Collections.sort(historyItems);
                historyAdapter.notifyDataSetChanged();

                ImageView progressView = (ImageView) findViewById(R.id.progressView);
                progressView.setVisibility(View.INVISIBLE);
                ListView listView = (ListView) findViewById(R.id.listView);
                listView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
