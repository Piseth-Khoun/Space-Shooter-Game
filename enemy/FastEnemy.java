package enemy;

public class FastEnemy extends Enemy {

    public FastEnemy(int panelWidth, double speed) {
        super(panelWidth, 40, 30, 1, speed + 1.5, 15, MovePattern.STRAIGHT);
        this.x = RAND.nextInt(Math.max(1, panelWidth - width));
        this.y = -height;
    }

    @Override
    public void update() {
        moveStraightDown();
        if (hitFlash > 0) hitFlash--;
    }
}
