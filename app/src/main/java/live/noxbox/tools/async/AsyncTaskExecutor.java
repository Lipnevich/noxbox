package live.noxbox.tools.async;

import android.app.Activity;
import android.os.AsyncTask;

import live.noxbox.tools.Task;

/**
 * Created by Vladislaw Kravchenok on 01.03.2019.
 */
public class AsyncTaskExecutor<T> extends AsyncTask<T, Task<Object>, Void> {
    private Task<T> loadInBackground;
    private Task<Void> afterLoaded;
    private Activity activity;

    public AsyncTaskExecutor(Task<T> loadInBackground, Task<Void> afterLoaded, Activity activity) {
        this.loadInBackground = loadInBackground;
        this.afterLoaded = afterLoaded;
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(T... object) {
        loadInBackground.execute(object[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        afterLoaded.execute(aVoid);
    }


}
