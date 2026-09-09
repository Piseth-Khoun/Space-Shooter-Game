package enemy;

public class ZigzagEnemy extends Enemy {

    public ZigzagEnemy(int panelWidth, double speed) {
        super(panelWidth, 46, 38, 2, speed, 20, MovePattern.ZIGZAG);
        this.x = RAND.nextInt(Math.max(1, panelWidth - width));
        this.y = -height;
        this.horizontalDirection = RAND.nextBoolean() ? 1 : -1;
    }

    @Override
    public void update() {
        moveZigZag(800);
        if (hitFlash > 0) hitFlash--;
    }
}
