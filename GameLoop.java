/**
 * Drives the game at a fixed update rate (60 times per second),
 * calling GamePanel.update() then triggering a repaint each tick.
 */
public class GameLoop implements Runnable {

    private static final int TARGET_FPS = 60;
    private static final long OPTIMAL_TIME_NS = 1_000_000_000L / TARGET_FPS;

    private final GamePanel gamePanel;
    private volatile boolean running = false;
    private Thread thread;

    public GameLoop(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void start() {
        if (running) return;
        running = true;
        thread = new Thread(this, "GameLoop");
        thread.start();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / (double) OPTIMAL_TIME_NS;
            lastTime = now;

            while (delta >= 1) {
                gamePanel.update();
                gamePanel.repaint();
                delta--;
            }

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }
}
