package live.noxbox.tools.async;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import live.noxbox.tools.Task;

/**
 * Created by Vladislaw Kravchenok on 01.03.2019.
 */
public class AsyncTaskLoaderCallback implements LoaderManager.LoaderCallbacks {

    private Task<Object> loadInBackground;
    private Context context;

    public AsyncTaskLoaderCallback() {
    }

    public AsyncTaskLoaderCallback(Task<Object> loadInBackground, Context context) {
        this.loadInBackground = loadInBackground;
        this.context = context;
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int i, @Nullable Bundle bundle) {
        return new AsyncTaskLoader(context);
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object o) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

    }
}
