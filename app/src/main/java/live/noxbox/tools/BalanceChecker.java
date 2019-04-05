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

import static com.google.common.base.Strings.isNullOrEmpty;

public class BalanceChecker {

    private static StringRequest stringRequest;
    private static RequestQueue requestQueue;
    private static final String TAG = BalanceChecker.class.getName();

    public static void cleanRequestBalanceQueue() {
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
            requestQueue = null;
        }

        if (stringRequest != null) {
            stringRequest = null;
        }
    }

    public static void checkBalance(Profile profile, Context context, Task<BigDecimal> afterBalanceCheck) {
        if (isNullOrEmpty(profile.getWallet().getAddress())) {
            afterBalanceCheck.execute(BigDecimal.ZERO);
            return;
        }

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
                afterBalanceCheck.execute(balance);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, Crashlytics::logException);
        stringRequest.setTag(TAG);
        requestQueue.add(stringRequest);

    }

    public static void checkBalance(Profile profile, Context context) {
        checkBalance(profile, context, balance -> {
            profile.getWallet().setBalance(balance.toString());
        });
    }

    public static void cancelBalanceUpdate() {
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}
