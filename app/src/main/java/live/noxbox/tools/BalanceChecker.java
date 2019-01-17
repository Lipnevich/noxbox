package live.noxbox.tools;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import live.noxbox.model.Profile;

public class BalanceChecker {

    private static StringRequest stringRequest;
    private static RequestQueue requestQueue;
    private static final String TAG = BalanceChecker.class.getName();

    public static void updateBalance(Profile profile, Context context) {
        if (requestQueue != null && stringRequest != null) {
            requestQueue.add(stringRequest);
            return;
        }
        requestQueue = Volley.newRequestQueue(context);
        String url = "https://nodes.wavesplatform.com/addresses/balance/";
        url = url.concat(profile.getWallet().getAddress());
        stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            JSONObject jObject = null;
            String walletBalance = null;
            try {
                jObject = new JSONObject(response);
                walletBalance = jObject.getString("balance");
                BigDecimal balance = new BigDecimal(walletBalance).divide(new BigDecimal("100000000"));
                profile.getWallet().setBalance(balance.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Crashlytics.logException(error));
        stringRequest.setTag(TAG);
        requestQueue.add(stringRequest);
    }

    public static void cancelBalanceUpdate() {
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}
