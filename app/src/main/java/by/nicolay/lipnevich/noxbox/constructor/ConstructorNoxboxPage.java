package by.nicolay.lipnevich.noxbox.constructor;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.MarketRole;
import by.nicolay.lipnevich.noxbox.model.NoxboxTime;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.TravelMode;
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.DebugMessage;
import by.nicolay.lipnevich.noxbox.tools.Task;

import static by.nicolay.lipnevich.noxbox.Configuration.LOCATION_PERMISSION_REQUEST_CODE;

public class ConstructorNoxboxPage extends AppCompatActivity{


    protected double price;
    private Button cancelOrRemove;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noxbox_constructor);
        cancelOrRemove = findViewById(R.id.closeOrRemove);
    }

    @Override
    protected void onResume() {
        super.onResume();
        State.listenProfile(new Task<Profile>() {
            @Override
            public void execute(final Profile profile) {
                if (profile.getCurrent() != null) {
                    cancelOrRemove.setText(R.string.remove);
                    cancelOrRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeNoxbox(profile);
                        }
                    });
                } else {
                    cancelOrRemove.setText(R.string.cancel);
                    cancelOrRemove.setOnClickListener(new View.OnClickListener() {
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
        findViewById(R.id.publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postNoxbox(profile);
            }
        });
        drawRole(profile);
        drawType(profile);
        drawTypeDescription(profile);
        drawPrice(profile);
        drawTravelMode(profile);
        drawNoxboxTimeSwitch(profile);
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
        ((TextView) findViewById(R.id.textTypeDescription)).setText(getResources().getString(profile.getCurrent().getType().getDescription()).concat("."));
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
                    profile.getCurrent().setPrice(s.toString());
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

    protected void startDialogList() {
        Intent intent = new Intent(this, NoxboxTypeListPage.class);
        startActivity(intent);
    }

    protected void createRoleList(final Profile profile, View textView) {
        final PopupMenu popup = new PopupMenu(ConstructorNoxboxPage.this, textView, Gravity.CENTER_HORIZONTAL);
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
        final PopupMenu popup = new PopupMenu(ConstructorNoxboxPage.this, textView, Gravity.CENTER_HORIZONTAL);
        for (TravelMode mode : TravelMode.values()) {
            popup.getMenu().add(Menu.NONE, mode.getId(), Menu.NONE, mode.getName());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                profile.getCurrent().getOwner().setTravelMode(TravelMode.byId(item.getItemId()));
                if(profile.getCurrent().getOwner().getTravelMode() != TravelMode.none) {
                    ActivityCompat.requestPermissions(ConstructorNoxboxPage.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                }
                draw(profile);
                return true;
            }
        });
        popup.show();
    }

    private Spinner timeSelectFrom;
    private Spinner timeSelectTo;
    private void drawNoxboxTimeSwitch(final Profile profile) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ConstructorNoxboxPage.this, R.layout.item_noxbox_time, NoxboxTime.getAllAsString());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        timeSelectFrom = findViewById(R.id.timeFromView);
        timeSelectFrom.setAdapter(adapter);
        timeSelectFrom.setSelection(profile.getCurrent().getWorkSchedule().getStartTime().getId());
        setHeightForDropdownList(timeSelectFrom);
        timeSelectFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                profile.getCurrent().getWorkSchedule().setStartTime(NoxboxTime.byId(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        timeSelectTo = findViewById(R.id.timeToView);
        timeSelectTo.setAdapter(adapter);
        timeSelectTo.setSelection(profile.getCurrent().getWorkSchedule().getEndTime().getId());
        setHeightForDropdownList(timeSelectTo);
        timeSelectTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                profile.getCurrent().getWorkSchedule().setEndTime(NoxboxTime.byId(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setHeightForDropdownList(Spinner spinner){
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinner);

            popupWindow.setHeight(500);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            DebugMessage.popup(this,e.getMessage());
        }
    }

    public void postNoxbox(Profile profile) {
        profile.getCurrent().setTimeCreated(System.currentTimeMillis());
        finish();
    }

    public void removeNoxbox(Profile profile) {
        profile.getCurrent().setTimeCreated(null);
        finish();
    }

    private void cancelNoxboxConstructor() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (checkLocationPermission()) {
                State.listenProfile(new Task<Profile>() {
                    @Override
                    public void execute(Profile profile) {
                        profile.getCurrent().getOwner().setTravelMode(TravelMode.none);
                        draw(profile);
                    }
                });
                return;
            }
        }
    }

}