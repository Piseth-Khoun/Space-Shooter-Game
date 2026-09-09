import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import util.ImageLoader;
import util.SpriteSheet;

/**
 * Visual-only explosion effect (no sound - see manager.SoundManager for that).
 *
 * Primary visual is the real hand-authored explosion sprite sheet
 * (images/explosion.png: flash -> fire -> ember -> smoke, 256px cells,
 * read left-to-right/top-to-bottom), with a few flying spark and debris
 * particles layered on top for extra punch. If the sprite sheet can't be
 * loaded for any reason, it falls back to the old procedural fire/smoke
 * burst so the game never breaks over missing art.
 *
 * Two flavors:
 *  - Explosion(x, y[, scale])  - a full death burst (sprite + debris/sparks)
 *  - Explosion.hitSpark(x, y)  - a quick, small tap for non-lethal hits,
 *    so a multi-hit enemy like a Tank doesn't play a full "boom" on every
 *    bullet that doesn't actually kill it
 */
public class Explosion {

    private static final BufferedImage SHEET_IMAGE = ImageLoader.load("explosion.png");
    private static final SpriteSheet SHEET = SHEET_IMAGE != null ? new SpriteSheet(SHEET_IMAGE, 256) : null;
    private static final int BASE_DISPLAY_SIZE = 76;

    private final List<Particle> particles = new ArrayList<>();
    private final int centerX, centerY;
    private final double scale;
    private final boolean fullBurst;

    private int frame = 0;
    private int frameTick = 0;
    private final int ticksPerFrame;
    private boolean spriteFinished;

    // only used when SHEET is null (art missing) or for the brief hit-spark flash
    private int flashLife;

    public Explosion(int centerX, int centerY) {
        this(centerX, centerY, 1.0, true);
    }

    /** scale > 1.0 makes a bigger burst - used for boss deaths and bombs. */
    public Explosion(int centerX, int centerY, double scale) {
        this(centerX, centerY, scale, true);
    }

    /** A quick, small spark tap for a non-lethal hit - no sprite burst, no "this enemy just died" implication. */
    public static Explosion hitSpark(int centerX, int centerY) {
        return new Explosion(centerX, centerY, 0.6, false);
    }

    private Explosion(int centerX, int centerY, double scale, boolean fullBurst) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.scale = scale;
        this.fullBurst = fullBurst;
        this.ticksPerFrame = Math.max(1, (int) Math.round(scale));

        Random rand = new Random();

        if (!fullBurst) {
            // Small non-lethal hit: a handful of sparks and a brief flash, nothing more.
            spriteFinished = true;
            flashLife = 4;
            int sparkCount = 4;
            for (int i = 0; i < sparkCount; i++) {
                double angle = rand.nextDouble() * Math.PI * 2;
                double speed = 2.0 + rand.nextDouble() * 2.2;
                particles.add(new Particle(centerX, centerY, Math.cos(angle) * speed, Math.sin(angle) * speed,
                        6 + rand.nextInt(5), Particle.Kind.SPARK, Color.WHITE));
            }
            return;
        }

        spriteFinished = (SHEET == null);
        flashLife = (SHEET == null) ? (int) (8 * Math.max(1.0, scale)) : 0;

        if (SHEET == null) {
            // Fallback: the old procedural fire/smoke burst, only used if the art failed to load.
            int fireCount = (int) (8 * scale);
            for (int i = 0; i < fireCount; i++) {
                double angle = rand.nextDouble() * Math.PI * 2;
                double speed = 1.5 + rand.nextDouble() * 3.0 * scale;
                particles.add(new Particle(centerX, centerY, Math.cos(angle) * speed, Math.sin(angle) * speed,
                        14 + rand.nextInt(10), Particle.Kind.FIRE, rand.nextBoolean() ? Color.ORANGE : Color.YELLOW));
            }
            int smokeCount = (int) (5 * scale);
            for (int i = 0; i < smokeCount; i++) {
                double angle = rand.nextDouble() * Math.PI * 2;
                double speed = 0.4 + rand.nextDouble() * 0.8;
                particles.add(new Particle(centerX, centerY, Math.cos(angle) * speed, Math.sin(angle) * speed,
                        26 + rand.nextInt(14), Particle.Kind.SMOKE, new Color(95, 95, 95)));
            }
        }

        // Flying debris + sparks always complement the sprite burst (the sheet itself has no directional debris).
        int sparkCount = (int) (5 * scale);
        for (int i = 0; i < sparkCount; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double speed = 3.0 + rand.nextDouble() * 4.0 * scale;
            particles.add(new Particle(centerX, centerY, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    10 + rand.nextInt(8), Particle.Kind.SPARK, Color.WHITE));
        }
        int debrisCount = (int) (3 * scale);
        for (int i = 0; i < debrisCount; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double speed = 1.0 + rand.nextDouble() * 2.5 * scale;
            particles.add(new Particle(centerX, centerY, Math.cos(angle) * speed, Math.sin(angle) * speed - 1,
                    24 + rand.nextInt(12), Particle.Kind.DEBRIS, new Color(120, 90, 70)));
        }
    }

    public void update() {
        if (flashLife > 0) flashLife--;

        if (fullBurst && SHEET != null && !spriteFinished) {
            frameTick++;
            if (frameTick >= ticksPerFrame) {
                frameTick = 0;
                frame++;
                if (frame >= SHEET.getFrameCount()) {
                    spriteFinished = true;
                }
            }
        }

        for (Particle p : particles) p.update();
        particles.removeIf(Particle::isDead);
    }

    public boolean isFinished() {
        return spriteFinished && flashLife <= 0 && particles.isEmpty();
    }

    public void draw(Graphics2D g) {
        if (fullBurst && SHEET != null && !spriteFinished) {
            int size = (int) (BASE_DISPLAY_SIZE * scale);
            SHEET.draw(g, frame, centerX - size / 2, centerY - size / 2, size);
        } else if (flashLife > 0) {
            float alpha = Math.max(0f, Math.min(1f, flashLife / 8f));
            Composite original = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(Color.WHITE);
            int r = fullBurst ? 18 : 10;
            g.fillOval(centerX - r / 2, centerY - r / 2, r, r);
            g.setComposite(original);
        }
        for (Particle p : particles) p.draw(g);
    }
}
