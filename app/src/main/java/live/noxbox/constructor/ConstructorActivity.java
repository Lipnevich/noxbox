package live.noxbox.constructor;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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

import java.lang.reflect.Field;

import live.noxbox.BaseActivity;
import live.noxbox.Configuration;
import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.detailed.CoordinateActivity;
import live.noxbox.model.MarketRole;
import live.noxbox.model.NoxboxTime;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.GeoRealtime;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.AddressManager;
import live.noxbox.tools.BalanceCalculator;
import live.noxbox.tools.BottomSheetDialog;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.detailed.CoordinateActivity.COORDINATE;
import static live.noxbox.detailed.CoordinateActivity.LAT;
import static live.noxbox.detailed.CoordinateActivity.LNG;

public class ConstructorActivity extends BaseActivity {

    protected double price;
    private TextView closeOrRemove;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constructor);
        closeOrRemove = findViewById(R.id.closeOrRemove);
        TextView textCurrency = findViewById(R.id.textCurrency);
        String currency = Configuration.CURRENCY + ".";
        textCurrency.setText(currency);
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                profile.getCurrent().setGeoId(GeoRealtime.createKey(profile.getCurrent()));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(final Profile profile) {
                if (profile.getCurrent().getId() != null) {
                    closeOrRemove.setText(R.string.remove);
                    closeOrRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeNoxbox(profile);
                        }
                    });
                } else {
                    closeOrRemove.setText(R.string.cancel);
                    closeOrRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cancelNoxboxConstructor();
                        }
                    });
                }
                draw(profile);
            }
        });

    }

    protected boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED;
    }


    private void draw(@NonNull final Profile profile) {
        drawToolbar(profile);
        drawRole(profile);
        drawType(profile);
        drawTypeDescription(profile);
        drawPrice(profile);
        drawTravelMode(profile);
        drawHost(profile);
        drawAddress(profile);
        drawNoxboxTimeSwitch(profile);
        drawPublishButton(profile);
    }

    private void drawToolbar(final Profile profile) {
        ((TextView) findViewById(R.id.title)).setText(R.string.constructorService);
        findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.startActivity(ConstructorActivity.this, MapActivity.class);
            }
        });
    }

    private void drawRole(final Profile profile) {
        ((TextView) findViewById(R.id.textProfile)).setText(getString(R.string.i).concat(" ").concat(profile.getName()).concat(" ").concat(getResources().getString(R.string.want)).concat(" "));
        final TextView textView = findViewById(R.id.textRole);
        SpannableStringBuilder spanTxt =
                new SpannableStringBuilder(getResources().getString(profile.getCurrent().getRole().getName()));
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                createRoleList(profile, textView);
            }
        }, spanTxt.length() - getResources().getString(profile.getCurrent().getRole().getName()).length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
        findViewById(R.id.arrowRole).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRoleList(profile, textView);
            }
        });
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
        findViewById(R.id.arrowNoxboxType).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogList();
            }
        });
    }

    private void drawTypeDescription(Profile profile) {
        ((TextView) findViewById(R.id.textTypeDescription)).setText(getResources().getString(profile.getCurrent().getType().getDuration()).concat("."));
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
                    String price = s.toString().replaceAll(",", "\\.");
                    profile.getCurrent().setPrice(price);
                }
            };
            priceInput.addTextChangedListener(changeCountOfMoneyListener);
        }
        priceInput.setText(profile.getCurrent().getPrice());
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
        if (profile.getCurrent().getOwner().getTravelMode() == TravelMode.none) {
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
        final TextView address = findViewById(R.id.textAddress);

        if (profile.getCurrent().getOwner().getTravelMode() == TravelMode.none || profile.getCurrent().getOwner().getHost()) {
            SpannableStringBuilder spanTxt =
                    new SpannableStringBuilder(AddressManager.provideAddressByPosition(getApplicationContext(), profile.getCurrent().getPosition()));
            spanTxt.append(" ");
            spanTxt.append(getString(R.string.change));
            spanTxt.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    ConstructorActivity.this.startActivityForResult(new Intent(ConstructorActivity.this, CoordinateActivity.class), COORDINATE);
                }
            }, spanTxt.length() - (getString(R.string.change).length()), spanTxt.length(), 0);
            address.setMovementMethod(LinkMovementMethod.getInstance());
            address.setText(spanTxt, TextView.BufferType.SPANNABLE);

        } else {
            address.setText(AddressManager.provideAddressByPosition(getApplicationContext(), profile.getCurrent().getPosition()) + " " + getResources().getString(R.string.change));
        }


    }

    protected void startDialogList() {
        Intent intent = new Intent(this, NoxboxTypeListActivity.class);
        startActivity(intent);
    }

    protected void createRoleList(final Profile profile, View textView) {
        final PopupMenu popup = new PopupMenu(ConstructorActivity.this, textView, Gravity.CENTER_HORIZONTAL);
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
        final PopupMenu popup = new PopupMenu(ConstructorActivity.this, textView, Gravity.CENTER_HORIZONTAL);
        for (TravelMode mode : TravelMode.values()) {
            popup.getMenu().add(Menu.NONE, mode.getId(), Menu.NONE, mode.getName());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                profile.getCurrent().getOwner().setTravelMode(TravelMode.byId(item.getItemId()));
                if (profile.getCurrent().getOwner().getTravelMode() != TravelMode.none) {
                    //если человек хочет двигаться путь разрешит отслеживать своё местоположение
                    if (checkLocationPermission()) {
                        ActivityCompat.requestPermissions(ConstructorActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    }
                } else {
                    profile.getCurrent().getOwner().setHost(true);
                    ((CheckBox) findViewById(R.id.isHost)).setChecked(true);
                    findViewById(R.id.isHost).setEnabled(false);
                }
                draw(profile);
                return true;
            }
        });
        popup.show();
    }

    private void drawNoxboxTimeSwitch(final Profile profile) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ConstructorActivity.this, R.layout.item_noxbox_time, NoxboxTime.getAllAsString());
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

    private void drawPublishButton(final Profile profile) {
        // TODO (vl) если имя пустое или null то выводить просьбу ввести имя
        ((LinearLayout) findViewById(R.id.publish).getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getCurrent().getRole() == MarketRole.demand && !BalanceCalculator.enoughBalance(profile.getCurrent(), profile)) {

                    ((LinearLayout) findViewById(R.id.publish).getParent())
                            .setBackgroundColor(getResources().getColor(R.color.translucent));

                    BottomSheetDialog.openWalletAddressSheetDialog(ConstructorActivity.this, profile);
                    return;
                }
                postNoxbox(profile);
            }
        });
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

    public void postNoxbox(Profile profile) {
        if (profile.getCurrent().getRole() == MarketRole.supply) {
            profile.getCurrent().getOwner().setPortfolio(profile.getPortfolio());
        } else {
            profile.getCurrent().getOwner().setPortfolio(null);
        }
        profile.getCurrent().clean();
        profile.getCurrent().setOwner(profile.publicInfo());
        profile.getCurrent().setTimeCreated(System.currentTimeMillis());
        Router.startActivity(ConstructorActivity.this, MapActivity.class);
    }

    public void removeNoxbox(Profile profile) {
        profile.getCurrent().clean();
        profile.getCurrent().setTimeRemoved(System.currentTimeMillis());
        Router.startActivity(ConstructorActivity.this, MapActivity.class);
    }

    private void cancelNoxboxConstructor() {
        Router.startActivity(ConstructorActivity.this, MapActivity.class);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (checkLocationPermission()) {
                ProfileStorage.readProfile(new Task<Profile>() {
                    @Override
                    public void execute(Profile profile) {
                        profile.getCurrent().getOwner().setTravelMode(TravelMode.none);
                        draw(profile);
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COORDINATE && resultCode == RESULT_OK) {
            final Position position = new Position(data.getExtras().getDouble(LAT), data.getExtras().getDouble(LNG));
            ProfileStorage.readProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    profile.getCurrent().setPosition(position);
                }
            });
        }
    }

}