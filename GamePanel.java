import bullet.Bullet;
import collision.CollisionChecker;
import enemy.BasicEnemy;
import enemy.BossEnemy;
import enemy.Enemy;
import enemy.EnemyBullet;
import enemy.FastEnemy;
import enemy.ShooterEnemy;
import enemy.TankEnemy;
import enemy.ZigzagEnemy;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import manager.Achievement;
import manager.AchievementManager;
import manager.SoundManager;
import player.Player;
import powerup.PowerUp;
import ui.Toast;
import ui.UI;
import util.CoinManager;
import util.HighScoreManager;
import util.ImageLoader;

/**
 * The main game surface. Owns all game objects and state, runs the
 * update logic each tick, and renders everything.
 */
public class GamePanel extends JPanel {

    public static final int PANEL_WIDTH = 600;
    public static final int PANEL_HEIGHT = 800;

    public enum GameState {
        MAIN_MENU, SELECT_PLAYERS, HOW_TO_PLAY, SETTINGS,
        HIGH_SCORES, ACHIEVEMENTS, PLAYING, PAUSED, GAME_OVER
    }

    private static final String[] MAIN_MENU_OPTIONS =
            { "Start", "How To Play", "High Scores", "Achievements", "Settings", "Exit" };
    private static final String[] PAUSE_MENU_OPTIONS =
            { "Resume", "Restart", "Settings", "Main Menu", "Exit" };
    private static final String[] SELECT_PLAYERS_OPTIONS = { "1 Player", "2 Player" };

    private GameState state = GameState.MAIN_MENU;
    private GameState settingsReturnState = GameState.MAIN_MENU;
    private int mainMenuIndex = 0;
    private int pauseMenuIndex = 0;
    private int selectPlayersIndex = 0;

    private boolean twoPlayerMode = false;
    private final Player player;
    private Player player2;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<EnemyBullet> enemyBullets = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();
    private final List<Explosion> explosions = new ArrayList<>();
    private final List<Toast> toasts = new ArrayList<>();

    private final KeyHandler keyHandler;
    private final Random random = new Random();
    private final Starfield starfield = new Starfield(PANEL_WIDTH, PANEL_HEIGHT);
    private final AchievementManager achievementManager = new AchievementManager();

    private int score;
    private int level = 1;
    private int previousLevel = 1;
    private int shootCooldown = 0;
    private int shootCooldown2 = 0;
    private static final int SHOOT_COOLDOWN_MAX = 12;
    private static final int RAPID_FIRE_COOLDOWN_MAX = 5;
    private int scoreMultiplier = 1;
    private int scoreMultiplierTimer = 0;
    private boolean bossActive = false;

    private int coinsEarnedThisRun = 0;
    private int totalCoins = CoinManager.loadTotalCoins();

    private int framesSinceStart = 0;
    private int spawnTimer = 0;
    private int spawnInterval = 70;
    private static final int MIN_SPAWN_INTERVAL = 18;
    private double enemySpeed = 2.0;

    private int frameCounter = 0;
    private int fps = 60;
    private long lastFpsSample = System.nanoTime();

    private static final BufferedImage backgroundImage = ImageLoader.load("background.png");

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        keyHandler = new KeyHandler(this);
        addKeyListener(keyHandler);

