package live.noxbox.tools.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Vladislaw Kravchenok on 01.03.2019.
 */
public class AsyncTaskLoader extends androidx.loader.content.AsyncTaskLoader {


    public AsyncTaskLoader(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public Object loadInBackground() {
        return null;
    }


}
