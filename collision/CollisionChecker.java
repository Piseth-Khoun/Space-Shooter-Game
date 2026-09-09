package collision;

import java.awt.Rectangle;

/**
 * Utility class for detecting collisions between game objects.
 */
public class CollisionChecker {

    private CollisionChecker() {
        // static utility class, no instances
    }

    public static boolean checkCollision(Rectangle a, Rectangle b) {
        return a.intersects(b);
    }
}
