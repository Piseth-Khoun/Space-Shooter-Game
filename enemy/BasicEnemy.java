package enemy;

public class BasicEnemy extends Enemy {

    public BasicEnemy(int panelWidth, double speed) {
        super(panelWidth, 50, 40, 1, speed, 10, MovePattern.STRAIGHT);
        this.x = RAND.nextInt(Math.max(1, panelWidth - width));
        this.y = -height;
    }

    @Override
    public void update() {
        moveStraightDown();
        if (hitFlash > 0) hitFlash--;
    }
}
