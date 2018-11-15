package live.noxbox.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import live.noxbox.model.Comment;
import live.noxbox.model.ImageType;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Portfolio;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.Rating;
import live.noxbox.model.TravelMode;
import live.noxbox.model.WorkSchedule;

public class NoxboxExamples {

    public static List<Noxbox> generateNoxboxes(Position position, int size, double delta) {
        if (position == null) position = new Position();
        List<Noxbox> noxboxes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {

            Noxbox noxbox = new Noxbox();

            noxbox.setRole(MarketRole.values()[ThreadLocalRandom.current().nextInt(MarketRole.values().length)]);
            Profile owner = new Profile().setId("" + ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE))
                    .setNoxboxId("" + ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE)).setPosition(new Position().setLongitude(27.609018).setLatitude(53.951399)).setPhoto("http://fit4brain.com/wp-content/uploads/2014/06/zelda.jpg").setName("Granny Zelda");

            owner.setTravelMode(TravelMode.values()[ThreadLocalRandom.current().nextInt(TravelMode.values().length)]);
            if (owner.getTravelMode() == TravelMode.none) {
                owner.setHost(true);
            } else {
                owner.setHost(ThreadLocalRandom.current().nextBoolean());
            }

            Rating rating = new Rating();
            rating.setReceivedLikes(ThreadLocalRandom.current().nextInt(900, 1000));
            rating.setReceivedDislikes(ThreadLocalRandom.current().nextInt(rating.getReceivedLikes() / 10));

            rating.getComments().put("0", new Comment("0", "Очень занятный молодой человек, и годный напарник!", System.currentTimeMillis(), true));
            rating.getComments().put("1", new Comment("1", "Добротный паренёк!", System.currentTimeMillis(), true));
            rating.getComments().put("2", new Comment("2", "Выносливость бы повысить, слишком быстро выдыхается во время кросса.", System.currentTimeMillis(), false));

            noxbox.setType(NoxboxType.values()[ThreadLocalRandom.current().nextInt(NoxboxType.values().length)]);
            owner.getDemandsRating().put(noxbox.getType().name(), rating);
            owner.getSuppliesRating().put(noxbox.getType().name(), rating);
            noxbox.setOwner(owner);
            noxbox.setId(owner.getNoxboxId());
            noxbox.setEstimationTime("0");
            noxbox.setPrice(ThreadLocalRandom.current().nextInt(10, 100) + "");
            noxbox.setPosition(new Position(
                    position.getLatitude() + ThreadLocalRandom.current().nextDouble(-delta, delta),
                    position.getLongitude() + ThreadLocalRandom.current().nextDouble(-delta, delta)));
            owner.setPosition(new Position(
                    position.getLatitude() + ThreadLocalRandom.current().nextDouble(-delta, delta),
                    position.getLongitude() + ThreadLocalRandom.current().nextDouble(-delta, delta)));
            noxbox.setWorkSchedule(new WorkSchedule());

            if (noxbox.getRole() == MarketRole.supply) {
                List<String> certificatesList = new ArrayList<>();
                certificatesList.add("https://i.pinimg.com/736x/1d/ba/a1/1dbaa1fb5b2f64e54010cf6aae72b8b1.jpg");
                certificatesList.add("http://4u-professional.com/assets/images/sert/gel-lak.jpg");
                certificatesList.add("https://www.hallyuuk.com/wp-content/uploads/2018/06/reiki-master-certificate-template-inspirational-reiki-certificate-templates-idealstalist-of-reiki-master-certificate-template.jpg");
                certificatesList.add("http://www.childminder.ng/blog_pics/1479134810.jpg");

                List<String> workSampleList = new ArrayList<>();
                workSampleList.add("http://coolmanicure.com/media/k2/items/cache/stilnyy_manikur_so_strazami_XL.jpg");
                workSampleList.add("http://rosdesign.com/design_materials3/img_materials3/kopf/kopf1.jpg");
                workSampleList.add("http://vmirevolos.ru/wp-content/uploads/2015/12/61.jpg");

                Map<String, List<String>> images = new HashMap<>();
                images.put(ImageType.samples.name(), new ArrayList<String>(workSampleList));
                images.put(ImageType.certificates.name(), new ArrayList<String>(certificatesList));

                Map<String, Portfolio> portfolioMap = new HashMap<>();
                portfolioMap.put(noxbox.getType().name(), new Portfolio(new HashMap<String, List<String>>(images)));
                noxbox.getOwner().setPortfolio(portfolioMap);
            }

            //Фильтрация услуг в зависимости от настроек
            noxbox.setTimeCreated(System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(-1000000, 0));
            if (noxbox.getType() == NoxboxType.redirect) continue;
            noxboxes.add(noxbox);
        }


        return noxboxes;
    }
}