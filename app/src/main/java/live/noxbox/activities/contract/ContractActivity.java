package live.noxbox.activities.contract;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Strings;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.activities.detailed.CoordinateActivity;
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.analitics.BusinessEvent;
import live.noxbox.cluster.ClusterAdapter;
import live.noxbox.cluster.NoxboxMarker;
import live.noxbox.database.AppCache;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxTime;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.tools.AddressManager;
import live.noxbox.tools.BalanceCalculator;
import live.noxbox.tools.BottomSheetDialog;
import live.noxbox.tools.Router;

import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE_ON_PUBLISH;
import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE_ON_UPDATE;
import static live.noxbox.Constants.MINIMUM_PRICE;
import static live.noxbox.activities.contract.NoxboxTypeListFragment.CONTRACT_CODE;
import static live.noxbox.activities.detailed.CoordinateActivity.COORDINATE;
import static live.noxbox.activities.detailed.CoordinateActivity.LAT;
import static live.noxbox.activities.detailed.CoordinateActivity.LNG;
import static live.noxbox.analitics.BusinessEvent.contractOpening;
import static live.noxbox.database.AppCache.NONE;
import static live.noxbox.database.AppCache.executeUITasks;
import static live.noxbox.database.AppCache.isProfileReady;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.database.AppCache.showPriceInUsd;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.model.TravelMode.none;
import static live.noxbox.tools.BalanceChecker.checkBalance;
import static live.noxbox.tools.BottomSheetDialog.openNameNotVerifySheetDialog;
import static live.noxbox.tools.BottomSheetDialog.openPhotoNotVerifySheetDialog;
import static live.noxbox.tools.DialogBuilder.createMessageAlertDialog;
import static live.noxbox.tools.LocationOperator.getLocationPermission;
import static live.noxbox.tools.LocationOperator.isLocationPermissionGranted;

public class ContractActivity extends BaseActivity {

    private DialogFragment noxboxTypeListFragment;
    private TextView title;
    private ImageView homeButton;
    private TextView role;
    private TextView noxboxType;
    private TextInputLayout inputLayout;
    private EditText inputPrice;
    private TextWatcher changePriceListener;
    private TextView textCurrency;
    private TextView textTravelMode;
    private TextView addressView;

