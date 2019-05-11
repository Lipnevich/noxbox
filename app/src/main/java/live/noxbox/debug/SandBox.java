package live.noxbox.debug;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class SandBox {


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void main(String[] args) {
        String one = "one";
        System.out.println(one.split("1")[0]);
    }
}
