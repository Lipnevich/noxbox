package live.noxbox.tools;

import java.math.BigDecimal;

import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;

public class BalanceCalculator {

    public static boolean enoughBalance(Noxbox noxbox, Profile profile) {
        BigDecimal quarter = new BigDecimal("4.0");
        BigDecimal minimalPrice = new BigDecimal(noxbox.getPrice());
        minimalPrice = minimalPrice.divide(quarter, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal walletBalance = new BigDecimal(profile.getWallet().getBalance());

        if (minimalPrice.compareTo(walletBalance) < 0) return true;

        return false;
    }
}
