package manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Tracks progress toward a fixed set of achievements and hands back any
 * newly-unlocked ones so the caller can show a popup / play a sound.
 *
 * Kill count persists across restarts (so "Centurion" can be earned over
 * several runs); per-run stats like "no damage taken" reset on resetRun().
 */
public class AchievementManager {

    private final LinkedHashMap<String, Achievement> achievements = new LinkedHashMap<>();
    private final List<Achievement> pendingPopups = new ArrayList<>();

    private int totalKills = 0;
    private boolean damagedThisRun = false;

    public AchievementManager() {
        add("FIRST_BLOOD", "First Blood", "Destroy your first enemy");
        add("CENTURION", "Centurion", "Destroy 100 enemies (across all runs)");
        add("UNTOUCHABLE", "Untouchable", "Reach Level 3 without taking damage");
        add("SURVIVOR", "Survivor", "Reach Level 5 in a single run");
        add("BOSS_SLAYER", "Boss Slayer", "Defeat a boss");
    }

    private void add(String id, String title, String description) {
        achievements.put(id, new Achievement(id, title, description));
    }

    public void recordKill() {
        totalKills++;
        unlock("FIRST_BLOOD");
        if (totalKills >= 100) {
            unlock("CENTURION");
        }
    }

    public void recordDamageTaken() {
        damagedThisRun = true;
    }

    public void recordBossKill() {
        unlock("BOSS_SLAYER");
    }

    public void recordLevelReached(int level) {
        if (level >= 3 && !damagedThisRun) {
            unlock("UNTOUCHABLE");
        }
        if (level >= 5) {
            unlock("SURVIVOR");
        }
    }

    /** Call at the start of each new run; kill count deliberately is not reset here. */
    public void resetRun() {
        damagedThisRun = false;
    }

    private void unlock(String id) {
        Achievement a = achievements.get(id);
        if (a != null && !a.isUnlocked()) {
            a.unlock();
            pendingPopups.add(a);
        }
    }

    /** Returns (and clears) any achievements unlocked since the last call. */
    public List<Achievement> drainPopups() {
        if (pendingPopups.isEmpty()) {
            return Collections.emptyList();
        }
        List<Achievement> copy = new ArrayList<>(pendingPopups);
        pendingPopups.clear();
        return copy;
    }

    public Collection<Achievement> getAll() {
        return achievements.values();
    }

    public int getUnlockedCount() {
        int count = 0;
        for (Achievement a : achievements.values()) {
            if (a.isUnlocked()) count++;
        }
        return count;
    }

    public int getTotalCount() {
        return achievements.size();
    }
}
