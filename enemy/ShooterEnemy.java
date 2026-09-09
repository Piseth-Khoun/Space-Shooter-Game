package enemy;

import java.util.List;

public class ShooterEnemy extends Enemy {

    public ShooterEnemy(int panelWidth, double speed) {
        super(panelWidth, 54, 42, 2, speed * 0.9, 25, MovePattern.ZIGZAG);
        this.x = RAND.nextInt(Math.max(1, panelWidth - width));
        this.y = -height;
        this.attackCooldownMax = 120;
        this.attackCooldown = 60;
    }

    @Override
    public void update() {
        moveZigZag(800);
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
        return spreadShot(targetX, targetY, 3);
    }
}
