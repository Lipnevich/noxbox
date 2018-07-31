package by.nicolay.lipnevich.noxbox.constructor;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.state.ProfileStorage;
import by.nicolay.lipnevich.noxbox.tools.Task;

import java.util.Arrays;
import java.util.List;

public class NoxboxTypeListActivity extends ListActivity {
    private List<NoxboxType> typeList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        typeList = Arrays.asList(NoxboxType.values());
        ArrayAdapter<NoxboxType> itemArrayAdapter = new NoxboxTypeAdapter(this, typeList);
        setListAdapter(itemArrayAdapter);
        ProfileStorage.listenProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                draw(profile);
            }
        });
    }

    private void draw(final Profile profile) {
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                profile.getCurrent().setType(typeList.get(i));
                finish();
            }
        });
    }
}
