package ui;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import manager.Achievement;

/**
 * Handles all on-screen text/UI rendering: HUD, menus, screens and
 * toast notifications.
 */
public class UI {

    private UI() {
        // static utility class, no instances
    }

    public static void drawHUD(Graphics2D g, int score, int level, int fps, boolean muted,
                                boolean twoPlayer, int health1, int maxHealth1, String weaponLabel1,
                                int health2, int maxHealth2, String weaponLabel2) {
        g.setFont(new Font("Consolas", Font.BOLD, 18));
        g.setColor(new Color(255, 255, 255, 230));
        g.drawString("Score: " + score, 15, 24);
        g.drawString("Level: " + level, 15, 46);
        g.drawString("FPS: " + fps, 15, 68);

        g.setFont(new Font("Consolas", Font.PLAIN, 13));
        g.setColor(muted ? new Color(190, 100, 100) : new Color(140, 200, 140));
        g.drawString(muted ? "Sound: OFF (M)" : "Sound: ON (M)", 15, 88);

        // Player 1 info, top-right
        g.setFont(new Font("Consolas", Font.BOLD, 15));
        g.setColor(new Color(255, 255, 255, 230));
        String p1Label = twoPlayer ? "P1: " + weaponLabel1 : weaponLabel1;
        g.drawString(p1Label, 350, 22);
        drawHearts(g, 350, 28, health1, maxHealth1, Color.RED);

        if (twoPlayer) {
            g.setColor(new Color(255, 255, 255, 230));
            g.drawString("P2: " + weaponLabel2, 350, 60);
            drawHearts(g, 350, 66, health2, maxHealth2, new Color(90, 220, 140));
        }
    }

    private static void drawHearts(Graphics2D g, int x, int y, int health, int maxHealth, Color color) {
        int heartX = x;
        for (int i = 0; i < maxHealth; i++) {
            g.setColor(i < health ? color : new Color(80, 80, 80));
            g.fillOval(heartX, y, 14, 14);
            heartX += 19;
        }
    }

