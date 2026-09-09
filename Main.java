import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Entry point: creates the game window and starts the game loop.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("2D Space Shooter");
            GamePanel gamePanel = new GamePanel();

            frame.add(gamePanel);
            frame.pack();
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            gamePanel.requestFocusInWindow();

            GameLoop loop = new GameLoop(gamePanel);
            loop.start();
        });
    }
}
