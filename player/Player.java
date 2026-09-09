package player;

import java.awt.*;
import java.awt.image.BufferedImage;
import util.ImageLoader;
import weapon.WeaponMode;

/**
 * Represents a player's spaceship: position, movement, aim, health and
 * rendering. Also used for Player 2 in 2-player mode (pass a tint color
 * to visually distinguish the second ship).
 */
public class Player {

    private int x, y;
    private final int width = 60;
    private final int height = 60;
    private int speed = 6;

    private int health;
    private final int maxHealth = 3;
    private WeaponMode weaponMode = WeaponMode.NORMAL;
    private int rapidFireTimer = 0;
    private int speedBoostTimer = 0;
    private int shieldTimer = 0;
    private boolean shielded = false;

    // "R3 - Move Gun": the turret can be aimed left/right independently of ship movement.
    private double aimAngleDeg = 0;
    private static final double MAX_AIM_ANGLE = 25;
    private static final double AIM_SPEED = 2.2;

    // Simple hit-flash so the player can see when they've been hit
    private int hitFlashTimer = 0;

    private final Color tint;

    private static final BufferedImage sprite = ImageLoader.load("player.png");

    public Player(int panelWidth, int panelHeight) {
        this(panelWidth, panelHeight, null);
    }

    /** tint is drawn as a translucent overlay on the ship sprite; pass null for the default look (Player 1). */
    public Player(int panelWidth, int panelHeight, Color tint) {
        this.x = panelWidth / 2 - width / 2;
        this.y = panelHeight - height - 30;
        this.health = maxHealth;
        this.tint = tint;
    }

    public void update(boolean up, boolean down, boolean left, boolean right,
                        boolean aimLeft, boolean aimRight, int panelWidth, int panelHeight) {
        if (rapidFireTimer > 0) rapidFireTimer--;
        if (speedBoostTimer > 0) speedBoostTimer--;
        if (shieldTimer > 0) {
            shieldTimer--;
            if (shieldTimer == 0) shielded = false;
        }

        int movementSpeed = speed;
        if (speedBoostTimer > 0) movementSpeed += 2;

        if (up) y -= movementSpeed;
        if (down) y += movementSpeed;
        if (left) x -= movementSpeed;
        if (right) x += movementSpeed;

        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > panelWidth - width) x = panelWidth - width;
        if (y > panelHeight - height) y = panelHeight - height;

        if (aimLeft) aimAngleDeg -= AIM_SPEED;
        if (aimRight) aimAngleDeg += AIM_SPEED;
        if (aimAngleDeg < -MAX_AIM_ANGLE) aimAngleDeg = -MAX_AIM_ANGLE;
        if (aimAngleDeg > MAX_AIM_ANGLE) aimAngleDeg = MAX_AIM_ANGLE;

        if (hitFlashTimer > 0) hitFlashTimer--;
    }

    public void draw(Graphics2D g) {
        boolean flashRed = hitFlashTimer > 0 && hitFlashTimer % 6 < 3;

        if (sprite != null) {
            if (flashRed) {
                g.drawImage(sprite, x, y, width, height, null);
                Composite original = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g.setColor(Color.RED);
                g.fillRect(x, y, width, height);
                g.setComposite(original);
            } else {
                g.drawImage(sprite, x, y, width, height, null);
                if (tint != null) {
                    Composite original = g.getComposite();
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
                    g.setColor(tint);
                    g.fillRect(x, y, width, height);
                    g.setComposite(original);
                }
            }
            if (shielded) {
                g.setColor(new Color(70, 190, 255, 160));
                g.drawOval(x - 6, y - 6, width + 12, height + 12);
            }
            drawAimIndicator(g);
            return;
        }

        g.setColor(flashRed ? Color.RED : (tint != null ? tint : new Color(80, 200, 255)));
        int[] xs = { x + width / 2, x, x + width };
        int[] ys = { y, y + height, y + height };
        g.fillPolygon(xs, ys, 3);
        g.setColor(Color.WHITE);
        g.drawPolygon(xs, ys, 3);
        g.setColor(Color.ORANGE);
        g.fillOval(x + width / 2 - 6, y + height - 6, 12, 14);
        if (shielded) {
            g.setColor(new Color(70, 190, 255, 160));
            g.drawOval(x - 6, y - 6, width + 12, height + 12);
        }
        drawAimIndicator(g);
    }

    private void drawAimIndicator(Graphics2D g) {
        if (Math.abs(aimAngleDeg) < 0.5) return;
        double rad = Math.toRadians(aimAngleDeg);
        int noseX = x + width / 2;
        int noseY = y;
        int tipX = noseX + (int) Math.round(Math.sin(rad) * 24);
        int tipY = noseY - (int) Math.round(Math.cos(rad) * 24);
        Stroke original = g.getStroke();
        g.setColor(new Color(255, 255, 255, 190));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(noseX, noseY, tipX, tipY);
        g.setStroke(original);
    }

    public void takeDamage() {
        if (shielded) {
            shielded = false;
            shieldTimer = 0;
            hitFlashTimer = 8;
            return;
        }
        health--;
        hitFlashTimer = 30;
    }

    public void heal() {
        if (health < maxHealth) {
            health++;
        }
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void reset(int panelWidth, int panelHeight) {
        this.x = panelWidth / 2 - width / 2;
        this.y = panelHeight - height - 30;
        this.health = maxHealth;
        this.hitFlashTimer = 0;
        this.weaponMode = WeaponMode.NORMAL;
        this.rapidFireTimer = 0;
        this.speedBoostTimer = 0;
        this.shieldTimer = 0;
        this.shielded = false;
        this.aimAngleDeg = 0;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x + 8, y + 8, width - 16, height - 16);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public WeaponMode getWeaponMode() { return weaponMode; }
    public String getWeaponLabel() { return weaponMode.getLabel(); }
    public int getRapidFireTimer() { return rapidFireTimer; }
    public int getSpeedBoostTimer() { return speedBoostTimer; }
    public double getAimAngleDeg() { return aimAngleDeg; }

    public void setWeaponMode(WeaponMode weaponMode) {
        this.weaponMode = weaponMode;
    }

    public void cycleWeaponMode() {
        this.weaponMode = this.weaponMode.nextMode();
    }

    public void activateRapidFire() {
        rapidFireTimer = 300;
    }

    public void activateSpeedBoost() {
        speedBoostTimer = 300;
    }

    public void activateShield() {
        shielded = true;
        shieldTimer = 300;
    }

    public int getShotCount() {
        return weaponMode.getLevel();
    }
}
