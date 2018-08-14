package live.noxbox.tools;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import live.noxbox.Configuration;
import live.noxbox.model.Comment;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.Rating;
import live.noxbox.model.TravelMode;
import live.noxbox.model.WorkSchedule;

public class NoxboxExamples {

    public static List<Noxbox> generateNoxboxes(Position position, int size, Activity activity) {
        List<Noxbox> noxboxes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {

            Noxbox noxbox = new Noxbox();

            noxbox.setTimeCreated(System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(-1000000, 0));
            noxbox.setRole(MarketRole.values()[ThreadLocalRandom.current().nextInt(MarketRole.values().length)]);
            Profile owner = new Profile().setId("1231" + i).setPosition(new Position().setLongitude(27.609018).setLatitude(53.951399));

            owner.setTravelMode(TravelMode.values()[ThreadLocalRandom.current().nextInt(TravelMode.values().length)]);
            if (owner.getTravelMode() == TravelMode.none) {
                owner.setHost(true);
            } else {
                owner.setHost(ThreadLocalRandom.current().nextBoolean());
            }

            Rating rating = new Rating();
            rating.setReceivedLikes(ThreadLocalRandom.current().nextInt(0, 1000));
            rating.setReceivedDislikes(ThreadLocalRandom.current().nextInt((int)Math.ceil(rating.getReceivedLikes() / 10)));

            rating.getComments().put("0", new Comment("0", "Очень занятный молодой человек, и годный напарник!", System.currentTimeMillis(), true));
            rating.getComments().put("1", new Comment("1", "Добротный паренёк!", System.currentTimeMillis(), true));
            rating.getComments().put("2", new Comment("2", "Выносливость бы повысить, слишком быстро выдыхается во время кросса.", System.currentTimeMillis(), false));

            noxbox.setType(NoxboxType.values()[ThreadLocalRandom.current().nextInt(NoxboxType.values().length)]);
            owner.getDemandsRating().put(activity.getResources().getString(noxbox.getType().getName()), rating);
            noxbox.setOwner(owner);
            noxbox.setId("12311" + i);
            noxbox.setEstimationTime("0");
            noxbox.setPrice(ThreadLocalRandom.current().nextInt(10, 100) + "");
            double delta = 360 * Configuration.RADIUS_IN_METERS / 40075000;
            noxbox.setPosition(new Position(
                    position.getLatitude() + ThreadLocalRandom.current().nextDouble(-delta, delta),
                    position.getLongitude() + ThreadLocalRandom.current().nextDouble(-delta, delta)));
            owner.setPosition(new Position(
                    position.getLatitude() + ThreadLocalRandom.current().nextDouble(-delta, delta),
                    position.getLongitude() + ThreadLocalRandom.current().nextDouble(-delta, delta)));
            noxbox.setWorkSchedule(new WorkSchedule());
            noxboxes.add(noxbox);
        }
        return noxboxes;
    }
}