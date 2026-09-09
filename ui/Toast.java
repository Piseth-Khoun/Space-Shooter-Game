package ui;

/**
 * A short-lived on-screen notification (level-up, boss incoming,
 * achievement unlocked). GamePanel owns a list of these, ticks
 * life down each frame, and UI.drawToasts renders whatever remains.
 */
public class Toast {

    public final String title;
    public final String subtitle;
    public int life;
    public final int maxLife;

    public Toast(String title, String subtitle, int life) {
        this.title = title;
        this.subtitle = subtitle;
        this.life = life;
        this.maxLife = life;
    }
}
