package by.nicolay.lipnevich.noxbox.tools;

import android.support.design.widget.Snackbar;
import android.view.View;

import by.nicolay.lipnevich.noxbox.R;

public class ConfirmationMessage {
    public static void messageWithAction(View layout, String message, View.OnClickListener view) {
        Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE).setAction(R.string.yes, view).show();
    }

    public static View.OnClickListener messageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO enable GPS (vlad)
        }
    };
}
