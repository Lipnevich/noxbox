package by.nicolay.lipnevich.noxbox.constructor;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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

import org.joda.time.DateTime;

import java.util.Calendar;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.MarketRole;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.TimePeriod;
import by.nicolay.lipnevich.noxbox.model.TravelMode;
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.DebugMessage;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.Task;

import static by.nicolay.lipnevich.noxbox.MapActivity.ON_CONSTRUCTOR_RESULT_CODE;
import static by.nicolay.lipnevich.noxbox.state.State.getCurrentNoxbox;
import static by.nicolay.lipnevich.noxbox.state.State.setCurrentNoxbox;

public class ConstructorNoxboxPage extends AppCompatActivity {

    protected double price;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noxbox_constructor);

        ((EditText) findViewById(R.id.inputPrice)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                getCurrentNoxbox().setPrice(s.toString());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        State.listenCurrentNoxbox(new Task<Noxbox>() {
            @Override
            public void execute(Noxbox object) {
                draw();
            }
        });
    }

    private void drawRole(final TextView textView) {
        SpannableStringBuilder spanTxt =
                new SpannableStringBuilder(getResources().getString(R.string.i).concat(" ").concat(Firebase.getProfile().getName()).concat(" ").concat(getResources().getString(R.string.want)).concat(" "));
        spanTxt.append(getResources().getString(getCurrentNoxbox().getRole().getName()));
        spanTxt.append("\uD83E\uDC93");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                createRoleList(textView);
            }
        }, spanTxt.length() - getResources().getString(getCurrentNoxbox().getRole().getName()).concat("\uD83E\uDC93").length(), spanTxt.length(), 0);
        spanTxt.append(" ".concat(getResources().getString(R.string.service)).concat(" "));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private void drawType(final TextView textView) {
        SpannableStringBuilder spanTxt =
                new SpannableStringBuilder(getResources().getString(getCurrentNoxbox().getType().getName()));
        spanTxt.append("\uD83E\uDC93");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startDialogList();
            }
        }, spanTxt.length() - (getResources().getString(getCurrentNoxbox().getType().getName()).concat("\uD83E\uDC93")).length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private void drawPrice(EditText editText) {
        editText.setText(getCurrentNoxbox().getPrice());
    }

    private void drawTravelMode(final TextView textView) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(getResources().getString(getCurrentNoxbox().getOwner().getTravelMode().getName()));
        spanTxt.append("\uD83E\uDC93");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                createTravelModeList(textView);
            }
        }, spanTxt.length() - getResources().getString(getCurrentNoxbox().getOwner().getTravelMode().getName()).concat(("\uD83E\uDC93")).length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private void drawTimePicker(final TextView textView) {
        String result;
        if (getCurrentNoxbox().getNoxboxTime().getPeriod() == TimePeriod.accurate) {
            result = getCurrentNoxbox().getNoxboxTime().getTimeAsString();
        } else {
            result = getResources().getString(getCurrentNoxbox().getNoxboxTime().getPeriod().getName());
        }
        DebugMessage.popup(ConstructorNoxboxPage.this,result);
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(result);
        spanTxt.append("\uD83E\uDC93");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                createTimePeriodList(textView);
            }
        }, spanTxt.length() - result.concat("\uD83E\uDC93").length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }


    protected void startDialogList() {
        Intent intent = new Intent(this, NoxboxTypeListPage.class);
        startActivity(intent);
    }

    protected void createRoleList(View textView) {
        final PopupMenu popup = new PopupMenu(ConstructorNoxboxPage.this, textView, Gravity.CENTER_HORIZONTAL);
        for (MarketRole role : MarketRole.values()) {
            popup.getMenu().add(Menu.NONE, role.getId(), Menu.NONE, role.getName());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                setCurrentNoxbox(getCurrentNoxbox().setRole(MarketRole.byId(item.getItemId())));
                return true;
            }
        });
        popup.show();
    }

    private void createTravelModeList(TextView textView) {
        final PopupMenu popup = new PopupMenu(ConstructorNoxboxPage.this, textView, Gravity.CENTER_HORIZONTAL);
        for (TravelMode mode : TravelMode.values()) {
            popup.getMenu().add(Menu.NONE, mode.getId(), Menu.NONE, mode.getName());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                getCurrentNoxbox().getOwner().setTravelMode(TravelMode.byId(item.getItemId()));
                draw();
                return true;
            }
        });
        popup.show();
    }

    private void createTimePeriodList(TextView textView) {
        final PopupMenu popup = new PopupMenu(ConstructorNoxboxPage.this, textView, Gravity.CENTER_HORIZONTAL);
        for (TimePeriod element : TimePeriod.values()) {
            popup.getMenu().add(Menu.NONE, element.getId(), Menu.NONE, element.getName());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() != 1) {
                    getCurrentNoxbox().getNoxboxTime().setPeriod(TimePeriod.byId(item.getItemId()));
                } else {
                    displayStartTimeDialog();
                    getCurrentNoxbox().getNoxboxTime().setPeriod(TimePeriod.byId(item.getItemId()));
                }
                draw();
                return true;
            }
        });
        popup.show();
    }

    private void displayStartTimeDialog() {
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                getCurrentNoxbox().getNoxboxTime().setStart(new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute));
                displayEndTimeDialog();
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

    private void displayEndTimeDialog() {
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                getCurrentNoxbox().getNoxboxTime().setEnd(new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute));
                draw();
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

   /* private List<Noxbox> lsitExistence(Noxbox noxbox) {
        return Collections.singletonList(noxbox);
    }*/

    private void draw() {
        drawRole((TextView) findViewById(R.id.textRole));
        drawType((TextView) findViewById(R.id.textNoxboxType));
        drawPrice((EditText) findViewById(R.id.inputPrice));
        drawTravelMode((TextView) findViewById(R.id.textTravelMode));
        drawTimePicker((TextView) findViewById(R.id.textTimePeriod));
    }


    public void backToMapActivity(View view) {
        if (State.getProfile() != null) {
            setResult(ON_CONSTRUCTOR_RESULT_CODE);
            finish();
        } else {
            DebugMessage.popup(this, "Noxbox is not exist");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            DebugMessage.popup(this, "onActivityResult");
        }
    }

}
