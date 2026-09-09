package util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A grid-based sprite sheet: knows its frame size and draws any frame
 * index at an arbitrary destination size. Frames are read in row-major
 * order (left to right, top to bottom); any fully-transparent trailing
 * cells (common padding at the end of a generated sheet) are excluded
 * from the frame count automatically.
 */
public class SpriteSheet {

    private final BufferedImage sheet;
    private final int frameSize;
    private final int columns;
    private final int frameCount;

    public SpriteSheet(BufferedImage sheet, int frameSize) {
        this.sheet = sheet;
        this.frameSize = frameSize;
        this.columns = Math.max(1, sheet.getWidth() / frameSize);
        int rows = Math.max(1, sheet.getHeight() / frameSize);
        this.frameCount = countRealFrames(sheet, frameSize, columns, columns * rows);
    }

    private static int countRealFrames(BufferedImage sheet, int frameSize, int columns, int totalCells) {
        int count = totalCells;
        for (int i = totalCells - 1; i >= 0; i--) {
            int col = i % columns;
            int row = i / columns;
            if (!isCellEmpty(sheet, col * frameSize, row * frameSize, frameSize)) {
                break;
            }
            count--;
        }
        return Math.max(1, count);
    }

    private static boolean isCellEmpty(BufferedImage sheet, int x, int y, int size) {
        for (int dy = 0; dy < size; dy += 4) {
            for (int dx = 0; dx < size; dx += 4) {
                if ((sheet.getRGB(x + dx, y + dy) >>> 24) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getFrameCount() {
        return frameCount;
    }

    /** Draws the given frame index (clamped to valid range) into a destSize x destSize square centered on (destX, destY) being its top-left corner. */
    public void draw(Graphics2D g, int frameIndex, int destX, int destY, int destSize) {
        int idx = Math.max(0, Math.min(frameCount - 1, frameIndex));
        int col = idx % columns;
        int row = idx / columns;
        int sx = col * frameSize;
        int sy = row * frameSize;
        g.drawImage(sheet, destX, destY, destX + destSize, destY + destSize,
                sx, sy, sx + frameSize, sy + frameSize, null);
    }
}
