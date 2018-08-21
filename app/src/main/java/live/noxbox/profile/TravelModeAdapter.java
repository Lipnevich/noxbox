package live.noxbox.profile;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import live.noxbox.R;
import live.noxbox.model.TravelMode;

public class TravelModeAdapter extends ArrayAdapter<TravelMode> {

    private final List<TravelMode> list;
    private final Activity activity;

    static class ViewHolder {
        protected TextView name;
        protected ImageView flag;
    }

    public TravelModeAdapter(Activity activity, List<TravelMode> list) {
        super(activity, R.layout.activity_dialog_list, list);
        this.activity = activity;
        this.list = list;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = activity.getLayoutInflater();
            view = inflator.inflate(R.layout.activity_dialog_list, null);
            final TravelModeAdapter.ViewHolder viewHolder = new TravelModeAdapter.ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.name);
            viewHolder.flag = (ImageView) view.findViewById(R.id.image);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        TravelModeAdapter.ViewHolder holder = (TravelModeAdapter.ViewHolder) view.getTag();
        holder.name.setText(list.get(position).getName());
        holder.flag.setImageDrawable(activity.getResources().getDrawable(list.get(position).getImage()));
        return view;
    }
}
