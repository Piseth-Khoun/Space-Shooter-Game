import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Animated parallax background: three star layers moving at different
 * speeds, drifting rotating asteroids, and the project's own nebula
 * image scrolled seamlessly underneath everything.
 *
 * Replaces the old flat single-layer starfield. Runs every frame
 * regardless of game state, so menus have a living backdrop too.
 */
public class Starfield {

    private static final Random RAND = new Random();

    private final int panelWidth;
    private final int panelHeight;

    // far / mid / near star layers
    private final int[] layerCount = { 55, 35, 18 };
    private final double[] layerSpeed = { 0.35, 0.9, 2.0 };
    private final int[] layerSize = { 1, 2, 3 };
    private final int[][] layerX;
    private final int[][] layerY;

    private double nebulaScrollY = 0;
    private static final double NEBULA_SPEED = 0.3;

    private static final int ASTEROID_COUNT = 5;
    private final double[] asteroidX = new double[ASTEROID_COUNT];
    private final double[] asteroidY = new double[ASTEROID_COUNT];
    private final double[] asteroidSpeed = new double[ASTEROID_COUNT];
    private final double[] asteroidRotation = new double[ASTEROID_COUNT];
    private final double[] asteroidRotationSpeed = new double[ASTEROID_COUNT];
    private final int[] asteroidSize = new int[ASTEROID_COUNT];

    public Starfield(int panelWidth, int panelHeight) {
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;

        layerX = new int[layerCount.length][];
        layerY = new int[layerCount.length][];
        for (int layer = 0; layer < layerCount.length; layer++) {
            layerX[layer] = new int[layerCount[layer]];
            layerY[layer] = new int[layerCount[layer]];
            for (int i = 0; i < layerCount[layer]; i++) {
                layerX[layer][i] = RAND.nextInt(panelWidth);
                layerY[layer][i] = RAND.nextInt(panelHeight);
            }
        }

        for (int i = 0; i < ASTEROID_COUNT; i++) resetAsteroid(i, true);
    }

    private void resetAsteroid(int i, boolean randomStart) {
        asteroidX[i] = RAND.nextInt(Math.max(1, panelWidth));
        asteroidY[i] = randomStart ? RAND.nextInt(panelHeight) : -40;
        asteroidSpeed[i] = 0.6 + RAND.nextDouble() * 0.9;
        asteroidRotation[i] = RAND.nextDouble() * Math.PI * 2;
        asteroidRotationSpeed[i] = (RAND.nextDouble() - 0.5) * 0.03;
        asteroidSize[i] = 14 + RAND.nextInt(18);
    }

    public void update() {
        for (int layer = 0; layer < layerCount.length; layer++) {
            for (int i = 0; i < layerCount[layer]; i++) {
                layerY[layer][i] += layerSpeed[layer];
                if (layerY[layer][i] > panelHeight) {
                    layerY[layer][i] = 0;
                    layerX[layer][i] = RAND.nextInt(panelWidth);
                }
            }
        }

        nebulaScrollY += NEBULA_SPEED;
        if (nebulaScrollY >= panelHeight) nebulaScrollY -= panelHeight;

        for (int i = 0; i < ASTEROID_COUNT; i++) {
            asteroidY[i] += asteroidSpeed[i];
            asteroidRotation[i] += asteroidRotationSpeed[i];
            if (asteroidY[i] - asteroidSize[i] > panelHeight) resetAsteroid(i, false);
        }
    }

    public void draw(Graphics2D g, BufferedImage nebulaImage) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, panelWidth, panelHeight);

        if (nebulaImage != null) {
            int y1 = (int) nebulaScrollY - panelHeight;
            int y2 = (int) nebulaScrollY;
            g.drawImage(nebulaImage, 0, y1, panelWidth, panelHeight, null);
            g.drawImage(nebulaImage, 0, y2, panelWidth, panelHeight, null);
        }

        g.setColor(new Color(255, 255, 255, 130));
        drawLayer(g, 0);
        g.setColor(new Color(255, 255, 255, 185));
        drawLayer(g, 1);

        g.setColor(Color.WHITE);
        drawLayer(g, 2);

        for (int i = 0; i < ASTEROID_COUNT; i++) drawAsteroid(g, i);
    }

    private void drawLayer(Graphics2D g, int layer) {
        int size = layerSize[layer];
        for (int i = 0; i < layerCount[layer]; i++) {
            g.fillOval(layerX[layer][i], layerY[layer][i], size, size);
        }
    }

    private void drawAsteroid(Graphics2D g, int i) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(asteroidX[i], asteroidY[i]);
        g2.rotate(asteroidRotation[i]);
        int s = asteroidSize[i];
        int[] xs = { -s / 2, -s / 4, s / 3, s / 2, s / 4, -s / 3 };
        int[] ys = { -s / 4, -s / 2, -s / 3, s / 5, s / 2, s / 3 };
        g2.setColor(new Color(112, 102, 96));
        g2.fillPolygon(xs, ys, xs.length);
        g2.setColor(new Color(70, 64, 60));
        g2.drawPolygon(xs, ys, xs.length);
        g2.dispose();
    }
}
