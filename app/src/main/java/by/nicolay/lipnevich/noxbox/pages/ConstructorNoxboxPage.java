package by.nicolay.lipnevich.noxbox.pages;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.MarketRole;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.TravelMode;
import by.nicolay.lipnevich.noxbox.tools.Firebase;

public class ConstructorNoxboxPage extends AppCompatActivity {

    protected String way;
    protected String type;
    protected String payment;// for EditText hint
    protected String travelMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noxbox_constructor);
        init();
        draw(Firebase.getProfile().getCurrent());
    }

    private void init() {
        way = getResources().getString(R.string.selectionField);
        type = getResources().getString(R.string.selectionField);
        payment = getResources().getString(R.string.selectionField);
        travelMode = getResources().getString(R.string.travelMode);
        Firebase.getProfile().setCurrent(new Noxbox());
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
                createLongDialog(NoxboxType.getAll());
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


    protected void createLongDialog(List<Integer> list) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ConstructorNoxboxPage.this);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ConstructorNoxboxPage.this, android.R.layout.select_dialog_singlechoice);
        for (int element : list) {
            arrayAdapter.add(getResources().getString(element));
        }

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                type = arrayAdapter.getItem(which).concat("\u2BC6").toLowerCase();
                draw(Firebase.getProfile().getCurrent());
            }
        });
        builderSingle.show();

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
                        Firebase.getProfile().getCurrent().setRole(MarketRole.supply);
                        Firebase.getProfile().getCurrent().setPerformer(Firebase.getProfile());
                        way = getResources().getString(R.string.supply).concat("\u2BC6");
                        payment = getResources().getString(R.string.earn);
                        ((EditText) findViewById(R.id.inputWaves)).setHint(R.string.earn);
                        break;
                    }
                    case R.id.demand: {
                        Firebase.getProfile().getCurrent().setRole(MarketRole.demand);
                        Firebase.getProfile().getCurrent().setPayer(Firebase.getProfile());
                        way = getResources().getString(R.string.demand).concat("\u2BC6");
                        payment = getResources().getString(R.string.spend);
                        ((EditText) findViewById(R.id.inputWaves)).setHint(R.string.spend);
                        break;
                    }
                    case R.id.none: {
                        Firebase.getProfile().setTravelMode(TravelMode.none);
                        travelMode = getResources().getString(R.string.stayNone).concat("\u2BC6");
                        break;
                    }
                    case R.id.car: {
                        Firebase.getProfile().setTravelMode(TravelMode.driving);
                        travelMode = getResources().getString(R.string.onCar).concat("\u2BC6");
                        break;
                    }
                    case R.id.bike: {
                        Firebase.getProfile().setTravelMode(TravelMode.bicycling);
                        travelMode = getResources().getString(R.string.onBike).concat("\u2BC6");
                        break;
                    }
                    case R.id.transit: {
                        Firebase.getProfile().setTravelMode(TravelMode.transit);
                        travelMode = getResources().getString(R.string.onTransit).concat("\u2BC6");
                        break;
                    }
                    case R.id.walk: {
                        Firebase.getProfile().setTravelMode(TravelMode.walking);
                        travelMode = getResources().getString(R.string.beWalk).concat("\u2BC6");
                        break;
                    }
                }
                draw(Firebase.getProfile().getCurrent());
                return true;
            }
        });
        popup.show(); //showing popup menu
    }

   /* private List<Noxbox> lsitExistence(Noxbox noxbox) {
        return Collections.singletonList(noxbox);
    }*/

    private void draw(Noxbox noxbox) {

        createContractPartOne((TextView) findViewById(R.id.contractPartOne));
        createContractPartTwo((TextView) findViewById(R.id.contractPartTwo));
    }

    private void backToMap() {
    }

}
