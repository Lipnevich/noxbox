package by.nicolay.lipnevich.noxbox.pages;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

import by.nicolay.lipnevich.noxbox.payer.tirefitting.Car;
import by.nicolay.lipnevich.noxbox.payer.tirefitting.TireFittingProfile;
import by.nicolay.lipnevich.noxbox.payer.massage.R;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.updateProfile;
import static java.util.Collections.singletonList;

public class MyCarPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mycar);
        if(((TireFittingProfile) getProfile()).getCars().isEmpty()) {
            createCar(new Car().setCurrent(true)
                    .setManufacturer(getManufacturers().get(0))
                    .setModel(getModels().get(0))
                    .setSize(getSizes().get(0)));
        }

        final Spinner manufacturerCar = (Spinner) findViewById(R.id.manufacturerCar);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item,
                getManufacturers());
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        manufacturerCar.setAdapter(adapter);

        final Spinner modelCar = (Spinner) findViewById(R.id.modelCar);
        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, getModels());
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        modelCar.setAdapter(adapter);

        final Spinner sizeWeelCar = (Spinner) findViewById(R.id.sizeWeelCar);
        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, getSizes());
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sizeWeelCar.setAdapter(adapter);

        manufacturerCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!manufacturerCar.getSelectedItem().toString().equals(getCar().getManufacturer())) {
                    getCar().setManufacturer(((Spinner)parent).getAdapter().getItem(position).toString());
                } else {
                    return;
                }
                updateCar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        modelCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!modelCar.getSelectedItem().toString().equals(getCar().getModel())) {
                    getCar().setModel(((Spinner)parent).getAdapter().getItem(position).toString());
                } else {
                    return;
                }
                updateCar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        sizeWeelCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(!sizeWeelCar.getSelectedItem().toString().equals(getCar().getSize())) {
                        getCar().setSize(((Spinner)parent).getAdapter().getItem(position).toString());
                    } else {
                        return;
                    }
                    updateCar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        manufacturerCar.setSelection(getSelection(getManufacturers(), getCar().getManufacturer()));
        modelCar.setSelection(getSelection(getModels(), getCar().getModel()));
        sizeWeelCar.setSelection(getSelection(getSizes(), getCar().getSize().toString()));
        setupActionBar();
    }

    private void updateCar() {
        updateProfile(new TireFittingProfile().setCars(singletonList(getCar()))
                .setId(getProfile().getId()));
    }

    private void createCar(Car car) {
        ((TireFittingProfile) getProfile()).getCars().add(car);
        updateCar();
    }

    private Car getCar() {
        return ((TireFittingProfile) getProfile()).getCars().get(0);
    }

    private List<String> getSizes() {
        return Arrays.asList("19", "21", "100");
    }

    private List<String> getModels() {
        return Arrays.asList("X5", "X6", "X119");
    }

    private int getSelection(List<String> items, String selected) {
        int i = 0;
        for (String item : items) {
            if(item.equals(selected)) {
                break;
            }
            i++;
        }
        return i;
    }

    private List<String> getManufacturers() {
        return Arrays.asList("Bmw", "Audi", "Ferrari");
    }

    private void setupActionBar() {
        setTitle(R.string.my_car);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
