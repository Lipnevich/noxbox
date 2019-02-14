package live.noxbox.tools;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;

/**
 * Created by nicolay.lipnevich on 18/12/2017.
 */

public class ExchangeRate {

    private static final String WAVES_USD = "https://marketdata.wavesplatform.com/api/ticker/WAVES/USD";

    public static void wavesToUSD(final Task<BigDecimal> task) {
        new AsyncTask<Void, Void, BigDecimal>() {
            @Override
            protected BigDecimal doInBackground(Void... voids) {
                return ExchangeRate.getPrice();
            }

            @Override
            protected void onPostExecute(BigDecimal price) {
                task.execute(price);
            }
        }.execute();
    }

    private static BigDecimal getPrice() {
        BigDecimal price = null;

        try (InputStream is = new URL(WAVES_USD).openConnection().getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            JSONObject json = new JSONObject(sb.toString());

            price = new BigDecimal(json.getString("24h_open"));
        } catch (IOException | JSONException e) {
            Log.d("getPrice() FAILED", e.getMessage());
        }
        return price;
    }

}
