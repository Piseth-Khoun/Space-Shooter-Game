package enemy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import util.ImageLoader;

/**
 * Base enemy type that demonstrates inheritance and polymorphism.
 */
public abstract class Enemy {

    protected enum MovePattern {
        STRAIGHT,
        ZIGZAG,
        SIDE_SWEEP
    }

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected double speed;
    protected int hp;
    protected int scoreValue;
    protected int hitFlash;
    protected int attackCooldown;
    protected int attackCooldownMax;
    protected int horizontalDirection = 1;
    protected int zigzagPhase;
    protected MovePattern movePattern;
    protected final int panelWidth;

    protected static final Random RAND = new Random();
    protected static final BufferedImage sprite = ImageLoader.load("enemy.png");

    protected Enemy(int panelWidth, int width, int height, int hp, double speed, int scoreValue, MovePattern movePattern) {
        this.panelWidth = panelWidth;
        this.width = width;
        this.height = height;
        this.hp = hp;
        this.speed = speed;
        this.scoreValue = scoreValue;
        this.movePattern = movePattern;
        this.attackCooldownMax = 180;
        this.attackCooldown = RAND.nextInt(attackCooldownMax);
    }

    public abstract void update();

    public void draw(Graphics2D g) {
        if (sprite != null) {
            if (hitFlash > 0 && hitFlash % 4 < 2) {
                g.drawImage(sprite, x, y, width, height, null);
                Composite original = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
                g.setColor(Color.RED);
                g.fillRect(x, y, width, height);
                g.setComposite(original);
            } else {
                g.drawImage(sprite, x, y, width, height, null);
            }
            return;
        }

        drawFallbackEnemy(g);
    }

    protected void drawFallbackEnemy(Graphics2D g) {
        g.setColor(new Color(220, 60, 60));
        int[] xs = { x, x + width, x + width / 2 };
        int[] ys = { y, y, y + height };
        g.fillPolygon(xs, ys, 3);

        g.setColor(Color.WHITE);
        g.drawPolygon(xs, ys, 3);

        g.setColor(new Color(255, 180, 0));
        g.fillOval(x + width / 2 - 5, y - 8, 10, 10);
    }

    public void takeDamage(int amount) {
        hp -= amount;
        hitFlash = 8;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public boolean isOffScreen(int panelWidth, int panelHeight) {
        return y > panelHeight || x < -width - 30 || x > panelWidth + width + 30;
    }

    public Rectangle getBounds() {
        return new Rectangle(x + 4, y + 4, width - 8, height - 8);
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public boolean canShoot() {
        return false;
    }

    public List<EnemyBullet> fireAt(int targetX, int targetY) {
        return Collections.emptyList();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    protected void moveStraightDown() {
        y += speed;
    }

    protected void moveZigZag(int panelWidth) {
        x += horizontalDirection * (speed * 0.9);
        y += speed;
        zigzagPhase++;
        if (zigzagPhase % 18 == 0) {
            horizontalDirection *= -1;
        }
        if (x < 0) {
            x = 0;
            horizontalDirection = 1;
        }
        if (x + width > panelWidth) {
            x = panelWidth - width;
            horizontalDirection = -1;
        }
    }

    protected void moveSideSweep(int panelWidth) {
        x += horizontalDirection * (speed * 0.7);
        if (x < 0 || x + width > panelWidth) {
            horizontalDirection *= -1;
            x += horizontalDirection * (speed * 2.0);
        }
        y += speed * 0.35;
    }

    protected List<EnemyBullet> spreadShot(int targetX, int targetY, int bulletCount) {
        List<EnemyBullet> bullets = new ArrayList<>();
        for (int i = 0; i < bulletCount; i++) {
            int offset = (i - (bulletCount - 1) / 2) * 14;
            bullets.add(new EnemyBullet(x + width / 2, y + height / 2, targetX + offset, targetY));
        }
        return bullets;
    }
}
