package live.noxbox.tools;

import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;

public class BalanceCalculator {

    public static boolean enoughBalance(Noxbox noxbox, Profile profile) {
        Integer minimalPrice = Integer.parseInt(noxbox.getPrice()) / 4;
        Integer walletBalance = Integer.parseInt(profile.getWallet().getBalance());
        if (minimalPrice < walletBalance) return true;

        return false;
    }
}
