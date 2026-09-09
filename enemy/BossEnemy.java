package enemy;

import java.util.List;

public class BossEnemy extends Enemy {

    public BossEnemy(int panelWidth, double speed) {
        super(panelWidth, 120, 96, 15, speed * 0.6, 200, MovePattern.SIDE_SWEEP);
        this.x = panelWidth / 2 - width / 2;
        this.y = -height;
        this.attackCooldownMax = 90;
        this.attackCooldown = 40;
    }

    @Override
    public void update() {
        moveSideSweep(panelWidth);
        if (attackCooldown > 0) attackCooldown--;
        if (hitFlash > 0) hitFlash--;
    }

    @Override
    public boolean canShoot() {
        return attackCooldown == 0;
    }

    @Override
    public List<EnemyBullet> fireAt(int targetX, int targetY) {
        attackCooldown = attackCooldownMax;
        return spreadShot(targetX, targetY, 5);
    }
}
