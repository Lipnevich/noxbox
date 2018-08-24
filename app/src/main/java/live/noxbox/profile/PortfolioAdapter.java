package live.noxbox.profile;

import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import live.noxbox.R;
import live.noxbox.detailed.CommentAdapter;
import live.noxbox.model.Comment;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Portfolio;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder> {

    private List<Portfolio> portfolioList;
    private final ProfileActivity activity;

    public PortfolioAdapter(List<Portfolio> portfolioList, ProfileActivity activity) {
        this.portfolioList = portfolioList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public PortfolioAdapter.PortfolioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_performer_settings, parent, false);
        return new PortfolioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PortfolioAdapter.PortfolioViewHolder holder, int position) {
        holder.noxboxTypeImage.setImageResource(NoxboxType.byId(portfolioList.get(position).getTypeId()).getImage());
        holder.noxboxTypeName.setText(NoxboxType.byId(portfolioList.get(position).getTypeId()).getName());

        Iterator iterator = portfolioList.get(position).getRating().getComments().entrySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            i++;
        }
        if (i == 0) {
            holder.commentsCleanText.setVisibility(View.VISIBLE);
            holder.noxboxTypeListLayout.setVisibility(View.GONE);

        } else {
            holder.commentsCleanText.setVisibility(View.GONE);
            holder.noxboxTypeListLayout.setVisibility(View.VISIBLE);
            List<Comment> comments = new ArrayList<>();
            Iterator iterator1 = portfolioList.get(position).getRating().getComments().entrySet().iterator();
            while (iterator1.hasNext()) {
                comments.add((Comment) iterator.next());
            }

            RecyclerView recyclerView = (RecyclerView) holder.commentsList;
            recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(new CommentAdapter(comments));
        }


        List<String> certificatesList = portfolioList.get(position).getCertificates();

        holder.addCertificate.setVisibility(View.GONE);

        RecyclerView listCertificates = (RecyclerView) holder.certificatesList;
        listCertificates.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        listCertificates.setAdapter(new ImageListAdapter(certificatesList, activity));

        holder.certificateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


        List<String> workSampleList = portfolioList.get(position).getWorkSamples();


        holder.addWorkSamples.setVisibility(View.GONE);

        RecyclerView listSamples = (RecyclerView) holder.workSampleList;
        listSamples.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        listSamples.setAdapter(new ImageListAdapter(workSampleList, activity));

        holder.workSampleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


    }

    @Override
    public int getItemCount() {
        return portfolioList.size();
    }

    static class PortfolioViewHolder extends RecyclerView.ViewHolder {
        ImageView noxboxTypeImage;
        TextView noxboxTypeName;

        RecyclerView commentsList;
        NestedScrollView noxboxTypeListLayout;
        TextView commentsCleanText;
        
        RecyclerView certificatesList;
        TextView certificateCleanText;
        RelativeLayout certificateLayout;
        ImageButton addCertificate;

        TextView workSampleCleanText;
        RelativeLayout workSampleLayout;
        RecyclerView workSampleList;
        ImageButton addWorkSamples;

        PortfolioViewHolder(View layout) {
            super(layout);
            noxboxTypeImage = layout.findViewById(R.id.noxboxTypeImage);
            noxboxTypeName = layout.findViewById(R.id.noxboxTypeName);
            commentsList = layout.findViewById(R.id.commentsList);
            noxboxTypeListLayout = layout.findViewById(R.id.noxboxTypeListLayout);
            commentsCleanText = layout.findViewById(R.id.commentsCleanText);
            certificatesList = layout.findViewById(R.id.certificatesList);
            certificateCleanText = layout.findViewById(R.id.certificateCleanText);
            certificateLayout = layout.findViewById(R.id.certificateLayout);
            addCertificate = layout.findViewById(R.id.addCertificate);
            workSampleCleanText = layout.findViewById(R.id.workSampleCleanText);
            workSampleLayout = layout.findViewById(R.id.workSampleLayout);
            workSampleList = layout.findViewById(R.id.workSampleList);
            addWorkSamples = layout.findViewById(R.id.workSamples);


        }
    }
}
