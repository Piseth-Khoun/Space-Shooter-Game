package powerup;

import java.awt.*;
import java.util.Random;

public class PowerUp {

    public enum Type {
        LIFE, SPEED_BOOST, RAPID_FIRE, SHIELD, DOUBLE_SCORE, BOMB, WEAPON_UPGRADE
    }

    private final Type type;
    private int x;
    private int y;
    private final int width = 24;
    private final int height = 24;
    private final int speed = 3;

    private static final Random RANDOM = new Random();

    public PowerUp(int x, int y) {
        this.x = x;
        this.y = y;
        this.type = Type.values()[RANDOM.nextInt(Type.values().length)];
    }

    public PowerUp(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void update() {
        y += speed;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, width, height, 6, 6);

        String symbol = switch (type) {
            case LIFE -> "♥";
            case SPEED_BOOST -> "⚡";
            case RAPID_FIRE -> "🔫";
            case SHIELD -> "🛡";
            case DOUBLE_SCORE -> "💎";
            case BOMB -> "💥";
            case WEAPON_UPGRADE -> "⬆";
        };

        g.setFont(new Font("Consolas", Font.BOLD, 16));
        g.setColor(Color.BLACK);
        g.drawString(symbol, x + 4, y + 17);
    }

    public boolean isOffScreen(int panelHeight) {
        return y > panelHeight;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public Type getType() {
        return type;
    }
}
