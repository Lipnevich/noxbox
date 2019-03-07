package live.noxbox.debug;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.stream.IntStream;

import live.noxbox.model.Noxbox;

public class SandBox {

    private static String temp;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void main(String[] args) {
        Noxbox noxbox = new Noxbox();
        String id = "123";
        noxbox.setId(id);

        IntStream.of(1000000).forEach(o -> temp = id);

        long time = System.currentTimeMillis();
        IntStream.of(1000000).forEach(o -> temp = id);
        System.out.println(System.currentTimeMillis() - time);


        time = System.currentTimeMillis();
        IntStream.of(1000000).forEach(o -> temp = noxbox.getId());
        System.out.println(System.currentTimeMillis() - time);


    }
}
