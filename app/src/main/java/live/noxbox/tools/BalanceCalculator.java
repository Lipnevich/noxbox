package live.noxbox.tools;

import java.math.BigDecimal;

import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;

import static live.noxbox.Configuration.BIG_DECIMAL_DEFAULT_BALANCE_SCALE;
import static live.noxbox.Configuration.FIVE_MINUTES_PART_OF_HOUR;

public class BalanceCalculator {
    private static final BigDecimal QUARTER = new BigDecimal("4.0");

    public static boolean enoughBalance(Noxbox noxbox, Profile profile) {
        BigDecimal minimalPrice = new BigDecimal(noxbox.getPrice());
        minimalPrice = minimalPrice.divide(QUARTER, BIG_DECIMAL_DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        BigDecimal walletBalance = new BigDecimal(profile.getWallet().getBalance());

        return minimalPrice.compareTo(walletBalance) < 0;
    }

    public static boolean enoughBalanceOnFiveMinutes(Noxbox noxbox, Profile profile) {
        BigDecimal priceForFiveMinutes = new BigDecimal(noxbox.getPrice()).divide(new BigDecimal(FIVE_MINUTES_PART_OF_HOUR), BIG_DECIMAL_DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        BigDecimal pricePerSecond = new BigDecimal(profile.getCurrent().getPrice()).divide(new BigDecimal(profile.getCurrent().getType().getDuration()), BIG_DECIMAL_DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        pricePerSecond = pricePerSecond.divide(new BigDecimal("60"), BIG_DECIMAL_DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        BigDecimal secondsPassed = new BigDecimal(String.valueOf(System.currentTimeMillis())).subtract(new BigDecimal(String.valueOf(noxbox.getTimeStartPerforming())));
        secondsPassed = secondsPassed.divide(new BigDecimal("1000"), BIG_DECIMAL_DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        BigDecimal totalSpent = new BigDecimal(String.valueOf(pricePerSecond.doubleValue())).multiply(new BigDecimal(String.valueOf(secondsPassed.doubleValue())));
        BigDecimal balance;
        if (noxbox.getRole() == MarketRole.supply) {
            balance = new BigDecimal(noxbox.getParty().getWallet().getBalance()).setScale(BIG_DECIMAL_DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        } else {
            balance = new BigDecimal(noxbox.getOwner().getWallet().getBalance()).setScale(BIG_DECIMAL_DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        }

        return balance.subtract(totalSpent).compareTo(priceForFiveMinutes) > 0;
    }
}
