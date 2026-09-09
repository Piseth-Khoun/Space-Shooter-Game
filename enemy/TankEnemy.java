package enemy;

public class TankEnemy extends Enemy {

    public TankEnemy(int panelWidth, double speed) {
        super(panelWidth, 68, 58, 5, speed * 0.7, 35, MovePattern.SIDE_SWEEP);
        this.x = RAND.nextInt(Math.max(1, panelWidth - width));
        this.y = -height;
    }

    @Override
    public void update() {
        moveSideSweep(panelWidth);
        if (hitFlash > 0) hitFlash--;
    }
}
