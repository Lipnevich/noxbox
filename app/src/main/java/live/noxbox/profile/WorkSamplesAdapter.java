package live.noxbox.profile;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;

import java.util.List;

import live.noxbox.R;

public class WorkSamplesAdapter extends RecyclerView.Adapter<WorkSamplesAdapter.WorkSamplesViewHolder> {

    private List<String> workSamplesList;
    private ProfileActivity activity;

    public WorkSamplesAdapter(List<String> workSamplesList, ProfileActivity activity) {
        this.workSamplesList = workSamplesList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public WorkSamplesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_work_sample, parent, false);
        return new WorkSamplesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkSamplesViewHolder holder, int position) {
        final ImageButton imageButton = holder.workSample;

        Glide.with(activity)
                .asDrawable()
                .load(workSamplesList.get(position))
                .into(imageButton);
        
    }

    @Override
    public int getItemCount() {
        return workSamplesList.size();
    }


    static class WorkSamplesViewHolder extends RecyclerView.ViewHolder {
        ImageButton workSample;


        WorkSamplesViewHolder(View layout) {
            super(layout);
            workSample = layout.findViewById(R.id.workSampleImage);
        }
    }
}