        player = new Player(PANEL_WIDTH, PANEL_HEIGHT);
    }

    public void update() {
        starfield.update();

        for (int i = toasts.size() - 1; i >= 0; i--) {
            Toast t = toasts.get(i);
            t.life--;
            if (t.life <= 0) {
                toasts.remove(i);
            }
        }

        if (state == GameState.PLAYING) {
            updatePlaying();
        }
        updateFpsCounter();
    }

    private void updateFpsCounter() {
        frameCounter++;
        long now = System.nanoTime();
        if (now - lastFpsSample >= 1_000_000_000L) {
            fps = frameCounter;
            frameCounter = 0;
            lastFpsSample = now;
        }
    }

    private void updatePlaying() {
        framesSinceStart++;

        boolean p1Alive = !player.isDead();
        boolean p2Alive = twoPlayerMode && player2 != null && !player2.isDead();

        if (p1Alive) {
            if (twoPlayerMode) {
                player.update(keyHandler.isP1Up(), keyHandler.isP1Down(), keyHandler.isP1Left(), keyHandler.isP1Right(),
                        keyHandler.isP1AimLeft(), keyHandler.isP1AimRight(), PANEL_WIDTH, PANEL_HEIGHT);
            } else {
                player.update(keyHandler.isUp(), keyHandler.isDown(), keyHandler.isLeft(), keyHandler.isRight(),
                        keyHandler.isAimLeft(), keyHandler.isAimRight(), PANEL_WIDTH, PANEL_HEIGHT);
            }
        }
        if (twoPlayerMode && player2 != null && p2Alive) {
            player2.update(keyHandler.isP2Up(), keyHandler.isP2Down(), keyHandler.isP2Left(), keyHandler.isP2Right(),
                    keyHandler.isP2AimLeft(), keyHandler.isP2AimRight(), PANEL_WIDTH, PANEL_HEIGHT);
        }

        if (scoreMultiplierTimer > 0) {
            scoreMultiplierTimer--;
        } else {
            scoreMultiplier = 1;
        }

        boolean p1ShootInput = twoPlayerMode ? keyHandler.isP1Shoot() : keyHandler.isSpace();
        if (shootCooldown > 0) shootCooldown--;
        if (p1Alive && p1ShootInput && shootCooldown == 0) {
            fireVolley(player);
            shootCooldown = (player.getRapidFireTimer() > 0) ? RAPID_FIRE_COOLDOWN_MAX : SHOOT_COOLDOWN_MAX;
            SoundManager.playShoot();
        }

        if (twoPlayerMode && player2 != null) {
            if (shootCooldown2 > 0) shootCooldown2--;
            if (p2Alive && keyHandler.isP2Shoot() && shootCooldown2 == 0) {
                fireVolley(player2);
                shootCooldown2 = (player2.getRapidFireTimer() > 0) ? RAPID_FIRE_COOLDOWN_MAX : SHOOT_COOLDOWN_MAX;
                SoundManager.playShoot();
            }
        }

        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();
            if (b.isOffScreen()) {
                bullets.remove(i);
            }
        }

        level = Math.max(1, 1 + score / 150);
        if (level != previousLevel) {
            toasts.add(new Toast("LEVEL " + level, null, 130));
            achievementManager.recordLevelReached(level);
            SoundManager.playLevelUp();
            previousLevel = level;
        }
        spawnInterval = Math.max(MIN_SPAWN_INTERVAL, 70 - level * 4);
        enemySpeed = 2.0 + (level - 1) * 0.35;

        spawnTimer++;
        if (spawnTimer >= spawnInterval && !bossActive) {
            spawnTimer = 0;
            enemies.add(spawnEnemy());
        }

        if (score > 0 && score % 500 == 0 && !bossActive) {
            bossActive = true;
            enemies.add(new BossEnemy(PANEL_WIDTH, enemySpeed));
            toasts.add(new Toast("BOSS INCOMING", "Level " + level, 150));
            SoundManager.playBossAlert();
        }

        Player nearestTarget = twoPlayerMode && p2Alive && !p1Alive ? player2 : player;

        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy en = enemies.get(i);
            en.update();
            if (en.canShoot() && en.getY() > 60 && random.nextInt(240) == 0) {
                enemyBullets.addAll(en.fireAt(nearestTarget.getX() + nearestTarget.getWidth() / 2,
                        nearestTarget.getY() + nearestTarget.getHeight() / 2));
            }
            if (en.isOffScreen(PANEL_WIDTH, PANEL_HEIGHT)) {
                if (en instanceof BossEnemy) {
                    bossActive = false;
                }
                enemies.remove(i);
            }
        }

        for (int i = enemyBullets.size() - 1; i >= 0; i--) {
            EnemyBullet bullet = enemyBullets.get(i);
            bullet.update();
            if (bullet.isOffScreen(PANEL_WIDTH, PANEL_HEIGHT)) {
                enemyBullets.remove(i);
            }
        }

        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy en = enemies.get(i);
            for (int j = bullets.size() - 1; j >= 0; j--) {
                Bullet b = bullets.get(j);
                if (CollisionChecker.checkCollision(en.getBounds(), b.getBounds())) {
                    en.takeDamage(1);
                    bullets.remove(j);
                    if (en.isDead()) {
                        boolean wasBoss = en instanceof BossEnemy;
                        explosions.add(wasBoss
                                ? new Explosion(en.getX() + en.getWidth() / 2, en.getY() + en.getHeight() / 2, 2.4)
                                : new Explosion(en.getX() + en.getWidth() / 2, en.getY() + en.getHeight() / 2));
                        score += en.getScoreValue() * scoreMultiplier;
                        achievementManager.recordKill();
                        if (wasBoss) {
                            achievementManager.recordBossKill();
                            SoundManager.playBigExplosion();
                        } else {
                            SoundManager.playExplosion();
                        }
                        if (random.nextInt(100) < 18) {
                            powerUps.add(new PowerUp(en.getX(), en.getY()));
                        }
                        enemies.remove(i);
                        if (wasBoss) {
                            bossActive = false;
                        }
                    } else {
                        explosions.add(Explosion.hitSpark(en.getX() + en.getWidth() / 2,
                                en.getY() + en.getHeight() / 2));
                    }
                    break;
                }
            }
        }

        if (p1Alive) {
            p1Alive = handlePlayerDamage(player);
        }
        if (twoPlayerMode && p2Alive) {
            p2Alive = handlePlayerDamage(player2);
        }

        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            powerUp.update();
            if (powerUp.isOffScreen(PANEL_HEIGHT)) {
                powerUps.remove(i);
                continue;
            }
            if (p1Alive && CollisionChecker.checkCollision(powerUp.getBounds(), player.getBounds())) {
                applyPowerUp(player, powerUp);
                powerUps.remove(i);
            } else if (twoPlayerMode && p2Alive
                    && CollisionChecker.checkCollision(powerUp.getBounds(), player2.getBounds())) {
                applyPowerUp(player2, powerUp);
                powerUps.remove(i);
            }
        }

        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion ex = explosions.get(i);
            ex.update();
            if (ex.isFinished()) {
                explosions.remove(i);
            }
        }

        for (Achievement a : achievementManager.drainPopups()) {
            toasts.add(new Toast("ACHIEVEMENT UNLOCKED", a.getTitle(), 170));
            SoundManager.playAchievement();
        }

        boolean everyoneDead = twoPlayerMode ? (player.isDead() && (player2 == null || player2.isDead())) : player.isDead();
        if (everyoneDead) {
            coinsEarnedThisRun = score;
            totalCoins = CoinManager.addCoins(coinsEarnedThisRun);
            HighScoreManager.saveScore(score);
            SoundManager.playGameOver();
            state = GameState.GAME_OVER;
        }
    }

    /** Returns true if the player is still alive after resolving all damage this frame. */
    private boolean handlePlayerDamage(Player p) {
        for (int i = enemyBullets.size() - 1; i >= 0; i--) {
            EnemyBullet enemyBullet = enemyBullets.get(i);
            if (CollisionChecker.checkCollision(p.getBounds(), enemyBullet.getBounds())) {
                enemyBullets.remove(i);
                damagePlayer(p);
            }
        }
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy en = enemies.get(i);
            if (CollisionChecker.checkCollision(en.getBounds(), p.getBounds())) {
                explosions.add(new Explosion(en.getX() + en.getWidth() / 2, en.getY() + en.getHeight() / 2));
                enemies.remove(i);
                damagePlayer(p);
            }
        }
        return !p.isDead();
    }

    private void damagePlayer(Player p) {
        int healthBefore = p.getHealth();
        p.takeDamage();
        if (p.getHealth() < healthBefore) {
            achievementManager.recordDamageTaken();
            SoundManager.playHit();
        }
    }

    private void fireVolley(Player p) {
        int shotCount = p.getShotCount();
        int spacing = 18;
        int centerX = p.getX() + p.getWidth() / 2;
        int bulletY = p.getY();
        double aim = p.getAimAngleDeg();

        if (shotCount == 1) {
            bullets.add(new Bullet(centerX - 5, bulletY, aim));
        } else if (shotCount == 2) {
            bullets.add(new Bullet(centerX - 18, bulletY + 6, aim));
            bullets.add(new Bullet(centerX + 8, bulletY + 6, aim));
        } else if (shotCount == 3) {
            bullets.add(new Bullet(centerX - spacing, bulletY + 4, aim));
            bullets.add(new Bullet(centerX, bulletY, aim));
            bullets.add(new Bullet(centerX + spacing, bulletY + 4, aim));
        } else {
            bullets.add(new Bullet(centerX - spacing, bulletY + 4, aim));
            bullets.add(new Bullet(centerX, bulletY, aim));
            bullets.add(new Bullet(centerX + spacing, bulletY + 4, aim));
            bullets.add(new Bullet(centerX - 10, bulletY - 4, aim));
            bullets.add(new Bullet(centerX + 10, bulletY - 4, aim));
        }
    }

    /** playerNumber: 1 cycles Player 1's weapon, 2 cycles Player 2's (only when 2-player mode is active). */
    public void cycleWeapon(int playerNumber) {
        if (state != GameState.PLAYING) return;
        if (playerNumber == 1) {
            player.cycleWeaponMode();
        } else if (twoPlayerMode && player2 != null) {
            player2.cycleWeaponMode();
        }
    }

    public void toggleMute() {
        SoundManager.toggleMuted();
    }

    public void onMenuUp() {
        if (state == GameState.MAIN_MENU) {
            mainMenuIndex = (mainMenuIndex - 1 + MAIN_MENU_OPTIONS.length) % MAIN_MENU_OPTIONS.length;
            SoundManager.playMenuMove();
        } else if (state == GameState.PAUSED) {
            pauseMenuIndex = (pauseMenuIndex - 1 + PAUSE_MENU_OPTIONS.length) % PAUSE_MENU_OPTIONS.length;
            SoundManager.playMenuMove();
        } else if (state == GameState.SELECT_PLAYERS) {
            selectPlayersIndex = (selectPlayersIndex - 1 + SELECT_PLAYERS_OPTIONS.length) % SELECT_PLAYERS_OPTIONS.length;
            SoundManager.playMenuMove();
        }
    }

    public void onMenuDown() {
        if (state == GameState.MAIN_MENU) {
            mainMenuIndex = (mainMenuIndex + 1) % MAIN_MENU_OPTIONS.length;
            SoundManager.playMenuMove();
        } else if (state == GameState.PAUSED) {
            pauseMenuIndex = (pauseMenuIndex + 1) % PAUSE_MENU_OPTIONS.length;
            SoundManager.playMenuMove();
        } else if (state == GameState.SELECT_PLAYERS) {
            selectPlayersIndex = (selectPlayersIndex + 1) % SELECT_PLAYERS_OPTIONS.length;
            SoundManager.playMenuMove();
        }
    }

    public void onEnterPressed() {
        switch (state) {
            case MAIN_MENU:
                SoundManager.playMenuSelect();
                if (mainMenuIndex == 0) {
                    selectPlayersIndex = 0;
                    state = GameState.SELECT_PLAYERS;
                } else if (mainMenuIndex == 1) {
                    state = GameState.HOW_TO_PLAY;
                } else if (mainMenuIndex == 2) {
                    state = GameState.HIGH_SCORES;
                } else if (mainMenuIndex == 3) {
                    state = GameState.ACHIEVEMENTS;
                } else if (mainMenuIndex == 4) {
                    settingsReturnState = GameState.MAIN_MENU;
                    state = GameState.SETTINGS;
                } else {
                    System.exit(0);
                }
                break;
            case SELECT_PLAYERS:
                SoundManager.playMenuSelect();
                twoPlayerMode = (selectPlayersIndex == 1);
                startGame();
                break;
            case HOW_TO_PLAY:
            case HIGH_SCORES:
            case ACHIEVEMENTS:
                SoundManager.playMenuSelect();
                state = GameState.MAIN_MENU;
                break;
            case SETTINGS:
                SoundManager.toggleMuted();
                SoundManager.playMenuSelect();
                break;
            case PAUSED:
                SoundManager.playMenuSelect();
                if (pauseMenuIndex == 0) {
                    state = GameState.PLAYING;
                } else if (pauseMenuIndex == 1) {
                    startGame();
                } else if (pauseMenuIndex == 2) {
                    settingsReturnState = GameState.PAUSED;
                    state = GameState.SETTINGS;
                } else if (pauseMenuIndex == 3) {
                    state = GameState.MAIN_MENU;
                } else {
                    System.exit(0);
                }
                break;
            case GAME_OVER:
                state = GameState.MAIN_MENU;
                break;
            case PLAYING:
            default:
                break;
        }
    }

    public void onCancelPressed() {
        switch (state) {
            case SELECT_PLAYERS:
            case HOW_TO_PLAY:
            case HIGH_SCORES:
            case ACHIEVEMENTS:
                state = GameState.MAIN_MENU;
                SoundManager.playMenuSelect();
                break;
            case SETTINGS:
                state = settingsReturnState;
                SoundManager.playMenuSelect();
                break;
            case PAUSED:
                state = GameState.PLAYING;
                break;
            case MAIN_MENU:
            case PLAYING:
            case GAME_OVER:
            default:
                break;
        }
    }

    public void togglePause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
            pauseMenuIndex = 0;
        } else if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
        }
    }

    private void startGame() {
        player.reset(PANEL_WIDTH, PANEL_HEIGHT);

        if (twoPlayerMode) {
            if (player2 == null) {
                player2 = new Player(PANEL_WIDTH, PANEL_HEIGHT, new Color(60, 255, 140));
            }
            player2.reset(PANEL_WIDTH, PANEL_HEIGHT);
            player.setPosition(PANEL_WIDTH / 2 - 100, PANEL_HEIGHT - player.getHeight() - 30);
            player2.setPosition(PANEL_WIDTH / 2 + 40, PANEL_HEIGHT - player2.getHeight() - 30);
        }

        enemies.clear();
        bullets.clear();
        enemyBullets.clear();
        powerUps.clear();
        explosions.clear();
        toasts.clear();
        score = 0;
        coinsEarnedThisRun = 0;
        level = 1;
        previousLevel = 1;
        framesSinceStart = 0;
        spawnTimer = 0;
        spawnInterval = 70;
        enemySpeed = 2.0;
        scoreMultiplier = 1;
        scoreMultiplierTimer = 0;
        bossActive = false;
        shootCooldown = 0;
        shootCooldown2 = 0;
        achievementManager.resetRun();
        state = GameState.PLAYING;
    }

    private Enemy spawnEnemy() {
        int roll = random.nextInt(100);
        if (level >= 5 && roll < 10) {
            return new ShooterEnemy(PANEL_WIDTH, enemySpeed);
        }
        if (level >= 4 && roll < 20) {
            return new TankEnemy(PANEL_WIDTH, enemySpeed);
        }
        if (level >= 3 && roll < 35) {
            return new ZigzagEnemy(PANEL_WIDTH, enemySpeed);
        }
        if (level >= 2 && roll < 55) {
            return new FastEnemy(PANEL_WIDTH, enemySpeed);
        }
        return new BasicEnemy(PANEL_WIDTH, enemySpeed);
    }

    private void applyPowerUp(Player p, PowerUp powerUp) {
        switch (powerUp.getType()) {
            case LIFE:
                p.heal();
                SoundManager.playPowerUp();
                break;
            case SPEED_BOOST:
                p.activateSpeedBoost();
                SoundManager.playPowerUp();
                break;
            case RAPID_FIRE:
                p.activateRapidFire();
                SoundManager.playPowerUp();
                break;
            case SHIELD:
                p.activateShield();
                SoundManager.playPowerUp();
                break;
            case DOUBLE_SCORE:
                scoreMultiplier = 2;
                scoreMultiplierTimer = 240;
                SoundManager.playPowerUp();
                break;
            case BOMB:
                for (Enemy en : enemies) {
                    explosions.add(new Explosion(en.getX() + en.getWidth() / 2, en.getY() + en.getHeight() / 2));
                }
                enemies.clear();
                enemyBullets.clear();
                bossActive = false;
                explosions.add(new Explosion(PANEL_WIDTH / 2, PANEL_HEIGHT / 2, 2.0));
                SoundManager.playBigExplosion();
                break;
            case WEAPON_UPGRADE:
                p.cycleWeaponMode();
                SoundManager.playWeaponUpgrade();
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        starfield.draw(g2, backgroundImage);

        if (state == GameState.PLAYING || state == GameState.PAUSED) {
            drawGameObjects(g2);
            UI.drawHUD(g2, score, level, fps, SoundManager.isMuted(), twoPlayerMode,
                    player.getHealth(), player.getMaxHealth(), player.getWeaponLabel(),
                    twoPlayerMode && player2 != null ? player2.getHealth() : 0,
                    twoPlayerMode && player2 != null ? player2.getMaxHealth() : 0,
                    twoPlayerMode && player2 != null ? player2.getWeaponLabel() : "");
            UI.drawToasts(g2, PANEL_WIDTH, toasts);
        }

        switch (state) {
            case MAIN_MENU:
                UI.drawMainMenu(g2, PANEL_WIDTH, PANEL_HEIGHT, mainMenuIndex, MAIN_MENU_OPTIONS, totalCoins);
                break;
            case SELECT_PLAYERS:
                UI.drawSelectPlayers(g2, PANEL_WIDTH, PANEL_HEIGHT, selectPlayersIndex, SELECT_PLAYERS_OPTIONS);
                break;
            case HOW_TO_PLAY:
                UI.drawHowToPlay(g2, PANEL_WIDTH, PANEL_HEIGHT);
                break;
            case SETTINGS:
                UI.drawSettings(g2, PANEL_WIDTH, PANEL_HEIGHT, SoundManager.isMuted());
                break;
            case PAUSED:
                UI.drawPauseMenu(g2, PANEL_WIDTH, PANEL_HEIGHT, pauseMenuIndex, PAUSE_MENU_OPTIONS);
                break;
            case GAME_OVER:
                drawGameObjects(g2);
                UI.drawGameOverScreen(g2, PANEL_WIDTH, PANEL_HEIGHT, score, coinsEarnedThisRun, totalCoins);
                break;
            case HIGH_SCORES:
                UI.drawHighScores(g2, PANEL_WIDTH, PANEL_HEIGHT, HighScoreManager.loadScores());
                break;
            case ACHIEVEMENTS:
                UI.drawAchievements(g2, PANEL_WIDTH, PANEL_HEIGHT, achievementManager.getAll());
                break;
            case PLAYING:
            default:
                break;
        }
    }

    private void drawGameObjects(Graphics2D g2) {
        if (!player.isDead()) {
            player.draw(g2);
        }
        if (twoPlayerMode && player2 != null && !player2.isDead()) {
            player2.draw(g2);
        }
        for (Enemy en : enemies) en.draw(g2);
        for (PowerUp powerUp : powerUps) powerUp.draw(g2);
        for (Bullet b : bullets) b.draw(g2);
        for (EnemyBullet enemyBullet : enemyBullets) enemyBullet.draw(g2);
        for (Explosion ex : explosions) ex.draw(g2);
    }
}
