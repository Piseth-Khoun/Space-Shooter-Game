package enemy;

import java.awt.*;

public class EnemyBullet {

    private final int width = 10;
    private final int height = 18;
    private double x;
    private double y;
    private final double dx;
    private final double dy;
    private final int speed = 4;

    public EnemyBullet(int startX, int startY, int targetX, int targetY) {
        this.x = startX;
        this.y = startY;

        double directionX = targetX - startX;
        double directionY = targetY - startY;
        double magnitude = Math.max(1, Math.hypot(directionX, directionY));

        this.dx = (directionX / magnitude) * speed;
        this.dy = (directionY / magnitude) * speed;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 90, 90));
        g.fillRoundRect((int) Math.round(x), (int) Math.round(y), width, height, 4, 4);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), width, height);
    }

    public boolean isOffScreen(int panelWidth, int panelHeight) {
        return x < -20 || x > panelWidth + 20 || y > panelHeight + 20 || y < -20;
    }
}
