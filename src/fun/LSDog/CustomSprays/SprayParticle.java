package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.spray.Spray;
import org.bukkit.entity.Player;

import java.util.Collection;

public class SprayParticle extends Spray {


    /**
     * The constructor of Spray <br>
     * <b>pixels must be 128*128</b>
     *
     * @param player The sprayer
     * @param pixels Byte color array
     * @param showTo The players who can see this spray (in spraying).
     */
    public SprayParticle(Player player, byte[] pixels, Collection<? extends Player> showTo) {
        super(player, pixels, showTo);
    }


    //try {
    //    ImageParticles particles = new ImageParticles(
    //            ImageUtil.resizeImage(
    //                    ImageIO.read(new File("C:/Users/LSDog/Desktop/head.png")), 0.2),1);
    //    particles.setAnchor(50, 10);
    //    particles.setDisplayRatio(0.05);

    //    Vector dist = player.getEyeLocation().getDirection();
    //    dist.multiply(-0.5);
    //    Location location=player.getEyeLocation().add(dist).subtract(0,0.5,0);
    //    Map<Location, Color> particle = particles.getParticles(location, 0, 0);
    //    for(Location spot : particle.keySet()) {
    //        player.getWorld().spawnParticle(Particle.REDSTONE, spot, 1, new Particle.DustOptions(particle.get(spot), 1f));
    //    }
    //} catch (IOException e) {
    //    e.printStackTrace();
    //}

}
