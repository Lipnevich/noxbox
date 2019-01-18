package live.noxbox.menu.history;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;

import live.noxbox.R;
import live.noxbox.model.Noxbox;

public class HistoryFullScreenFragment extends DialogFragment {
    public static String TAG = "FullScreenDialog";
    private String profileId;

    public static Noxbox currentItemHistory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        profileId = getArguments().getString("profileId");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_full_screen_history, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.close_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        attachMapView(view.findViewById(R.id.map), currentItemHistory);

        attachTotal(view.findViewById(R.id.priceText), currentItemHistory);

        boolean isLiked = isLiked(currentItemHistory, profileId);
        ImageView rateBox = view.findViewById(R.id.rateBox);
        attachRating(rateBox, isLiked);
        if (isLiked) {
            rateBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.NoxboxAlertDialogStyle);
                    builder.setTitle(getResources().getString(R.string.dislikePrompt));
                    builder.setPositiveButton(getResources().getString(R.string.dislike),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    view.setOnClickListener(null);
                                    attachRating((ImageView) view, false);
//                                    dislikeNoxbox(profileId, noxbox);
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.show();
                }
            });
        }

        return view;
    }

    private void attachTotal(TextView total, Noxbox noxbox) {
        total.setText(noxbox.getTotal());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void attachMapView(MapView mapView, final Noxbox noxbox) {
        mapView.onCreate(null);
        mapView.onResume();
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsInitializer.initialize(getActivity());
                GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                        .image(BitmapDescriptorFactory.fromResource(noxbox.getType().getImage()))
                        .position(noxbox.getPosition().toLatLng(), 48, 48)
                        .anchor(0.5f, 1)
                        .zIndex(1000);
                GroundOverlay marker = googleMap.addGroundOverlay(newarkMap);
                marker.setDimensions(960, 960);

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noxbox.getPosition().toLatLng(), 11));

                googleMap.getUiSettings().setAllGesturesEnabled(false);
                googleMap.getUiSettings().setScrollGesturesEnabled(false);
                googleMap.getUiSettings().setZoomGesturesEnabled(false);
            }
        });
    }

    private void attachRating(ImageView view, boolean isLiked) {
        if (isLiked) {
            view.setImageResource(R.drawable.like);
        } else {
            view.setImageResource(R.drawable.dislike);
        }
    }

    private boolean isLiked(Noxbox noxbox, String profileId) {
        if (profileId.equals(noxbox.getOwner().getId())) {
            return noxbox.getTimeOwnerDisliked() == null;
        } else {
            return noxbox.getTimePartyDisliked() == null;
        }
    }
}
