package live.noxbox.tools;

import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;

public class BalanceCalculator {

    public static boolean enoughBalance(Noxbox noxbox, Profile profile) {
        //TODO (vl)  java.lang.NumberFormatException: For input string: "0,25"
        Double minimalPrice = Double.parseDouble(noxbox.getPrice()) / 4;
        Double walletBalance = Double.parseDouble(profile.getWallet().getBalance());
        if (minimalPrice < walletBalance) return true;

        return false;
    }
}
