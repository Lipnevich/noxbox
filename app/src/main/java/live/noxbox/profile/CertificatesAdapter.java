package live.noxbox.profile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;

import java.util.List;

import live.noxbox.R;

public class CertificatesAdapter extends RecyclerView.Adapter<CertificatesAdapter.CertificatesViewHolder> {

    private List<String> certificateList;
    private final ProfileActivity activity;

    public CertificatesAdapter(List<String> certificateList, ProfileActivity activity) {
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
        final ImageButton imageButton = holder.certificate;

        Glide.with(activity)
                .asDrawable()
                .load(certificateList.get(position))
                .into(imageButton);

    }

    @Override
    public int getItemCount() {
        return certificateList.size();
    }


    static class CertificatesViewHolder extends RecyclerView.ViewHolder {
        ImageButton certificate;


        CertificatesViewHolder(View layout) {
            super(layout);
            certificate = layout.findViewById(R.id.certificateImage);
        }
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private CertificatesAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final CertificatesAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildLayoutPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
