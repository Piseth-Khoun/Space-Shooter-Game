import java.awt.*;

/**
 * One particle in an explosion burst: fire, smoke, a spark streak, or a
 * tumbling piece of debris. Purely visual.
 */
public class Particle {

    public enum Kind { FIRE, SMOKE, SPARK, DEBRIS }

    private double x, y;
    private double vx, vy;
    private int life;
    private final int maxLife;
    private final Kind kind;
    private final Color color;
    private double rotation;
    private final double rotationSpeed;

    public Particle(double x, double y, double vx, double vy, int life, Kind kind, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        this.maxLife = life;
        this.kind = kind;
        this.color = color;
        this.rotation = Math.random() * Math.PI * 2;
        this.rotationSpeed = (Math.random() - 0.5) * 0.4;
    }

    public void update() {
        x += vx;
        y += vy;

        if (kind == Kind.SMOKE) {
            vy -= 0.03;
            vx *= 0.98;
        } else if (kind == Kind.DEBRIS) {
            vy += 0.12;
            rotation += rotationSpeed;
        } else {
            vx *= 0.95;
            vy *= 0.95;
        }

        life--;
    }

    public boolean isDead() {
        return life <= 0;
    }

    public void draw(Graphics2D g) {
        float progress = 1f - (life / (float) maxLife);
        float alpha = Math.max(0f, Math.min(1f, 1f - progress));
        Composite original = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(color);

        switch (kind) {
            case FIRE:
                int fireSize = (int) (6 * (1 - progress) + 2);
                g.fillOval((int) x - fireSize / 2, (int) y - fireSize / 2, fireSize, fireSize);
                break;
            case SMOKE:
                int smokeSize = (int) (4 + progress * 11);
                g.fillOval((int) x - smokeSize / 2, (int) y - smokeSize / 2, smokeSize, smokeSize);
                break;
            case SPARK:
                g.drawLine((int) x, (int) y, (int) (x - vx * 2), (int) (y - vy * 2));
                break;
            case DEBRIS:
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                g2.rotate(rotation);
                g2.fillRect(-2, -2, 4, 4);
                g2.dispose();
                break;
        }

        g.setComposite(original);
    }
}
