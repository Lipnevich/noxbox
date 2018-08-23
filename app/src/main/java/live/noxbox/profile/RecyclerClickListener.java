package live.noxbox.profile;

import android.view.View;

interface RecyclerClickListener {

    void onClick(View view, int position);

    void onLongClick(View view, int position);

}
