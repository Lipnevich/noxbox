package by.nicolay.lipnevich.noxbox.constructor;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.MarketRole;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.TimePeriod;
import by.nicolay.lipnevich.noxbox.model.TravelMode;
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.DebugMessage;
import by.nicolay.lipnevich.noxbox.tools.Task;
import org.joda.time.DateTime;

import java.util.Calendar;

public class ConstructorNoxboxPage extends AppCompatActivity {

    private static final String ARROW = "\uD83E\uDC93";
    protected double price;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noxbox_constructor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        State.listenProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                draw(profile);
            }
        });
    }

    private void draw(@NonNull Profile profile) {
        drawRole(profile);
        drawType(profile);
        drawPrice(profile);
        drawTravelMode(profile);
        drawTimePicker(profile);
    }

    private void drawRole(final Profile profile) {
        final TextView textView = findViewById(R.id.textRole);
        SpannableStringBuilder spanTxt =
                new SpannableStringBuilder(getResources().getString(R.string.i).concat(" ").concat(profile.getName()).concat(" ").concat(getResources().getString(R.string.want)).concat(" "));
        spanTxt.append(getResources().getString(profile.getCurrent().getRole().getName()));
        spanTxt.append(ARROW);
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                createRoleList(profile, textView);
            }
        }, spanTxt.length() - getResources().getString(profile.getCurrent().getRole().getName()).concat(ARROW).length(), spanTxt.length(), 0);
        spanTxt.append(" ".concat(getResources().getString(R.string.service)).concat(" "));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private void drawType(Profile profile) {
        final TextView textView = findViewById(R.id.textNoxboxType);
        SpannableStringBuilder spanTxt =
                new SpannableStringBuilder(getResources().getString(profile.getCurrent().getType().getName()));
        spanTxt.append(ARROW);
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startDialogList();
            }
        }, spanTxt.length() - (getResources().getString(profile.getCurrent().getType().getName()).concat(ARROW)).length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private TextWatcher listener;

    private void drawPrice(final Profile profile) {
        EditText priceInput = findViewById(R.id.inputPrice);
        if(listener == null) {
            listener = new TextWatcher() {
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
            priceInput.addTextChangedListener(listener);
        }
        priceInput.setText(profile.getCurrent().getPrice());
    }

    private void drawTravelMode(final Profile profile) {
        final TextView textView = findViewById(R.id.textTravelMode);
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(getResources().getString(profile.getCurrent().getOwner().getTravelMode().getName()));
        spanTxt.append(ARROW);
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                createTravelModeList(profile, textView);
            }
        }, spanTxt.length() - getResources().getString(profile.getCurrent().getOwner().getTravelMode().getName()).concat((ARROW)).length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private void drawTimePicker(final Profile profile) {
        final TextView textView = findViewById(R.id.textTimePeriod);
        String result;
        if (profile.getCurrent().getNoxboxTime().getPeriod() == TimePeriod.accurate) {
            result = profile.getCurrent().getNoxboxTime().getTimeAsString();
        } else {
            result = getResources().getString(profile.getCurrent().getNoxboxTime().getPeriod().getName());
        }
        DebugMessage.popup(ConstructorNoxboxPage.this,result);
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(result);
        spanTxt.append(ARROW);
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                createTimePeriodList(profile, textView);
            }
        }, spanTxt.length() - result.concat(ARROW).length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
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
                draw(profile);
                return true;
            }
        });
        popup.show();
    }

    private void createTimePeriodList(final Profile profile, TextView textView) {
        final PopupMenu popup = new PopupMenu(ConstructorNoxboxPage.this, textView, Gravity.CENTER_HORIZONTAL);
        for (TimePeriod element : TimePeriod.values()) {
            popup.getMenu().add(Menu.NONE, element.getId(), Menu.NONE, element.getName());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() != 1) {
                    profile.getCurrent().getNoxboxTime().setPeriod(TimePeriod.byId(item.getItemId()));
                } else {
                    displayStartTimeDialog(profile);
                    profile.getCurrent().getNoxboxTime().setPeriod(TimePeriod.byId(item.getItemId()));
                }
                draw(profile);
                return true;
            }
        });
        popup.show();
    }

    private void displayStartTimeDialog(final Profile profile) {
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                profile.getCurrent().getNoxboxTime().setStart(new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute));
                displayEndTimeDialog(profile);
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(ConstructorNoxboxPage.this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                myTimeListener, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.setTitle(getResources().getString(R.string.startTime));
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
    }

    private void displayEndTimeDialog(final Profile profile) {
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                profile.getCurrent().getNoxboxTime().setEnd(new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute));
                draw(profile);
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(ConstructorNoxboxPage.this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                myTimeListener, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.setTitle(getResources().getString(R.string.endTime));
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
    }

    public void postNoxbox(View view) {
        finish();
    }

}
