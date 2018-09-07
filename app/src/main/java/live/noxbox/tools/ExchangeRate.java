package live.noxbox.tools;

import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;

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

    private static final String ORDER_BOOK = "https://nodes.wavesnodes.com/matcher/orderbook/WAVES/%s";

    public enum Currency {
        USD("Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck", 2),
        EUR("Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU", 2)
        ;

        private final String id;
        private final int scale;

        Currency(String id, int scale) {
            this.id = id;
            this.scale = scale;
        }

        public String id() {
            return id;
        }

        public int scale() {
            return scale;
        }

    }

    // ExchangeRate.Currency.USD
    public static void wavesTo(final Currency currency, final Task<BigDecimal> task) {
        new AsyncTask<Void, Void, BigDecimal>() {
            @Override
            protected BigDecimal doInBackground(Void... voids) {
                return ExchangeRate.getPrice(currency);
            }
            @Override
            protected void onPostExecute(BigDecimal price) {
                task.execute(price);
            }
        }.execute();
    }

    private static BigDecimal getPrice(Currency currency) {
        String url = String.format(ORDER_BOOK, currency.id());
        BigDecimal price = null;

        try (InputStream is = new URL(url).openConnection().getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            JSONObject json = new JSONObject(sb.toString());

            price = BigDecimal.valueOf(json.getJSONArray("bids").getJSONObject(0)
                    .getLong("price"), currency.scale());
        } catch (IOException | JSONException e) {
            Crashlytics.logException(e);
        }
        return price;
    }

}
