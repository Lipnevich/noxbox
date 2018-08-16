package live.noxbox.profile;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import live.noxbox.R;
import live.noxbox.tools.DebugMessage;

public class CertificatesAdapter extends RecyclerView.Adapter<CertificatesAdapter.CertificatesViewHolder> {

    private List<String> certificateList;
    private Activity activity;

    public CertificatesAdapter(List<String> certificateList, Activity activity) {
        this.certificateList = certificateList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public CertificatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certeficate, parent, false);
        return new CertificatesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificatesViewHolder holder, int position) {
        if(position == certificateList.size() - 1){
            holder.certificate.setImageResource(R.drawable.add_certeficate);
            holder.certificate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO (vl) invite user to upload certificate
                    DebugMessage.popup(activity,"invite user to upload certificate");
                }
            });
            return;
        }
        Glide.with(activity)
                .asDrawable()
                .load(certificateList.get(position))
                .into(holder.certificate);

    }

    @Override
    public int getItemCount() {
        return certificateList.size();
    }


    static class CertificatesViewHolder extends RecyclerView.ViewHolder {
        ImageView certificate;


        CertificatesViewHolder(View layout) {
            super(layout);
            certificate = layout.findViewById(R.id.certificateImage);
        }
    }
}
