package live.noxbox.cluster;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxType;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

import static java.util.Collections.sort;

public class ClusterItemsActivity extends BaseActivity {

    private RecyclerView supplyList;
    private RecyclerView demandList;
    public static final Set<NoxboxMarker> noxboxes = new HashSet<>();
    private List<NoxboxMarker> supplyNoxboxes = new ArrayList<>();
    private List<NoxboxMarker> demandNoxboxes = new ArrayList<>();
    Map<NoxboxType, String> types = new HashMap<>();

    private TextView supplyTitle;
    private TextView demandTitle;
    private ImageView homeButton;
    private ImageView sort;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster_items);

        initializeUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.listenProfile(this.getClass().getName(), profile -> draw());
        AppCache.startListenAvailableNoxboxes(ClusterItemsActivity.class.toString(), new Task<Map<String, Noxbox>>() {
            @Override
            public void execute(Map<String, Noxbox> object) {
                draw();
            }
        });
}

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListenAvailableNoxboxes(ClusterItemsActivity.class.toString());
    }

    private void initializeUi() {
        supplyTitle = findViewById(R.id.supplyTitle);
        demandTitle = findViewById(R.id.demandTitle);
        homeButton = findViewById(R.id.homeButton);
        sort = findViewById(R.id.sort);
        demandNoxboxes.clear();
        supplyNoxboxes.clear();

        for(NoxboxType type : NoxboxType.values()) {
            types.put(type, getString(type.getName()));
        }
    }

    private void draw() {
        separationNoxboxesByRole();
        initClusterItemsLists();
        updateClusterItemsList();
        supplyTitle.setVisibility(supplyNoxboxes.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        demandTitle.setVisibility(demandNoxboxes.isEmpty() ? View.INVISIBLE : View.VISIBLE);

        homeButton.setOnClickListener(v -> Router.finishActivity(ClusterItemsActivity.this));
        sort.setOnClickListener(o -> showSettings());
    }


    private void separationNoxboxesByRole() {
        for (NoxboxMarker noxboxMarker : noxboxes) {
            if (noxboxMarker.getNoxbox().getRole() == MarketRole.supply
                    && !supplyNoxboxes.contains(noxboxMarker)) {
                supplyNoxboxes.add(noxboxMarker);
            } else if(noxboxMarker.getNoxbox().getRole() == MarketRole.demand
                    && !demandNoxboxes.contains(noxboxMarker)) {
                demandNoxboxes.add(noxboxMarker);
            }
        }
    }

    private void initClusterItemsLists() {
        supplyList = findViewById(R.id.supplyList);
        supplyList.setHasFixedSize(true);
        supplyList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        demandList = findViewById(R.id.demandList);
        demandList.setHasFixedSize(true);
        demandList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

    }

    private void updateClusterItemsList() {

        supplyList.setAdapter(new ClusterAdapter(supplyNoxboxes, this));
        demandList.setAdapter(new ClusterAdapter(demandNoxboxes, this));

    }

    private void showSettings() {
        final PopupMenu popupMenu = new PopupMenu(ClusterItemsActivity.this, findViewById(R.id.sort));
        popupMenu.getMenuInflater().inflate(R.menu.sort_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.type: {
                    executeSortByTypeInAlphabeticalOrder();
                    break;
                }
                case R.id.price: {
                    executeSortByPrice();
                    break;
                }
                case R.id.rating: {
                    executeSortByRating();
                    break;
                }
            }
            updateClusterItemsList();
            return true;
        });

        popupMenu.show();
    }

    private Comparator<NoxboxMarker> typeComparator = (first, second) ->
            types.get(first.getNoxbox().getType()).compareTo(types.get(second.getNoxbox().getType()));

    private void executeSortByTypeInAlphabeticalOrder() {
        sort(supplyNoxboxes, typeComparator);
        sort(demandNoxboxes, typeComparator);
    }

    private Comparator<NoxboxMarker> priceComparator = (first, second) ->
            first.getNoxbox().getPrice().compareTo(second.getNoxbox().getPrice());

    private void executeSortByPrice() {
        sort(supplyNoxboxes, priceComparator);
        sort(demandNoxboxes, priceComparator);
    }

    private Comparator<NoxboxMarker> ratingComparator = (first, second) ->
            first.getNoxbox().getOwner().getRatings().ratingToPercentage()
                    .compareTo(second.getNoxbox().getOwner().getRatings().ratingToPercentage());

    private void executeSortByRating() {
        sort(supplyNoxboxes, ratingComparator);
        sort(demandNoxboxes, ratingComparator);
    }

}
