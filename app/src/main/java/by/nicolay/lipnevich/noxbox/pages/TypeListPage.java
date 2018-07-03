package by.nicolay.lipnevich.noxbox.pages;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.List;

import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.tools.TypeArrayAdapterWithIcon;

public class TypeListPage extends ListActivity {
    private List<NoxboxType> typeList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        typeList = NoxboxType.getAll();
        ArrayAdapter<NoxboxType> itemArrayAdapter = new TypeArrayAdapterWithIcon(this, typeList);
        setListAdapter(itemArrayAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NoxboxType type = typeList.get(i);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("typeName", getResources().getString(type.getName()));
                setResult(1,returnIntent);
                finish();
            }
        });
    }
}
