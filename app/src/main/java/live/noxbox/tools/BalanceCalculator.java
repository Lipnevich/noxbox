package live.noxbox.tools;

import java.math.BigDecimal;

import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;

public class BalanceCalculator {
    private static final BigDecimal QUARTER = new BigDecimal("4.0");

    public static boolean enoughBalance(Noxbox noxbox, Profile profile) {
        BigDecimal minimalPrice = new BigDecimal(noxbox.getPrice());
        minimalPrice = minimalPrice.divide(QUARTER, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal walletBalance = new BigDecimal(profile.getWallet().getBalance());

        return minimalPrice.compareTo(walletBalance) < 0;
    }
}
