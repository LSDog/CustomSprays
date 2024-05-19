package fun.LSDog.CustomSprays.util;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.spray.SprayBase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.Random;

public class ParticleUtil {

    private static final Random RANDOM = new Random();

    /**
     * Play a particle animation like spraying. <br>
     * Only on 1.9+ since there's no particle api in 1.8. <br>
     * <a href="https://www.spigotmc.org/threads/comprehensive-particle-spawning-guide-1-13-1-19.343001/">A particle spawning guide post on SpigotMC</a>
     * @param spray The spray it goes
     * @param count Particle count
     * @param singleCount Number of particle in one turn
     * @param length Side length of spray range (square)
     */
    public static void playSprayParticleEffect(SprayBase spray, int count, int singleCount, double length, long delay) {
        Location originLoc = spray.player.getLocation();
        Location fromLoc = spray.player.getEyeLocation();
        // Add some distance on fromLoc to prevent particle collide with player's head
        fromLoc.add(spray.player.getLocation().getDirection().multiply(Math.min(spray.distance, 1.2)));
        Location toLoc = spray.location.clone();
        switch (spray.blockFace) {
            case NORTH: toLoc.add(0.5,0.5,1); break;
            case SOUTH: toLoc.add(0.5, 0.5, 0); break;
            case WEST: toLoc.add(1,0.5,0.5); break;
            case EAST: toLoc.add(0, 0.5, 0.5); break;
            case DOWN: toLoc.add(0.5,1,0.5); break;
            case UP: toLoc.add(0.5, 0, 0.5); break;
        }
        toLoc.subtract(fromLoc);
        Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.plugin, () -> {
            for (int i = 0; i < count; i++) {
                for (int i1 = 0; i1 < singleCount; i1++) {
                    // set count to 0 to make spawn offset = movement offset
                    spray.location.getWorld().spawnParticle(
                            Particle.CLOUD,
                            fromLoc.getX()+0.2*rd(),
                            fromLoc.getY(),
                            fromLoc.getZ()+0.2*rd(),
                            0,
                            toLoc.getX()+length*rd(),
                            toLoc.getY()+length*rd(),
                            toLoc.getZ()+length*rd(),
                            0.2 /* move speed */
                    );
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                fromLoc.add(spray.player.getLocation().subtract(originLoc));
            }
        });
    }

    /**
     * @return a double between -0.5 to 0.5
     */
    private static double rd() {
        return RANDOM.nextDouble()-0.5;
    }

}
