package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Tracks a persistent lifetime coin total (1 point earned = 1 coin,
 * converted at the end of each game). Unlike the high-score list this is
 * a single running total, not a top-N list.
 */
public class CoinManager {

    private static final File COIN_FILE = new File("coins.txt");

    private CoinManager() {
        // static utility class, no instances
    }

    public static int loadTotalCoins() {
        if (!COIN_FILE.exists()) {
            return 0;
        }

        try (Scanner scanner = new Scanner(COIN_FILE)) {
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            }
        } catch (IOException ignored) {
            // Ignore missing file/IO issues and keep the in-memory fallback.
        }

        return 0;
    }

    /** Adds "earned" coins to the persisted total and returns the new total. */
    public static int addCoins(int earned) {
        int total = loadTotalCoins() + Math.max(0, earned);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COIN_FILE))) {
            writer.write(Integer.toString(total));
        } catch (IOException ignored) {
            // Ignore file save failures for now.
        }

        return total;
    }
}
