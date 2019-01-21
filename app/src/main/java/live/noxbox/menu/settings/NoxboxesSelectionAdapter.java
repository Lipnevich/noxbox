package live.noxbox.menu.settings;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import live.noxbox.R;
import live.noxbox.debug.DebugMessage;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;

public class NoxboxesSelectionAdapter extends RecyclerView.Adapter<NoxboxesSelectionAdapter.ViewHolder> {

    private List<NoxboxType> noxboxTypes;
    private Activity activity;
    private Profile profile;
    private boolean[] typesChecked;
    private Integer totalChecked = 0;

    public NoxboxesSelectionAdapter(List<NoxboxType> noxboxTypes, Activity activity, Profile profile, boolean[] typesChecked, Integer totalChecked) {
        this.activity = activity;
        this.noxboxTypes = noxboxTypes;
        this.profile = profile;
        this.typesChecked = typesChecked;
        this.totalChecked = totalChecked;

        DebugMessage.popup(activity, String.valueOf(totalChecked));
    }

    @NonNull
    @Override
    public NoxboxesSelectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_noxboxes_selection, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoxboxesSelectionAdapter.ViewHolder viewHolder, int position) {
        viewHolder.checkBox.setChecked(typesChecked[position]);
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    totalChecked++;
                } else {
                    totalChecked--;
                }
                typesChecked[position] = checked;
            }
        });

        viewHolder.type.setText(noxboxTypes.get(position).getName());


    }

    @Override
    public int getItemCount() {
        return noxboxTypes.size() - 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView type;

        public ViewHolder(@NonNull View layout) {
            super(layout);
            checkBox = layout.findViewById(R.id.checkbox);
            type = layout.findViewById(R.id.noxboxTypeName);
        }
    }
}
