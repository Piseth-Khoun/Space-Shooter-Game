package weapon;

public enum WeaponMode {
    NORMAL(1, "Normal"),
    DOUBLE(2, "Double Shot"),
    TRIPLE(3, "Triple Shot"),
    LASER(4, "Laser");

    private final int level;
    private final String label;

    WeaponMode(int level, String label) {
        this.level = level;
        this.label = label;
    }

    public int getLevel() {
        return level;
    }

    public String getLabel() {
        return label;
    }

    public WeaponMode nextMode() {
        switch (this) {
            case NORMAL:
                return DOUBLE;
            case DOUBLE:
                return TRIPLE;
            case TRIPLE:
                return LASER;
            case LASER:
            default:
                return NORMAL;
        }
    }
}