    public static void drawMainMenu(Graphics2D g, int panelWidth, int panelHeight, int selected,
                                     String[] options, int totalCoins) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, panelWidth, panelHeight);

        g.setColor(Color.CYAN);
        g.setFont(new Font("Consolas", Font.BOLD, 40));
        centerString(g, "SPACE SHOOTER", panelWidth, panelHeight / 2 - 190);

        g.setColor(Color.ORANGE);
        g.setFont(new Font("Consolas", Font.BOLD, 16));
        centerString(g, "Coins: " + totalCoins, panelWidth, panelHeight / 2 - 158);

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Consolas", Font.PLAIN, 13));
        centerString(g, "See How To Play for full controls", panelWidth, panelHeight / 2 - 132);

        int startY = panelHeight / 2 - 100;
        g.setFont(new Font("Consolas", Font.BOLD, 23));
        for (int i = 0; i < options.length; i++) {
            boolean sel = i == selected;
            g.setColor(sel ? Color.YELLOW : Color.WHITE);
            String label = (sel ? "> " : "  ") + options[i];
            centerString(g, label, panelWidth, startY + i * 40);
        }

        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        centerString(g, "Up/Down to choose, Enter to select", panelWidth, panelHeight - 40);
    }

    public static void drawSelectPlayers(Graphics2D g, int panelWidth, int panelHeight, int selected, String[] options) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, panelWidth, panelHeight);

        g.setColor(Color.CYAN);
        g.setFont(new Font("Consolas", Font.BOLD, 30));
        centerString(g, "HOW MANY PLAYERS?", panelWidth, panelHeight / 2 - 90);

        int startY = panelHeight / 2 - 20;
        g.setFont(new Font("Consolas", Font.BOLD, 24));
        for (int i = 0; i < options.length; i++) {
            boolean sel = i == selected;
            g.setColor(sel ? Color.YELLOW : Color.WHITE);
            String label = (sel ? "> " : "  ") + options[i];
            centerString(g, label, panelWidth, startY + i * 44);
        }

        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        centerString(g, "Up/Down to choose, Enter to select, Escape to go back", panelWidth, panelHeight - 40);
    }

    public static void drawSettings(Graphics2D g, int panelWidth, int panelHeight, boolean muted) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, panelWidth, panelHeight);

        g.setColor(Color.CYAN);
        g.setFont(new Font("Consolas", Font.BOLD, 30));
        centerString(g, "SETTINGS", panelWidth, panelHeight / 2 - 90);

        g.setFont(new Font("Consolas", Font.BOLD, 22));
        g.setColor(muted ? new Color(220, 110, 110) : new Color(120, 220, 140));
        centerString(g, "Sound:  " + (muted ? "OFF" : "ON"), panelWidth, panelHeight / 2 - 10);

        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        centerString(g, "Press Enter to toggle", panelWidth, panelHeight / 2 + 30);
        centerString(g, "Press Escape to go back", panelWidth, panelHeight - 40);
    }

    public static void drawHowToPlay(Graphics2D g, int panelWidth, int panelHeight) {
        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, 0, panelWidth, panelHeight);

        g.setColor(Color.CYAN);
        g.setFont(new Font("Consolas", Font.BOLD, 28));
        centerString(g, "HOW TO PLAY", panelWidth, 55);

        String[][] rows = {
                { "RT", "Shoot", "Space", "/" },
                { "R3", "Aim Gun", "[ / ]", ", / ." },
                { "L3", "Move", "WASD", "Arrows" },
                { "RB", "Upgrade Weapon", "Q", "\\" },
                { "A", "OK / Select", "Enter", "Enter" },
                { "B", "Cancel", "Escape", "Escape" },
                { "\u2261", "Settings / Pause", "P", "P" },
        };

        int colButton = 60, colAction = 150, colP1 = 360, colP2 = 470;
        int y = 100;

        g.setFont(new Font("Consolas", Font.BOLD, 14));
        g.setColor(Color.YELLOW);
        g.drawString("Button", colButton, y);
        g.drawString("Action", colAction, y);
        g.drawString("Player 1", colP1, y);
        g.drawString("Player 2", colP2, y);
        y += 12;
        g.setColor(new Color(255, 255, 255, 80));
        g.drawLine(colButton, y, panelWidth - 30, y);
        y += 30;

        g.setFont(new Font("Consolas", Font.PLAIN, 15));
        for (String[] row : rows) {
            g.setColor(Color.ORANGE);
            g.drawString(row[0], colButton, y);
            g.setColor(Color.WHITE);
            g.drawString(row[1], colAction, y);
            g.setColor(new Color(150, 220, 255));
            g.drawString(row[2], colP1, y);
            g.setColor(new Color(150, 255, 190));
            g.drawString(row[3], colP2, y);
            y += 34;
        }

        y += 10;
        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        g.setColor(Color.LIGHT_GRAY);
        centerString(g, "In 1-Player mode, either column moves the one ship.", panelWidth, y);
        y += 20;
        centerString(g, "Tip: a real gamepad works too - map its buttons to these", panelWidth, y);
        y += 18;
        centerString(g, "keys with free software like Steam Input.", panelWidth, y);

        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        centerString(g, "Press Enter or Escape to go back", panelWidth, panelHeight - 30);
    }

    public static void drawPauseMenu(Graphics2D g, int panelWidth, int panelHeight, int selected, String[] options) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, panelWidth, panelHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 32));
        centerString(g, "PAUSED", panelWidth, panelHeight / 2 - 120);

        int startY = panelHeight / 2 - 50;
        g.setFont(new Font("Consolas", Font.BOLD, 22));
        for (int i = 0; i < options.length; i++) {
            boolean sel = i == selected;
            g.setColor(sel ? Color.YELLOW : Color.WHITE);
            String label = (sel ? "> " : "  ") + options[i];
            centerString(g, label, panelWidth, startY + i * 38);
        }

        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        centerString(g, "Up/Down to choose, Enter to select, P/Escape to resume", panelWidth, panelHeight - 40);
    }

    public static void drawGameOverScreen(Graphics2D g, int panelWidth, int panelHeight, int score,
                                           int coinsEarned, int totalCoins) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, panelWidth, panelHeight);

        g.setColor(Color.RED);
        g.setFont(new Font("Consolas", Font.BOLD, 40));
        centerString(g, "GAME OVER", panelWidth, panelHeight / 2 - 70);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 22));
        centerString(g, "Final Score: " + score, panelWidth, panelHeight / 2 - 25);

        g.setColor(Color.ORANGE);
        g.setFont(new Font("Consolas", Font.BOLD, 20));
        centerString(g, "+ " + coinsEarned + " Coins!", panelWidth, panelHeight / 2 + 10);

        g.setFont(new Font("Consolas", Font.PLAIN, 15));
        g.setColor(Color.LIGHT_GRAY);
        centerString(g, "Total Coins: " + totalCoins, panelWidth, panelHeight / 2 + 38);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 20));
        centerString(g, "Press ENTER to continue", panelWidth, panelHeight / 2 + 85);
    }

    public static void drawHighScores(Graphics2D g, int panelWidth, int panelHeight, List<Integer> scores) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, panelWidth, panelHeight);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 30));
        centerString(g, "HIGH SCORES", panelWidth, 150);

        g.setFont(new Font("Consolas", Font.PLAIN, 20));
        int y = 220;
        if (scores.isEmpty()) {
            g.setColor(Color.LIGHT_GRAY);
            centerString(g, "No scores yet \u2014 go set one!", panelWidth, y);
        } else {
            for (int i = 0; i < scores.size(); i++) {
                g.setColor(i == 0 ? Color.YELLOW : Color.WHITE);
                centerString(g, (i + 1) + ".  " + scores.get(i), panelWidth, y + i * 36);
            }
        }

        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        centerString(g, "Press ENTER or Escape to go back", panelWidth, panelHeight - 60);
    }

    public static void drawAchievements(Graphics2D g, int panelWidth, int panelHeight, Collection<Achievement> achievements) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, panelWidth, panelHeight);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 28));
        centerString(g, "ACHIEVEMENTS", panelWidth, 110);

        int y = 170;
        for (Achievement a : achievements) {
            g.setFont(new Font("Consolas", Font.BOLD, 16));
            g.setColor(a.isUnlocked() ? Color.GREEN : Color.DARK_GRAY);
            String mark = a.isUnlocked() ? "[X]  " : "[ ]  ";
            centerString(g, mark + a.getTitle(), panelWidth, y);

            g.setFont(new Font("Consolas", Font.PLAIN, 13));
            g.setColor(Color.LIGHT_GRAY);
            centerString(g, a.getDescription(), panelWidth, y + 22);

            y += 62;
        }

        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        centerString(g, "Press ENTER or Escape to go back", panelWidth, panelHeight - 40);
    }

    public static void drawToasts(Graphics2D g, int panelWidth, List<Toast> toasts) {
        int y = 130;
        for (Toast t : toasts) {
            float alpha = Math.max(0f, Math.min(1f, t.life / 20f));
            Composite original = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g.setFont(new Font("Consolas", Font.BOLD, 16));
            FontMetrics fm = g.getFontMetrics();
            String text = t.title + (t.subtitle != null && !t.subtitle.isEmpty() ? "  \u2014  " + t.subtitle : "");
            int textWidth = fm.stringWidth(text);
            int boxX = (panelWidth - textWidth) / 2 - 14;
            int boxWidth = textWidth + 28;

            g.setColor(new Color(15, 15, 30, 215));
            g.fillRoundRect(boxX, y, boxWidth, 30, 10, 10);
            g.setColor(Color.YELLOW);
            g.drawRoundRect(boxX, y, boxWidth, 30, 10, 10);
            g.setColor(Color.WHITE);
            g.drawString(text, boxX + 14, y + 20);

            g.setComposite(original);
            y += 36;
        }
    }

    private static void centerString(Graphics2D g, String text, int panelWidth, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (panelWidth - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }
}
