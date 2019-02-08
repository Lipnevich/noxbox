package live.noxbox.activities.contract;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Strings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.activities.detailed.CoordinateActivity;
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.cluster.ClusterAdapter;
import live.noxbox.cluster.NoxboxMarker;
import live.noxbox.database.AppCache;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxTime;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.states.State;
import live.noxbox.tools.AddressManager;
import live.noxbox.tools.BalanceCalculator;
import live.noxbox.tools.BottomSheetDialog;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.Router;

import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE_ON_PUBLISH;
import static live.noxbox.activities.contract.NoxboxTypeListFragment.CONTRACT_CODE;
import static live.noxbox.activities.detailed.CoordinateActivity.COORDINATE;
import static live.noxbox.activities.detailed.CoordinateActivity.LAT;
import static live.noxbox.activities.detailed.CoordinateActivity.LNG;
import static live.noxbox.analitics.BusinessEvent.contractOpening;
import static live.noxbox.database.AppCache.isProfileReady;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.database.AppCache.showPriceInUsd;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.model.TravelMode.none;
import static live.noxbox.tools.BalanceChecker.checkBalance;
import static live.noxbox.tools.BottomSheetDialog.openNameNotVerifySheetDialog;
import static live.noxbox.tools.BottomSheetDialog.openPhotoNotVerifySheetDialog;
import static live.noxbox.tools.LocationPermitOperator.getLocationPermission;
import static live.noxbox.tools.LocationPermitOperator.isLocationPermissionGranted;

public class ContractActivity extends BaseActivity {

