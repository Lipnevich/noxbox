package live.noxbox.pages;

import android.view.View;
import android.widget.ImageView;

import java.math.BigDecimal;

import live.noxbox.BuildConfig;
import live.noxbox.Configuration;
import live.noxbox.R;
import live.noxbox.menu.MenuActivity;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.ExchangeRate;
import live.noxbox.tools.Task;

import static live.noxbox.tools.DebugMessage.popup;

public class DebugActivity extends MenuActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            final ExchangeRate.Currency currency = ExchangeRate.Currency.USD;

            ImageView exchangeRateButton = findViewById(R.id.exchangeRate);
            exchangeRateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExchangeRate.wavesTo(currency, new Task<BigDecimal>() {
                        @Override
                        public void execute(BigDecimal price) {
                            popup(DebugActivity.this,
                                    price.toString() + " " + currency.name() + " per 1 Waves ");
                        }
                    });
                }
            });
            //exchangeRateButton.setVisibility(View.VISIBLE);


            ProfileStorage.readProfile(new Task<Profile>() {
                @Override
                public void execute(final Profile profile) {
                    DebugActivity.this.findViewById(R.id.debugLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.debugAccepting).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null) {
                                profile.getCurrent().setParty(new Profile().setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399)).setTravelMode(TravelMode.driving).setHost(false).setId("12321"));
                                // profile.getCurrent().getOwner().setPosition(new Position().setLongitude(27.639018).setLatitude(53.901399));
                                profile.getCurrent().setTimeRequested(System.currentTimeMillis());
                                ProfileStorage.fireProfile();
                            } else {
                                DebugMessage.popup(DebugActivity.this, "current noxbox is null");
                            }

                        }
                    });


                    findViewById(R.id.debugMoving).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null) {
                                profile.getCurrent().setParty(new Profile().setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399)).setTravelMode(TravelMode.driving).setHost(false).setId("12321").setPhoto(profile.getPhoto()));
                                profile.getCurrent().setTimeRequested(System.currentTimeMillis());
                                profile.getCurrent().setTimeAccepted(System.currentTimeMillis());
                                ProfileStorage.fireProfile();
                            } else {
                                DebugMessage.popup(DebugActivity.this, "current noxbox is null");
                            }
                        }
                    });
                    findViewById(R.id.debugPerforming).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null) {
                                profile.getCurrent().setParty(new Profile().setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399)).setTravelMode(TravelMode.driving).setHost(false).setId("12321"));
                                profile.getCurrent().setTimeRequested(System.currentTimeMillis());
                                profile.getCurrent().setTimeAccepted(System.currentTimeMillis());
                                profile.getCurrent().setTimeDemandVerified(System.currentTimeMillis());
                                profile.getCurrent().setTimeSupplyVerified(System.currentTimeMillis());
                                profile.getCurrent().setTimeStartPerforming(System.currentTimeMillis() - (Configuration.MINIMUM_PAYMENT_TIME_MILLIS - 10000));
                                ProfileStorage.fireProfile();
                            } else {
                                DebugMessage.popup(DebugActivity.this, "current noxbox is null");
                            }
                        }
                    });
                }
            });
        }


    }

}
