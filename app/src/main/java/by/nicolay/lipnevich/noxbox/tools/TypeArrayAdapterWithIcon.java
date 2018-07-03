package by.nicolay.lipnevich.noxbox.tools;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;

public class TypeArrayAdapterWithIcon extends ArrayAdapter<NoxboxType> {

    private final List<NoxboxType> list;
    private final Activity activity;

    static class ViewHolder {
        protected TextView name;
        protected ImageView flag;
    }

    public TypeArrayAdapterWithIcon(Activity activity, List<NoxboxType> list) {
        super(activity,R.layout.activity_service_dialog_list, list);
        this.activity = activity;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = activity.getLayoutInflater();
            view = inflator.inflate(R.layout.activity_service_dialog_list, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.name);
            viewHolder.flag = (ImageView) view.findViewById(R.id.image);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.name.setText(list.get(position).getName());
        holder.flag.setImageDrawable(activity.getResources().getDrawable(list.get(position).getImage()));
        return view;
    }
}
