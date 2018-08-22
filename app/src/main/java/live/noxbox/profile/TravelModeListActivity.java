package live.noxbox.profile;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.Arrays;
import java.util.List;

import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.Task;

public class TravelModeListActivity extends ListActivity {
    public static final int CODE = 1008;
    private List<TravelMode> travelModeList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        travelModeList = Arrays.asList(TravelMode.values());
        ArrayAdapter<TravelMode> itemArrayAdapter = new TravelModeAdapter(this, travelModeList);
        setListAdapter(itemArrayAdapter);

        int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = this.getWindow().getDecorView().findViewById(titleDividerId);
        titleDivider.setBackgroundColor(getResources().getColor(R.color.primary));

        ProfileStorage.readProfile(new Task<Profile>() {
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

                profile.setTravelMode(travelModeList.get(i));

                if (profile.getTravelMode() == TravelMode.none) {
                    profile.setHost(true);
                }
                finish();
            }
        });
    }
}