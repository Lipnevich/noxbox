package live.noxbox.menu.settings;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import live.noxbox.R;
import live.noxbox.model.NoxboxType;

public class NoxboxesSelectionAdapter extends RecyclerView.Adapter<NoxboxesSelectionAdapter.ViewHolder> {

    private List<NoxboxType> noxboxTypes;
    private Activity activity;
    private boolean[] typesChecked;

    public NoxboxesSelectionAdapter(List<NoxboxType> noxboxTypes, Activity activity, boolean[] typesChecked) {
        this.activity = activity;
        this.noxboxTypes = noxboxTypes;
        this.typesChecked = typesChecked;
    }

    @NonNull
    @Override
    public NoxboxesSelectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_noxboxes_selection, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoxboxesSelectionAdapter.ViewHolder viewHolder, int position) {
        viewHolder.checkBox.setChecked(typesChecked[position]);
        viewHolder.checkBox.setOnCheckedChangeListener((compoundButton, checked) -> typesChecked[position] = checked);

        viewHolder.type.setText(noxboxTypes.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return noxboxTypes.size() - 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView type;

        ViewHolder(@NonNull View layout) {
            super(layout);
            checkBox = layout.findViewById(R.id.checkbox);
            type = layout.findViewById(R.id.noxboxTypeName);
        }
    }
}
