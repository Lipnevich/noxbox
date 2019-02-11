package live.noxbox;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context context(){
        return context;
    }
}
