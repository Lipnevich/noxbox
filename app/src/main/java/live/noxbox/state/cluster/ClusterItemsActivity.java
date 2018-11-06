package live.noxbox.state.cluster;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import live.noxbox.BaseActivity;
import live.noxbox.R;
import live.noxbox.model.MarketRole;

public class ClusterItemsActivity extends BaseActivity {

    private RecyclerView supplyList;
    private RecyclerView demandList;
    public static final List<NoxboxMarker> noxboxes = new ArrayList<>();
    private List<NoxboxMarker> supplyNoxboxes;
    private List<NoxboxMarker> demandNoxboxes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster_items);

    }


    @Override
    protected void onResume() {
        super.onResume();
        draw();


    }

    private void draw() {
        ((TextView) findViewById(R.id.supplyTitle)).setText("Предложение");
        ((TextView) findViewById(R.id.demandTitle)).setText("Спрос");
        separationNoxboxesByRole();
        initClusterItemsLists();
        updateClusterItemsList();
        findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.sort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettings();
                //showSortDialog();
            }
        });
    }


    private void separationNoxboxesByRole() {
        for (NoxboxMarker noxboxMarker : noxboxes) {
            if (noxboxMarker.getNoxbox().getRole() == MarketRole.supply) {
                if (supplyNoxboxes == null) {
                    supplyNoxboxes = new ArrayList<>();
                }
                supplyNoxboxes.add(noxboxMarker);
            } else {
                if (demandNoxboxes == null) {
                    demandNoxboxes = new ArrayList<>();
                }
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
        if (supplyNoxboxes != null) {
            supplyList.setAdapter(new ClusterAdapter(supplyNoxboxes, this));
        }

        if (demandNoxboxes != null) {
            demandList.setAdapter(new ClusterAdapter(demandNoxboxes, this));
        }

    }

    private void showSettings() {
        PopupMenu popupMenu = new PopupMenu(ClusterItemsActivity.this, findViewById(R.id.sort));
        popupMenu.getMenuInflater().inflate(R.menu.sort_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
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
            }
        });

        popupMenu.show();
    }

    private void showSortDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ClusterItemsActivity.this);
        final View view = getLayoutInflater().inflate(R.layout.dialog_sort_cluster_items, null);

        RadioButton sortByType = view.findViewById(R.id.type);
        RadioButton sortByPrice = view.findViewById(R.id.price);
        RadioButton sortByRating = view.findViewById(R.id.rating);

        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        sortByType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeSortByTypeInAlphabeticalOrder();
                updateClusterItemsList();
                alertDialog.cancel();
            }
        });
        sortByPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeSortByPrice();
                updateClusterItemsList();
                alertDialog.cancel();
            }
        });
        sortByRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeSortByRating();
                updateClusterItemsList();
                alertDialog.cancel();
            }
        });


        alertDialog.show();

    }

    private void executeSortByTypeInAlphabeticalOrder() {
        if (supplyNoxboxes != null) {
            Collections.sort(supplyNoxboxes, new Comparator<NoxboxMarker>() {
                @Override
                public int compare(NoxboxMarker o1, NoxboxMarker o2) {
                    String name1 = getResources().getString(o1.getNoxbox().getType().getName());
                    String name2 = getResources().getString(o2.getNoxbox().getType().getName());
                    return name1.compareTo(name2);
                }
            });
        }

        if (demandNoxboxes != null) {
            Collections.sort(demandNoxboxes, new Comparator<NoxboxMarker>() {
                @Override
                public int compare(NoxboxMarker o1, NoxboxMarker o2) {
                    String name1 = getResources().getString(o1.getNoxbox().getType().getName());
                    String name2 = getResources().getString(o2.getNoxbox().getType().getName());
                    return name1.compareTo(name2);
                }
            });
        }
    }

    private void executeSortByPrice() {
        if (supplyNoxboxes != null) {
            Collections.sort(supplyNoxboxes, new Comparator<NoxboxMarker>() {
                @Override
                public int compare(NoxboxMarker o1, NoxboxMarker o2) {
                    return o1.getNoxbox().getPrice().compareTo(o2.getNoxbox().getPrice());
                }
            });
        }
        if (demandNoxboxes != null) {
            Collections.sort(demandNoxboxes, new Comparator<NoxboxMarker>() {
                @Override
                public int compare(NoxboxMarker o1, NoxboxMarker o2) {
                    return o1.getNoxbox().getPrice().compareTo(o2.getNoxbox().getPrice());
                }
            });
        }

    }

    private void executeSortByRating() {
        if (supplyNoxboxes != null) {
            Collections.sort(supplyNoxboxes, new Comparator<NoxboxMarker>() {
                @Override
                public int compare(NoxboxMarker o1, NoxboxMarker o2) {
                    Integer rating1 = o1.getNoxbox().getOwner().ratingToPercentage();
                    Integer rating2 = o2.getNoxbox().getOwner().ratingToPercentage();
                    return rating2.compareTo(rating1);
                }
            });
        }
        if (demandNoxboxes != null) {
            Collections.sort(demandNoxboxes, new Comparator<NoxboxMarker>() {
                @Override
                public int compare(NoxboxMarker o1, NoxboxMarker o2) {
                    Integer rating1 = o1.getNoxbox().getOwner().ratingToPercentage();
                    Integer rating2 = o2.getNoxbox().getOwner().ratingToPercentage();
                    return rating2.compareTo(rating1);
                }
            });
        }
    }

}
