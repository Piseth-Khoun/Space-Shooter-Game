package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Loads sprite images robustly, regardless of what the "current working
 * directory" happens to be when the game is launched (this differs between
 * running via `java Main` from the SpaceShooter folder, running from an IDE
 * like IntelliJ, or running from a packaged jar).
 *
 * It tries, in order:
 *  1) images/&lt;filename&gt;              (working dir = SpaceShooter/)
 *  2) SpaceShooter/images/&lt;filename&gt;  (working dir = project root, one level up)
 *  3) classpath resource /images/&lt;filename&gt; (IDE copied it into the output/build folder)
 *
 * If all three fail, it prints a helpful diagnostic to the console and
 * returns null so callers can fall back to a drawn placeholder shape.
 */
public class ImageLoader {

    private ImageLoader() {
        // static utility class, no instances
    }

    public static BufferedImage load(String filename) {
        BufferedImage img = tryFile("images/" + filename);
        if (img != null) return img;

        img = tryFile("SpaceShooter/images/" + filename);
        if (img != null) return img;

        img = tryResource("/images/" + filename);
        if (img != null) return img;

        System.err.println("[ImageLoader] Could not find '" + filename
                + "' at images/, SpaceShooter/images/, or as a classpath resource. "
                + "Current working directory is: " + new File("").getAbsolutePath()
                + " -- falling back to a drawn placeholder shape.");
        return null;
    }

    private static BufferedImage tryFile(String path) {
        File f = new File(path);
        if (!f.exists()) return null;
        try {
            return ImageIO.read(f);
        } catch (IOException e) {
            return null;
        }
    }

    private static BufferedImage tryResource(String path) {
        try (InputStream is = ImageLoader.class.getResourceAsStream(path)) {
            if (is == null) return null;
            return ImageIO.read(is);
        } catch (IOException e) {
            return null;
        }
    }
}
