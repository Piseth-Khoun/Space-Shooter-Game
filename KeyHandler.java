import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Captures keyboard input and exposes it as simple state that GamePanel
 * reads on every update tick. Player 1 and Player 2 each have their own
 * independent key set (see the "How To Play" screen in-game for the
 * full controller-style mapping):
 *
 *   P1: WASD move, Space shoot, [ / ] aim, Q weapon cycle
 *   P2: Arrows move, / shoot, , / . aim, \ weapon cycle
 *   Shared: Enter = OK/Select (A), Escape = Cancel (B), P = Settings/Pause,
 *           M = quick mute toggle
 */
public class KeyHandler implements KeyListener {

    private boolean p1Up, p1Down, p1Left, p1Right, p1Shoot, p1AimLeft, p1AimRight;
    private boolean p2Up, p2Down, p2Left, p2Right, p2Shoot, p2AimLeft, p2AimRight;
    private final GamePanel gamePanel;

    public KeyHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // not used
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        switch (code) {
            case KeyEvent.VK_UP:
                p2Up = true;
                gamePanel.onMenuUp();
                break;
            case KeyEvent.VK_W:
                p1Up = true;
                gamePanel.onMenuUp();
                break;
            case KeyEvent.VK_DOWN:
                p2Down = true;
                gamePanel.onMenuDown();
                break;
            case KeyEvent.VK_S:
                p1Down = true;
                gamePanel.onMenuDown();
                break;
            case KeyEvent.VK_LEFT:
                p2Left = true;
                break;
            case KeyEvent.VK_A:
                p1Left = true;
                break;
            case KeyEvent.VK_RIGHT:
                p2Right = true;
                break;
            case KeyEvent.VK_D:
                p1Right = true;
                break;
            case KeyEvent.VK_SPACE:
                p1Shoot = true;
                break;
            case KeyEvent.VK_SLASH:
                p2Shoot = true;
                break;
            case KeyEvent.VK_OPEN_BRACKET:
                p1AimLeft = true;
                break;
            case KeyEvent.VK_CLOSE_BRACKET:
                p1AimRight = true;
                break;
            case KeyEvent.VK_COMMA:
                p2AimLeft = true;
                break;
            case KeyEvent.VK_PERIOD:
                p2AimRight = true;
                break;
            case KeyEvent.VK_Q:
                gamePanel.cycleWeapon(1);
                break;
            case KeyEvent.VK_BACK_SLASH:
                gamePanel.cycleWeapon(2);
                break;
            case KeyEvent.VK_ENTER:
                gamePanel.onEnterPressed();
                break;
            case KeyEvent.VK_ESCAPE:
                gamePanel.onCancelPressed();
                break;
            case KeyEvent.VK_P:
                gamePanel.togglePause();
                break;
            case KeyEvent.VK_M:
                gamePanel.toggleMute();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        switch (code) {
            case KeyEvent.VK_UP:
                p2Up = false;
                break;
            case KeyEvent.VK_W:
                p1Up = false;
                break;
            case KeyEvent.VK_DOWN:
                p2Down = false;
                break;
            case KeyEvent.VK_S:
                p1Down = false;
                break;
            case KeyEvent.VK_LEFT:
                p2Left = false;
                break;
            case KeyEvent.VK_A:
                p1Left = false;
                break;
            case KeyEvent.VK_RIGHT:
                p2Right = false;
                break;
            case KeyEvent.VK_D:
                p1Right = false;
                break;
            case KeyEvent.VK_SPACE:
                p1Shoot = false;
                break;
            case KeyEvent.VK_SLASH:
                p2Shoot = false;
                break;
            case KeyEvent.VK_OPEN_BRACKET:
                p1AimLeft = false;
                break;
            case KeyEvent.VK_CLOSE_BRACKET:
                p1AimRight = false;
                break;
            case KeyEvent.VK_COMMA:
                p2AimLeft = false;
                break;
            case KeyEvent.VK_PERIOD:
                p2AimRight = false;
                break;
        }
    }

    // Player 1 (also doubles as the sole player's controls in 1-player mode)
    public boolean isUp() { return p1Up || p2Up; }
    public boolean isDown() { return p1Down || p2Down; }
    public boolean isLeft() { return p1Left || p2Left; }
    public boolean isRight() { return p1Right || p2Right; }
    public boolean isSpace() { return p1Shoot || p2Shoot; }
    public boolean isAimLeft() { return p1AimLeft || p2AimLeft; }
    public boolean isAimRight() { return p1AimRight || p2AimRight; }

    // Player 1 exclusively (used once 2-player mode is active)
    public boolean isP1Up() { return p1Up; }
    public boolean isP1Down() { return p1Down; }
    public boolean isP1Left() { return p1Left; }
    public boolean isP1Right() { return p1Right; }
    public boolean isP1Shoot() { return p1Shoot; }
    public boolean isP1AimLeft() { return p1AimLeft; }
    public boolean isP1AimRight() { return p1AimRight; }

    // Player 2 exclusively (used once 2-player mode is active)
    public boolean isP2Up() { return p2Up; }
    public boolean isP2Down() { return p2Down; }
    public boolean isP2Left() { return p2Left; }
    public boolean isP2Right() { return p2Right; }
    public boolean isP2Shoot() { return p2Shoot; }
    public boolean isP2AimLeft() { return p2AimLeft; }
    public boolean isP2AimRight() { return p2AimRight; }
}
