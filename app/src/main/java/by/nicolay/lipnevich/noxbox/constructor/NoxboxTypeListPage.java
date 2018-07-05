package by.nicolay.lipnevich.noxbox.constructor;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.Arrays;
import java.util.List;

import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.state.State;

public class NoxboxTypeListPage extends ListActivity {
    private List<NoxboxType> typeList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        typeList = Arrays.asList(NoxboxType.values());
        ArrayAdapter<NoxboxType> itemArrayAdapter = new NoxboxTypeAdapterWithIcon(this, typeList);
        setListAdapter(itemArrayAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                State.getCurrentNoxbox().setType(typeList.get(i));
                finish();
            }
        });
    }
}
