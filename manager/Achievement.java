package manager;

/**
 * A single unlockable milestone. Instances are created and unlocked only
 * by AchievementManager; everyone else just reads them.
 */
public class Achievement {

    private final String id;
    private final String title;
    private final String description;
    private boolean unlocked;

    public Achievement(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isUnlocked() { return unlocked; }

    void unlock() {
        unlocked = true;
    }
}