    private CheckBox host;
    private EditText comment;
    private TextView closeOrRemove;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract);

        title = findViewById(R.id.title);
        homeButton = findViewById(R.id.homeButton);
        role = findViewById(R.id.textRole);
        noxboxType = findViewById(R.id.textNoxboxType);
        inputLayout = findViewById(R.id.textInputLayout);
        inputPrice = findViewById(R.id.inputPrice);
        textCurrency = findViewById(R.id.textCurrency);
        textTravelMode = findViewById(R.id.textTravelMode);
        host = findViewById(R.id.host);
        addressView = findViewById(R.id.textAddress);
        comment = findViewById(R.id.comment);
        closeOrRemove = findViewById(R.id.closeOrRemove);

        checkBalance(profile(), this);

        contract().copy(profile().getCurrent());
        BusinessActivity.businessEvent(contractOpening);
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

    private Noxbox contract() {
        return profile().getContract();
    }

    private void draw(@NonNull final Profile profile) {
        drawToolbar();
        drawRole(profile);
        drawType();
        drawTypeDescription();
        drawTextPayment();
        drawPrice(profile);
        drawTravelMode(profile);
        drawHost(profile);
        drawAddress(profile);
        drawTimeSwitch();
        drawCommentView();
        drawButtons(profile);
    }

    private void drawToolbar() {
        title.setText(R.string.contractService);
        homeButton.setOnClickListener(v -> Router.finishActivity(ContractActivity.this));
    }

    private void drawRole(final Profile profile) {
        ((TextView) findViewById(R.id.textProfile)).setText(getString(R.string.i).concat(" ").concat(profile.getName()).concat(" ").concat(getResources().getString(R.string.want)).concat(" "));

        SpannableStringBuilder spanTxt =
                new SpannableStringBuilder(getResources().getString(contract().getRole().getName()));
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                createRoleList(profile, role);
            }
        }, spanTxt.length() - getResources().getString(contract().getRole().getName()).length(), spanTxt.length(), 0);
        role.setMovementMethod(LinkMovementMethod.getInstance());
        role.setText(spanTxt, TextView.BufferType.SPANNABLE);
        findViewById(R.id.arrowRole).setOnClickListener(v -> createRoleList(profile, role));
    }

    private void drawType() {
        SpannableStringBuilder spanTxt =
                new SpannableStringBuilder(getResources().getString(contract().getType().getName()).toLowerCase());
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startDialogList();
            }
        }, spanTxt.length() - (getResources().getString(contract().getType().getName())).length(), spanTxt.length(), 0);
        noxboxType.setMovementMethod(LinkMovementMethod.getInstance());
        noxboxType.setText(spanTxt, TextView.BufferType.SPANNABLE);
        findViewById(R.id.arrowNoxboxType).setOnClickListener(v -> startDialogList());
    }

    private void drawTypeDescription() {
        ((TextView) findViewById(R.id.textTypeDescription)).setText(getResources().getString(contract().getType().getDescription()).concat("."));
    }

    private void drawTextPayment() {
        switch (contract().getType()) {
            case water:
                ((TextView) findViewById(R.id.textPayment)).setText(R.string.priceService);
                break;
            default:
                ((TextView) findViewById(R.id.textPayment)).setText(R.string.priceOneHourOfService);
        }

    }


    private void drawPrice(final Profile profile) {
        if (changePriceListener == null) {
            changePriceListener = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String price = s.toString().replaceAll(",", "\\.");
                    contract().setPrice(price);


                    if (s.length() > 0) {
                        drawSimilarNoxboxList(profile);
                        inputLayout.setErrorEnabled(false);
                        textCurrency.setText(showPriceInUsd(getString(R.string.currency), contract().getPrice()));
                    } else {
                        inputLayout.requestFocus();
                        inputLayout.setErrorEnabled(true);
                        inputLayout.setError("* " + getString(R.string.emptyPriceErrorMessage));
                        textCurrency.setText(getString(R.string.currency));
                    }
                }
            };
            inputPrice.addTextChangedListener(changePriceListener);
        }
        inputPrice.setText(contract().getPrice());

        if (!Strings.isNullOrEmpty(contract().getPrice())) {
            inputLayout.setErrorEnabled(false);
            textCurrency.setText(showPriceInUsd(getString(R.string.currency),
                    contract().getPrice()));
        }


    }

    private void getMinimumPriceError() {
        createMessageAlertDialog(this, R.string.emptyPriceErrorMessage, (dialog, which) -> {
            contract().setPrice(MINIMUM_PRICE);
            inputPrice.setText(MINIMUM_PRICE);
            dialog.dismiss();
            inputLayout.setErrorEnabled(false);
            inputLayout.requestFocus();
        });

    }

    private void drawTravelMode(final Profile profile) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(getResources().getString(contract().getOwner().getTravelMode().getName()));
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                createTravelModeList(profile, textTravelMode);
            }
        }, spanTxt.length() - getResources().getString(contract().getOwner().getTravelMode().getName()).length(), spanTxt.length(), 0);
        textTravelMode.setMovementMethod(LinkMovementMethod.getInstance());
        textTravelMode.setText(spanTxt, TextView.BufferType.SPANNABLE);
        findViewById(R.id.arrowTravelMode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTravelModeList(profile, textTravelMode);
            }
        });
    }

    private void drawHost(final Profile profile) {
        //at least one option should be selected travel mode or host
        if (contract().getOwner().getTravelMode() == none) {
            host.setChecked(true);
            host.setEnabled(false);
            contract().getOwner().setHost(true);
            findViewById(R.id.or).setVisibility(View.GONE);
        } else {
            host.setEnabled(true);
            host.setChecked(contract().getOwner().getHost());
            findViewById(R.id.or).setVisibility(View.VISIBLE);
        }

        host.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contract().getOwner().setHost(isChecked);
            drawAddress(profile);
        });

    }

    private void drawAddress(Profile profile) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String address;
                if (contract().getOwner().getTravelMode() == none || contract().getOwner().getHost()) {
                    address = AddressManager.provideAddressByPosition(getApplicationContext(), contract().getPosition());
                } else {
                    address = AddressManager.provideAddressByPosition(getApplicationContext(), contract().getPosition()) + " " + getResources().getString(R.string.change);
                }

                return address;
            }

            @Override
            protected void onPostExecute(String address) {
                if (contract().getOwner().getTravelMode() == none || contract().getOwner().getHost()) {
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
        if (noxboxTypeListFragment == null || !noxboxTypeListFragment.isVisible()) {
            noxboxTypeListFragment = new NoxboxTypeListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("key", CONTRACT_CODE);
            noxboxTypeListFragment.setArguments(bundle);
            noxboxTypeListFragment.show(getSupportFragmentManager(), NoxboxTypeListFragment.TAG);
        }
    }

    private void createRoleList(final Profile profile, View textView) {
        final PopupMenu popup = new PopupMenu(ContractActivity.this, textView, Gravity.CENTER_HORIZONTAL);
        for (MarketRole role : MarketRole.values()) {
            popup.getMenu().add(Menu.NONE, role.getId(), Menu.NONE, role.getName());
        }
        popup.setOnMenuItemClickListener(item -> {
            contract().setRole(MarketRole.byId(item.getItemId()));
            draw(profile);
            return true;
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
            contract().getOwner().setTravelMode(travelMode);


            if (travelMode == none) {
                contract().getOwner().setHost(true);
                host.setChecked(true);
                host.setEnabled(false);
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

    private void drawTimeSwitch() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ContractActivity.this, R.layout.item_noxbox_time, NoxboxTime.getAllAsString());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner timeSelectFrom = findViewById(R.id.timeFromView);
        timeSelectFrom.setAdapter(adapter);
        timeSelectFrom.setSelection(contract().getWorkSchedule().getStartTime().getId());
        setHeightForDropdownList(timeSelectFrom);
        timeSelectFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                contract().getWorkSchedule().setStartTime(NoxboxTime.byId(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        Spinner timeSelectTo = findViewById(R.id.timeToView);
        timeSelectTo.setAdapter(adapter);
        timeSelectTo.setSelection(contract().getWorkSchedule().getEndTime().getId());
        setHeightForDropdownList(timeSelectTo);
        timeSelectTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                contract().getWorkSchedule().setEndTime(NoxboxTime.byId(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void drawCommentView() {
        if (!Strings.isNullOrEmpty(contract().getContractComment())) {
            comment.setText(contract().getContractComment());
        }
        comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                contract().setContractComment(s.toString());
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
        //TODO (vl) запретить размещать услуги для пользователей у которых низкий рейтинг выбранной услуги
        final LinearLayout publishButton = ((LinearLayout) findViewById(R.id.publish).getParent());
        if (isNullOrZero(contract().getTimeCreated())) {
            ((TextView) findViewById(R.id.publish)).setText(R.string.post);
            publishButton.setOnClickListener(v -> {
                if (Strings.isNullOrEmpty(contract().getPrice())) {
                    getMinimumPriceError();
                    return;
                }

                if (contract().getRole() == MarketRole.demand && !BalanceCalculator.enoughBalance(contract(), profile)) {
                    BottomSheetDialog.openWalletAddressSheetDialog(ContractActivity.this, profile);
                    return;
                }
                if (!profile.getAcceptance().isAccepted()) {
                    openPhotoNotVerifySheetDialog(ContractActivity.this);
                    return;
                }
                if (profile.getName() != null && profile.getName().length() == 0) {
                    openNameNotVerifySheetDialog(ContractActivity.this);
                    return;
                }

                if (contract().getOwner().getTravelMode() != none) {
                    if (isLocationPermissionGranted(getApplicationContext())) {
                        createNoxbox();
                    } else {
                        getLocationPermission(ContractActivity.this, LOCATION_PERMISSION_REQUEST_CODE_ON_PUBLISH);
                    }
                } else {
                    createNoxbox();
                }
            });
        } else {
            ((TextView) findViewById(R.id.publish)).setText(R.string.update);
            publishButton.setOnClickListener(v -> {
                if (contract().getRole() == MarketRole.demand && !BalanceCalculator.enoughBalance(contract(), profile)) {
                    publishButton.setBackgroundColor(getResources().getColor(R.color.translucent));
                    BottomSheetDialog.openWalletAddressSheetDialog(ContractActivity.this, profile);
                    return;
                }
                if (!profile.getAcceptance().isAccepted()) {
                    openPhotoNotVerifySheetDialog(ContractActivity.this);
                    return;
                }
                if (profile.getName() != null && profile.getName().length() == 0) {
                    openNameNotVerifySheetDialog(ContractActivity.this);
                    return;
                }

                if (contract().getOwner().getTravelMode() != none) {
                    if (isLocationPermissionGranted(getApplicationContext())) {
                        updateNoxbox();
                    } else {
                        getLocationPermission(ContractActivity.this, LOCATION_PERMISSION_REQUEST_CODE_ON_UPDATE);
                    }
                } else {
                    updateNoxbox();
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

    public void createNoxbox() {
        AppCache.createNoxbox(success -> BusinessActivity.businessEvent(BusinessEvent.post), NONE);
        Router.finishActivity(ContractActivity.this);
    }

    public void updateNoxbox() {
        Router.finishActivity(ContractActivity.this);
        AppCache.removeNoxbox(removed ->
                AppCache.createNoxbox(created ->
                        BusinessActivity.businessEvent(BusinessEvent.update), NONE));
    }

    public void removeNoxbox() {
        Router.finishActivity(ContractActivity.this);
        AppCache.removeNoxbox(profile -> {
            profile.getCurrent().clean();
            profile.getContract().clean();
            executeUITasks();
        });
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
                    contract().getOwner().setTravelMode(none);
                    draw(profile);
                });
            }
        }

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE_ON_PUBLISH
                && isLocationPermissionGranted(getApplicationContext()))
            createNoxbox();


        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE_ON_UPDATE
                && isLocationPermissionGranted(getApplicationContext()))
            updateNoxbox();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COORDINATE && resultCode == RESULT_OK) {
            final Position position = new Position(data.getExtras().getDouble(LAT), data.getExtras().getDouble(LNG));
            AppCache.readProfile(profile -> contract().setPosition(position));
        }
    }


    private List<NoxboxMarker> noxboxes;
    private RecyclerView similarNoxboxesList;

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
                    if (item.getNoxbox().getType() != contract().getType()) {
                        continue;
                    }

                    //role
                    if (item.getNoxbox().getRole() == contract().getRole()) {
                        continue;
                    }

                    //travelmode
                    if (contract().getOwner().getTravelMode() == none && item.getNoxbox().getOwner().getTravelMode() == none) {
                        continue;
                    }

                    //price
                    if (contract().getRole() == MarketRole.supply) {
                        if (new BigDecimal(contract().getPrice())
                                .compareTo(new BigDecimal(item.getNoxbox().getPrice())) > 0) {
                            continue;
                        }
                    } else {
                        if (new BigDecimal(contract().getPrice())
                                .compareTo(new BigDecimal(item.getNoxbox().getPrice())) < 0) {
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

                    similarNoxboxesList = findViewById(R.id.similarNoxboxesList);
                    similarNoxboxesList.setHasFixedSize(true);
                    similarNoxboxesList.setLayoutManager(new LinearLayoutManager(ContractActivity.this, LinearLayout.VERTICAL, false));
                    similarNoxboxesList.setAdapter(new ClusterAdapter(similarNoxboxes, ContractActivity.this, profile));
                } else {
                    findViewById(R.id.buttonsRootLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.similarNoxboxesLayout).setVisibility(View.GONE);
                }
            }
        }.execute();
    }

}