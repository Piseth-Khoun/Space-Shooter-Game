package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class HighScoreManager {

    private static final File HIGH_SCORE_FILE = new File("highscores.txt");

    public static List<Integer> loadScores() {
        List<Integer> scores = new ArrayList<>();
        if (!HIGH_SCORE_FILE.exists()) {
            return scores;
        }

        try (Scanner scanner = new Scanner(HIGH_SCORE_FILE)) {
            while (scanner.hasNextInt()) {
                scores.add(scanner.nextInt());
            }
        } catch (IOException ignored) {
            // Ignore missing file/IO issues and keep the in-memory fallback.
        }

        return scores;
    }

    public static void saveScore(int score) {
        List<Integer> scores = loadScores();
        scores.add(score);
        Collections.sort(scores, Collections.reverseOrder());

        while (scores.size() > 5) {
            scores.remove(scores.size() - 1);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            for (int value : scores) {
                writer.write(Integer.toString(value));
                writer.newLine();
            }
        } catch (IOException ignored) {
            // Ignore file save failures for now.
        }
    }
}