    protected double price;
    private TextView closeOrRemove;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract);
        closeOrRemove = findViewById(R.id.closeOrRemove);
        ((TextView) findViewById(R.id.textCurrency)).setText(getString(R.string.currency));

        //если услуга была создана
        if (!profile().getNoxboxId().isEmpty() && !isNullOrZero(profile().getCurrent().getTimeCreated())) {
            profile().getBackup().copy(profile().getCurrent());
            profile().getCurrent().clean();
        } else {
            //если услуги нет
            profile().getBackup().clean();
            BusinessActivity.businessEvent(contractOpening);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.listenProfile(ContractActivity.class.getName(), profile -> {
            if (!isProfileReady()) return;
            draw(profile);
            checkBalance(profile, ContractActivity.this);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListen(ContractActivity.class.getName());
    }

    private void draw(@NonNull final Profile profile) {

        drawToolbar(profile);
        drawRole(profile);
        drawType(profile);
        drawTypeDescription(profile);
        drawTextPayment(profile);
        drawPrice(profile);
        drawTravelMode(profile);
        drawHost(profile);
        drawAddress(profile);
        drawNoxboxTimeSwitch(profile);
        drawCommentView(profile);
        drawButtons(profile);
    }

    private void drawToolbar(final Profile profile) {
        ((TextView) findViewById(R.id.title)).setText(R.string.contractService);
        findViewById(R.id.homeButton).setOnClickListener(v -> {
            if(profile.getBackup() != null && profile.getBackup().getTimeCreated() != 0){
                profile.getCurrent().copy(profile.getBackup());
            }
            Router.finishActivity(ContractActivity.this);
        });
    }

    private void drawRole(final Profile profile) {
        ((TextView) findViewById(R.id.textProfile)).setText(getString(R.string.i).concat(" ").concat(profile.getName()).concat(" ").concat(getResources().getString(R.string.want)).concat(" "));
        final TextView role = findViewById(R.id.textRole);
        SpannableStringBuilder spanTxt =
                new SpannableStringBuilder(getResources().getString(profile.getCurrent().getRole().getName()));
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                createRoleList(profile, role);
            }
        }, spanTxt.length() - getResources().getString(profile.getCurrent().getRole().getName()).length(), spanTxt.length(), 0);
        role.setMovementMethod(LinkMovementMethod.getInstance());
        role.setText(spanTxt, TextView.BufferType.SPANNABLE);
        findViewById(R.id.arrowRole).setOnClickListener(v -> createRoleList(profile, role));
    }

    private void drawType(Profile profile) {
        final TextView textView = findViewById(R.id.textNoxboxType);
        SpannableStringBuilder spanTxt =
                new SpannableStringBuilder(getResources().getString(profile.getCurrent().getType().getName()).toLowerCase());
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startDialogList();
            }
        }, spanTxt.length() - (getResources().getString(profile.getCurrent().getType().getName())).length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
        findViewById(R.id.arrowNoxboxType).setOnClickListener(v -> startDialogList());
    }

    private void drawTypeDescription(Profile profile) {
        ((TextView) findViewById(R.id.textTypeDescription)).setText(getResources().getString(profile.getCurrent().getType().getDescription()).concat("."));
    }

    private void drawTextPayment(Profile profile) {
        switch (profile.getCurrent().getType()) {
            case water:
                ((TextView) findViewById(R.id.textPayment)).setText(R.string.priceService);
                break;
            default:
                ((TextView) findViewById(R.id.textPayment)).setText(R.string.priceOneHourOfService);
        }

    }

    private TextWatcher changeCountOfMoneyListener;

    private void drawPrice(final Profile profile) {
        EditText priceInput = findViewById(R.id.inputPrice);
        if (changeCountOfMoneyListener == null) {
            changeCountOfMoneyListener = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String price = s.toString().replaceAll("\\.", ",");
                    profile.getCurrent().setPrice(price);
                    if (s.length() > 0) {
                        drawSimilarNoxboxList(profile);
                        ((TextView) findViewById(R.id.textCurrency))
                                .setText(showPriceInUsd(getString(R.string.currency),
                                        profile.getCurrent().getPrice()));
                    }
                }
            };
            priceInput.addTextChangedListener(changeCountOfMoneyListener);
        }
        priceInput.setText(profile.getCurrent().getPrice());

        ((TextView) findViewById(R.id.textCurrency))
                .setText(showPriceInUsd(getString(R.string.currency),
                        profile.getCurrent().getPrice()));
    }

    private void drawTravelMode(final Profile profile) {
        final TextView textView = findViewById(R.id.textTravelMode);
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(getResources().getString(profile.getCurrent().getOwner().getTravelMode().getName()));
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                createTravelModeList(profile, textView);
            }
        }, spanTxt.length() - getResources().getString(profile.getCurrent().getOwner().getTravelMode().getName()).length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
        findViewById(R.id.arrowTravelMode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTravelModeList(profile, textView);
            }
        });
    }

    private void drawHost(final Profile profile) {
        CheckBox checkBox = findViewById(R.id.isHost);
        //at least one option should be selected travel mode or host
        if (profile.getCurrent().getOwner().getTravelMode() == none) {
            checkBox.setChecked(true);
            checkBox.setEnabled(false);
            profile.getCurrent().getOwner().setHost(true);
            findViewById(R.id.or).setVisibility(View.GONE);
        } else {
            checkBox.setEnabled(true);
            checkBox.setChecked(profile.getCurrent().getOwner().getHost());
            findViewById(R.id.or).setVisibility(View.VISIBLE);
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                profile.getCurrent().getOwner().setHost(isChecked);
                drawAddress(profile);
            }
        });

    }

    private void drawAddress(Profile profile) {
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String address;
                if (profile.getCurrent().getOwner().getTravelMode() == none || profile.getCurrent().getOwner().getHost()) {
                    address = AddressManager.provideAddressByPosition(getApplicationContext(), profile.getCurrent().getPosition());
                } else {
                    address = AddressManager.provideAddressByPosition(getApplicationContext(), profile.getCurrent().getPosition()) + " " + getResources().getString(R.string.change);
                }

                return address;
            }

            @Override
            protected void onPostExecute(String address) {
                final TextView addressView = findViewById(R.id.textAddress);
                if (profile.getCurrent().getOwner().getTravelMode() == none || profile.getCurrent().getOwner().getHost()) {
                    SpannableStringBuilder spanTxt =
                            new SpannableStringBuilder(address);
                    spanTxt.append(" ");
                    spanTxt.append(getString(R.string.change));
                    spanTxt.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            ContractActivity.this.startActivityForResult(new Intent(ContractActivity.this, CoordinateActivity.class), COORDINATE);
                        }
                    }, spanTxt.length() - (getString(R.string.change).length()), spanTxt.length(), 0);
                    addressView.setMovementMethod(LinkMovementMethod.getInstance());
                    addressView.setText(spanTxt, TextView.BufferType.SPANNABLE);
                } else {
                    addressView.setText(address);
                }
            }
        }.execute();
    }

    private void startDialogList() {
        //TODO (vl) надо придумать как вызывать перерисовку(draw()) из списко во фрагменте
        DialogFragment dialog = new NoxboxTypeListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("key", CONTRACT_CODE);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), NoxboxTypeListFragment.TAG);
    }

    protected void createRoleList(final Profile profile, View textView) {
        final PopupMenu popup = new PopupMenu(ContractActivity.this, textView, Gravity.CENTER_HORIZONTAL);
        for (MarketRole role : MarketRole.values()) {
            popup.getMenu().add(Menu.NONE, role.getId(), Menu.NONE, role.getName());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                profile.getCurrent().setRole(MarketRole.byId(item.getItemId()));
                draw(profile);
                return true;
            }
        });

        popup.show();

    }

    private void createTravelModeList(final Profile profile, TextView textView) {
        final PopupMenu popup = new PopupMenu(ContractActivity.this, textView, Gravity.CENTER_HORIZONTAL);
        for (TravelMode mode : TravelMode.values()) {
            popup.getMenu().add(Menu.NONE, mode.getId(), Menu.NONE, mode.getName());
        }
        popup.setOnMenuItemClickListener(item -> {
            TravelMode travelMode = TravelMode.byId(item.getItemId());
            profile.getCurrent().getOwner().setTravelMode(travelMode);


            if (travelMode == none) {
                profile.getCurrent().getOwner().setHost(true);
                ((CheckBox) findViewById(R.id.isHost)).setChecked(true);
                ((CheckBox) findViewById(R.id.isHost)).setEnabled(false);
            } else {
                if (!isLocationPermissionGranted(getApplicationContext())) {
                    getLocationPermission(ContractActivity.this, LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
            draw(profile);
            return true;
        });
        popup.show();
    }

    private void drawNoxboxTimeSwitch(final Profile profile) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ContractActivity.this, R.layout.item_noxbox_time, NoxboxTime.getAllAsString());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner timeSelectFrom = findViewById(R.id.timeFromView);
        timeSelectFrom.setAdapter(adapter);
        timeSelectFrom.setSelection(profile.getCurrent().getWorkSchedule().getStartTime().getId());
        setHeightForDropdownList(timeSelectFrom);
        timeSelectFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                profile.getCurrent().getWorkSchedule().setStartTime(NoxboxTime.byId(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        Spinner timeSelectTo = findViewById(R.id.timeToView);
        timeSelectTo.setAdapter(adapter);
        timeSelectTo.setSelection(profile.getCurrent().getWorkSchedule().getEndTime().getId());
        setHeightForDropdownList(timeSelectTo);
        timeSelectTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                profile.getCurrent().getWorkSchedule().setEndTime(NoxboxTime.byId(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void drawCommentView(Profile profile) {
        EditText editComment = findViewById(R.id.editComment);
        if (!Strings.isNullOrEmpty(profile.getCurrent().getOwnerComment())) {
            editComment.setText(profile.getCurrent().getOwnerComment());
        }
        editComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                profile.getCurrent().setOwnerComment(s.toString());
            }
        });
    }

    private void drawButtons(final Profile profile) {

        drawCancelOrRemoveButton(profile);

        drawPublishButton(profile);
    }

    private void drawCancelOrRemoveButton(final Profile profile) {
        if (profile.getNoxboxId().isEmpty()) {
            closeOrRemove.setText(R.string.cancel);
            closeOrRemove.setOnClickListener(v -> cancelNoxbox());
        } else {
            closeOrRemove.setText(R.string.remove);
            closeOrRemove.setOnClickListener(v -> removeNoxbox());
        }
    }

    private void drawPublishButton(final Profile profile) {
        final LinearLayout publishButton = ((LinearLayout) findViewById(R.id.publish).getParent());
        if (isNullOrZero(profile.getBackup().getTimeCreated())) {
            ((TextView) findViewById(R.id.publish)).setText(R.string.add);
            publishButton.setOnClickListener(v -> {
                if (profile.getCurrent().getRole() == MarketRole.demand && !BalanceCalculator.enoughBalance(profile.getCurrent(), profile)) {
                    publishButton.setBackgroundColor(getResources().getColor(R.color.translucent));
                    BottomSheetDialog.openWalletAddressSheetDialog(ContractActivity.this, profile);
                    return;
                }
                if (!profile.getAcceptance().isAccepted()) {
                    openPhotoNotVerifySheetDialog(ContractActivity.this);
                    return;
                }
                if (profile.getName() != null && profile.getName().length() == 0) {
                    openNameNotVerifySheetDialog(ContractActivity.this, profile);
                    return;
                }

                if (profile.getCurrent().getOwner().getTravelMode() != none) {
                    if (isLocationPermissionGranted(getApplicationContext())) {
                        postNoxbox();
                    } else {
                        getLocationPermission(ContractActivity.this, LOCATION_PERMISSION_REQUEST_CODE_ON_PUBLISH);
                    }
                } else {
                    postNoxbox();
                }
            });
        } else {
            ((TextView) findViewById(R.id.publish)).setText(R.string.update);
            publishButton.setOnClickListener(v -> {
                if (profile.getCurrent().getRole() == MarketRole.demand && !BalanceCalculator.enoughBalance(profile.getCurrent(), profile)) {
                    publishButton.setBackgroundColor(getResources().getColor(R.color.translucent));
                    BottomSheetDialog.openWalletAddressSheetDialog(ContractActivity.this, profile);
                    return;
                }
                if (!profile.getAcceptance().isAccepted()) {
                    openPhotoNotVerifySheetDialog(ContractActivity.this);
                    return;
                }
                if (profile.getName() != null && profile.getName().length() == 0) {
                    openNameNotVerifySheetDialog(ContractActivity.this, profile);
                    return;
                }

                if (profile.getCurrent().getOwner().getTravelMode() != none) {
                    if (isLocationPermissionGranted(getApplicationContext())) {
                        updateNoxbox();
                    } else {
                        getLocationPermission(ContractActivity.this, LOCATION_PERMISSION_REQUEST_CODE_ON_PUBLISH);
                    }
                } else {
                    postNoxbox();
                }
            });

        }
    }


    private void setHeightForDropdownList(Spinner spinner) {
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinner);

            popupWindow.setHeight(500);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            Crashlytics.logException(e);
        }
    }

    public void postNoxbox() {
        Log.d(State.TAG + "ContractActivity", "timeCreated: " + DateTimeFormatter.time(System.currentTimeMillis()));



        AppCache.noxboxCreated(profile -> {
                    Router.finishActivity(ContractActivity.this);
                },
                object -> {
                    profile().getCurrent().clean();
                    Router.finishActivity(ContractActivity.this);
                });
    }

    public void updateNoxbox() {
        Log.d(State.TAG + "ContractActivity", "timeCreated: " + DateTimeFormatter.time(System.currentTimeMillis()));
        AppCache.removeNoxbox(profile -> profile.getBackup().clean());

        postNoxbox();
    }

    public void removeNoxbox() {
        profile().getCurrent().copy(profile().getBackup().setTimeRemoved(System.currentTimeMillis()));
        AppCache.removeNoxbox(profile -> profile.getCurrent().clean());
        Router.finishActivity(ContractActivity.this);
    }

    private void cancelNoxbox() {
        Router.finishActivity(ContractActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (!isLocationPermissionGranted(getApplicationContext())) {
                AppCache.readProfile(profile -> {
                    profile.getCurrent().getOwner().setTravelMode(none);
                    draw(profile);
                });
            }
        }


        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE_ON_PUBLISH) {
            if (isLocationPermissionGranted(getApplicationContext())) {
                postNoxbox();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COORDINATE && resultCode == RESULT_OK) {
            final Position position = new Position(data.getExtras().getDouble(LAT), data.getExtras().getDouble(LNG));
            AppCache.readProfile(profile -> profile.getCurrent().setPosition(position));
        }
    }


    private List<NoxboxMarker> noxboxes;
    private RecyclerView similarListViews;

    private void drawSimilarNoxboxList(Profile profile) {
        new AsyncTask<Void, Void, List<NoxboxMarker>>() {
            @Override
            protected List<NoxboxMarker> doInBackground(Void... voids) {
                List<NoxboxMarker> similarNoxboxes = new ArrayList<>();
                if (noxboxes == null) {
                    noxboxes = new ArrayList<>();
                    for (Noxbox item : AppCache.availableNoxboxes.values()) {
                        noxboxes.add(new NoxboxMarker(item.getPosition().toLatLng(), item));
                    }
                }

                for (NoxboxMarker item : noxboxes) {
                    //type
                    if (item.getNoxbox().getType() != profile.getCurrent().getType()) {
                        continue;
                    }

                    //role
                    if (item.getNoxbox().getRole() == profile.getCurrent().getRole()) {
                        continue;
                    }

                    //travelmode
                    if (profile.getCurrent().getOwner().getTravelMode() == none && item.getNoxbox().getOwner().getTravelMode() == none) {
                        continue;
                    }

                    //price
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        if (Double.parseDouble(profile.getCurrent().getPrice()) > Double.parseDouble(item.getNoxbox().getPrice())) {
                            continue;
                        }
                    } else {
                        if (Double.parseDouble(profile.getCurrent().getPrice()) < Double.parseDouble(item.getNoxbox().getPrice())) {
                            continue;
                        }
                    }

                    similarNoxboxes.add(item);
                }
                return similarNoxboxes;
            }

            @Override
            protected void onPostExecute(List<NoxboxMarker> similarNoxboxes) {
                if (similarNoxboxes.size() > 0) {
                    findViewById(R.id.buttonsRootLayout).setVisibility(View.GONE);
                    findViewById(R.id.similarNoxboxesLayout).setVisibility(View.VISIBLE);

                    similarListViews = findViewById(R.id.similarNoxboxesList);
                    similarListViews.setHasFixedSize(true);
                    similarListViews.setLayoutManager(new LinearLayoutManager(ContractActivity.this, LinearLayout.VERTICAL, false));
                    similarListViews.setAdapter(new ClusterAdapter(similarNoxboxes, ContractActivity.this, profile));
                } else {
                    findViewById(R.id.buttonsRootLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.similarNoxboxesLayout).setVisibility(View.GONE);
                }
            }
        }.execute();
    }

}