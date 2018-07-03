package by.nicolay.lipnevich.noxbox.pages;

import android.app.Activity;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.MarketRole;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.TravelMode;
import by.nicolay.lipnevich.noxbox.model.UserAccount;
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.DebugMessage;
import by.nicolay.lipnevich.noxbox.tools.Firebase;

import static by.nicolay.lipnevich.noxbox.MapActivity.ON_ACTIVITY_RESULT_RQUEST_CODE;

public class ConstructorNoxboxPage extends AppCompatActivity {

    protected String way;
    protected String type;
    protected String payment;// for EditText hint
    protected String travelMode;
    protected double wavesSum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noxbox_constructor);
        init();
        draw();
    }

    private void init() {
        way = getResources().getString(R.string.selectionField);
        type = getResources().getString(R.string.selectionField);
        payment = getResources().getString(R.string.selectionField);
        travelMode = getResources().getString(R.string.travelMode);
        Firebase.getProfile().setCurrent(new Noxbox());
        State.setUserAccount(new UserAccount().setProfile(Firebase.getProfile()));
        ((EditText) findViewById(R.id.inputWaves)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    wavesSum = Double.parseDouble(s.toString());
                    Firebase.getProfile().getCurrent().setPrice(s.toString());
                } catch (Exception e) {
                    DebugMessage.popup(ConstructorNoxboxPage.this, e.getMessage());
                }
            }
        });
    }


    private void createContractPartOne(final TextView textView) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                getResources().getString(R.string.i).concat(" ").concat(Firebase.getProfile().getName()).concat(" ").concat(getResources().getString(R.string.want)).concat(" "));
        spanTxt.append(way);
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                createSimplyDialog(R.menu.way_service_menu, textView);
            }
        }, spanTxt.length() - way.length(), spanTxt.length(), 0);
        spanTxt.append(" ".concat(getResources().getString(R.string.service)).concat(" "));
        spanTxt.append(type);
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startDialogList(NoxboxType.getAll());
            }
        }, spanTxt.length() - type.length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private void createContractPartTwo(final TextView textView) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(travelMode);
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                createSimplyDialog(R.menu.travel_mode_menu, textView);
            }
        }, spanTxt.length() - travelMode.length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }


    protected void startDialogList(final List<NoxboxType> list) {
        Intent intent = new Intent(this, TypeListPage.class);
        //TODO need add saveInstanceState before starting dialog list activity(vlad)
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            type = data.getStringExtra("typeName");
            draw();
        }
    }

    protected void createSimplyDialog(int menu, View textView) {
        final PopupMenu popup = new PopupMenu(ConstructorNoxboxPage.this, textView, Gravity.CENTER_HORIZONTAL);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.supply: {
                        State.getUserAccount().getProfile().getCurrent().setRole(MarketRole.supply);
                        State.getUserAccount().getProfile().getCurrent().setPerformer(State.getUserAccount().getProfile());
                        way = getResources().getString(R.string.supply).concat("\u2BC6");
                        payment = getResources().getString(R.string.earn);
                        ((EditText) findViewById(R.id.inputWaves)).setHint(R.string.earn);
                        break;
                    }
                    case R.id.demand: {
                        State.getUserAccount().getProfile().getCurrent().setRole(MarketRole.demand);
                        State.getUserAccount().getProfile().getCurrent().setPerformer(State.getUserAccount().getProfile());
                        way = getResources().getString(R.string.demand).concat("\u2BC6");
                        payment = getResources().getString(R.string.spend);
                        ((EditText) findViewById(R.id.inputWaves)).setHint(R.string.spend);
                        break;
                    }
                    case R.id.none: {
                        State.getUserAccount().getProfile().setTravelMode(TravelMode.none);
                        travelMode = getResources().getString(R.string.stayNone).concat("\u2BC6");
                        break;
                    }
                    case R.id.car: {
                        State.getUserAccount().getProfile().setTravelMode(TravelMode.driving);
                        travelMode = getResources().getString(R.string.onCar).concat("\u2BC6");
                        break;
                    }
                    case R.id.bike: {
                        State.getUserAccount().getProfile().setTravelMode(TravelMode.bicycling);
                        travelMode = getResources().getString(R.string.onBike).concat("\u2BC6");
                        break;
                    }
                    case R.id.transit: {
                        State.getUserAccount().getProfile().setTravelMode(TravelMode.transit);
                        travelMode = getResources().getString(R.string.onTransit).concat("\u2BC6");
                        break;
                    }
                    case R.id.walk: {
                        State.getUserAccount().getProfile().setTravelMode(TravelMode.walking);
                        travelMode = getResources().getString(R.string.beWalk).concat("\u2BC6");
                        break;
                    }
                }

                draw();
                return true;
            }
        });
        popup.show(); //showing popup menu
    }

   /* private List<Noxbox> lsitExistence(Noxbox noxbox) {
        return Collections.singletonList(noxbox);
    }*/

    private void draw() {
        createContractPartOne((TextView) findViewById(R.id.contractPartOne));
        createContractPartTwo((TextView) findViewById(R.id.contractPartTwo));
    }


    public void backToMapActivity(View view) {
        if (Firebase.getProfile().getCurrent() != null) {
            if (wavesSum != 0) {
                if (!way.equals(getResources().getString(R.string.selectionField))) {
                    if (!type.equals(getResources().getString(R.string.selectionField))) {
                        if (!travelMode.equals(getResources().getString(R.string.travelMode))) {
                            setResult(ON_ACTIVITY_RESULT_RQUEST_CODE);
                            finish();
                        } else {
                            DebugMessage.popup(this, "TravelMode not selected");
                        }
                    } else {
                        DebugMessage.popup(this, "NoxboxType not selected");
                    }
                } else {
                    DebugMessage.popup(this, "Role not selected");
                }
            } else {
                DebugMessage.popup(this, "Price not entered");
            }
        } else {
            DebugMessage.popup(this, "Noxbox is not exist");
        }
    }
}
