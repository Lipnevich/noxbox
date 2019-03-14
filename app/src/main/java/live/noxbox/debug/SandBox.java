package live.noxbox.debug;

import android.os.Build;
import android.support.annotation.RequiresApi;

public class SandBox {


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void main(String[] args) {

        String s = "sadsada";
        s.hashCode();
        String sd = "sadsada";
        sd.hashCode();
        String sf = "sadsada";
        sf.hashCode();
        System.out.println(s.hashCode() + " " + sd.hashCode() + " " + sf.hashCode());

    }
}
