package live.noxbox.tools;

import java.math.BigDecimal;

import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;

public class BalanceCalculator {

    public static boolean enoughBalance(Noxbox noxbox, Profile profile) {
        BigDecimal price = new BigDecimal(noxbox.getPrice());
        BigDecimal walletBalance = valueOrZero(profile.getWallet().getBalance());

        return price.compareTo(walletBalance) <= 0;
    }

    public static boolean enoughBalance(String price, BigDecimal balance) {
        return valueOrZero(price).compareTo(balance) <= 0;
    }

    private static BigDecimal valueOrZero(String text) {
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
