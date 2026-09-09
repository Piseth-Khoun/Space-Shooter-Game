package bullet;

import java.awt.*;
import java.awt.image.BufferedImage;
import util.ImageLoader;

/**
 * Represents a single bullet fired by a player. Normally travels straight
 * up, but can drift sideways when fired at an aim angle ("R3 - Move Gun").
 */
public class Bullet {

    private double x, y;
    private final double vx;
    private final int width = 10;
    private final int height = 22;
    private final int speed = 10;

    private static final BufferedImage sprite = loadRotatedSprite();

    private static BufferedImage loadRotatedSprite() {
        BufferedImage raw = ImageLoader.load("bullet.png");
        if (raw == null) return null;
        // the source art points rightward (fire trail on the left); rotate
        // it -90 degrees so it points up, matching the bullet's flight direction
        BufferedImage rotated = new BufferedImage(raw.getHeight(), raw.getWidth(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rotated.createGraphics();
        g2.rotate(-Math.PI / 2, raw.getHeight() / 2.0, raw.getHeight() / 2.0);
        g2.drawImage(raw, 0, 0, null);
        g2.dispose();
        return rotated;
    }

    public Bullet(int x, int y) {
        this(x, y, 0);
    }

    /** aimAngleDeg tilts the bullet's flight path left/right of straight up. */
    public Bullet(int x, int y, double aimAngleDeg) {
        this.x = x;
        this.y = y;
        this.vx = Math.tan(Math.toRadians(aimAngleDeg)) * speed;
    }

    public void update() {
        x += vx;
        y -= speed;
    }

    public void draw(Graphics2D g) {
        int ix = (int) Math.round(x);
        int iy = (int) Math.round(y);

        if (sprite != null) {
            if (Math.abs(vx) > 0.05) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.rotate(Math.atan2(vx, speed), ix + width / 2.0, iy + height / 2.0);
                g2.drawImage(sprite, ix, iy, width, height, null);
                g2.dispose();
            } else {
                g.drawImage(sprite, ix, iy, width, height, null);
            }
            return;
        }
        g.setColor(Color.YELLOW);
        g.fillRoundRect(ix, iy, width, height, 4, 4);
    }

    public boolean isOffScreen() {
        return y + height < 0;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), width, height);
    }
}
