package by.nicolay.lipnevich.noxbox.pages;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.state.Firebase;
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.Task;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HistoryPage extends AppCompatActivity {

    public static final int CODE = 1002;

    private Activity activity;
    private HistoryAdapter historyAdapter;
    private List<Noxbox> historyItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
        setTitle(R.string.history);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int size = 384;
        Glide.with(this).asGif()
                .load(R.drawable.progress_cat)
                .apply(RequestOptions.overrideOf(size, size))
                .into((ImageView) findViewById(R.id.progressView));

        State.listenProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                loadHistory(profile);
            }
        });
    }

    private void loadHistory(Profile profile) {
        ListView listView = findViewById(R.id.historyView);
        historyAdapter = new HistoryAdapter(HistoryPage.this, profile.getId(), historyItems);
        listView.setAdapter(historyAdapter);

        Firebase.loadHistory(new Task<Collection<Noxbox>>() {
            @Override
            public void execute(Collection<Noxbox> noxboxes) {
                historyItems.addAll(noxboxes);
                Collections.sort(historyItems);
                historyAdapter.notifyDataSetChanged();

                ImageView progressView = findViewById(R.id.progressView);
                progressView.setVisibility(View.INVISIBLE);
                ListView listView = findViewById(R.id.historyView);
                listView.setVisibility(View.VISIBLE);
            }
        });
    }

}
