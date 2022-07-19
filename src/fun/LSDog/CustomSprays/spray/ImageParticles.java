package fun.LSDog.CustomSprays.spray;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple Utility to have images as particles
 * @author Backstabber
 */
public class ImageParticles {

    private final Map<Vector,Color> particles= new HashMap<>();
    private final BufferedImage image;
    private final int clearance = 300;
    private Vector anchor=new Vector(0,0,0);
    private double ratio=0.2;

    /**
     * Create a new object
     * @param image of the particles structure you want
     * @param scanQuality is the quality of the scanned image in particles (1 for default 2 for half etc)
     */
    public ImageParticles(BufferedImage image, int scanQuality) {
        this.image = image;
        renderParticles(Math.abs(scanQuality));
    }

    /**
     * Set the anchor point for the particles structure
     * by default the anchor will be the bottom right
     * @param x (the x axis of the image or width)
     * @param y (the y axis of the image or height
     */
    public void setAnchor(int x,int y) {
        anchor=new Vector(x, y, 0);
    }

    /**
     * Sets the ratio between blocks & pixels i.e block/pixel (0.1 means 10 pixels in 1 block space)
     */
    public void setDisplayRatio(double ratio) {
        this.ratio=ratio;
    }

    /**
     * Get a map of locations & colors on which particles are to be displayed
     * @param location of the anchor point
     * @param pitch (if you want picture to be rotated)
     * @param yaw  (if you want picture to be rotated)
     * @return map of the locations & color
     */
    @SuppressWarnings("UnusedAssignment")
    public Map<Location, Color> getParticles(Location location, double pitch, double yaw) {
        Map<Location, Color> map = new HashMap<>();
        for(Vector vector:particles.keySet()) {
            Vector difference=vector.clone().subtract(anchor).multiply(ratio);
            Vector v=rotateAroundAxisX(difference,pitch);
            v=rotateAroundAxisY(v, yaw);
            Location spot=location.clone().add(difference);
            map.put(spot, particles.get(vector));
        }
        return map;
    }

    /**
     * Get a map of locations & colors on which particles are to be displayed
     */
    public Map<Location, Color> getParticles(Location location) {
        return getParticles(location, location.getPitch(), location.getYaw());
    }


    private void renderParticles(int sensitivity) {
        int height=image.getHeight();
        int width=image.getWidth();
        for(int x=0;x<width;x=x+sensitivity) {
            for(int y=0;y<height;y=y+sensitivity) {
                int rgb=image.getRGB(x, y);
                if(-rgb<= clearance)
                    continue;
                java.awt.Color javaColor=new java.awt.Color(rgb);
                Vector vector=new Vector((width-1)-x, (height-1)-y, 0);
                particles.put(vector, Color.fromRGB(javaColor.getRed(),javaColor.getGreen(),javaColor.getBlue()));
            }
        }
    }

    private Vector rotateAroundAxisX(Vector v, double angle)
    {
        angle = Math.toRadians(angle);
        double y, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        y = v.getY() * cos - v.getZ() * sin;
        z = v.getY() * sin + v.getZ() * cos;
        return v.setY(y).setZ(z);
    }
    private Vector rotateAroundAxisY(Vector v, double angle)
    {
        angle = -angle;
        angle = Math.toRadians(angle);
        double x, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

}