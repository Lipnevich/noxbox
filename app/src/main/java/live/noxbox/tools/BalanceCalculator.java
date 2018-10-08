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
        //TODO (vl) пересчитать и зкрепить
        BigDecimal minimalPrice = new BigDecimal(noxbox.getPrice());
        minimalPrice = minimalPrice.divide(QUARTER, BIG_DECIMAL_DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_UP);
        BigDecimal walletBalance = new BigDecimal(profile.getWallet().getBalance());

        return minimalPrice.compareTo(walletBalance) < 0;
    }

    public static boolean enoughBalanceOnFiveMinutes(Noxbox noxbox, Profile profile) {
        BigDecimal priceForFiveMinutes = new BigDecimal(String.valueOf(Double.parseDouble(noxbox.getPrice()) / FIVE_MINUTES_PART_OF_HOUR))
                .setScale(BIG_DECIMAL_DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal pricePerSecond = new BigDecimal(Double.parseDouble(profile.getCurrent().getPrice()) / profile.getCurrent().getType().getDuration() / 60)
                .setScale(BIG_DECIMAL_DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal secondsPassed = new BigDecimal(String.valueOf((System.currentTimeMillis() - noxbox.getTimeStartPerforming()) / 1000));
        BigDecimal totalSpent = new BigDecimal(String.valueOf(pricePerSecond.doubleValue() * secondsPassed.doubleValue()));

        BigDecimal balance;
        if (noxbox.getRole() == MarketRole.supply) {
            balance = new BigDecimal(noxbox.getParty().getWallet().getBalance()).setScale(BIG_DECIMAL_DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_EVEN);
        } else {
            balance = new BigDecimal(noxbox.getOwner().getWallet().getBalance()).setScale(BIG_DECIMAL_DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_EVEN);
        }

        return balance.subtract(totalSpent).compareTo(priceForFiveMinutes) > 0;
    }
}
