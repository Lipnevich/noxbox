package live.noxbox.tools.async;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Vladislaw Kravchenok on 01.03.2019.
 */
public class AsyncTaskLoader extends android.support.v4.content.AsyncTaskLoader {


    public AsyncTaskLoader(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public Object loadInBackground() {
        return null;
    }


}
