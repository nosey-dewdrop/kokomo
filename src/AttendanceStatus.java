/**
 * Etkinlik katilim durumu - Going/Interested/Maybe.
 * Plandaki 2.3 Attendance System.
 */
public enum AttendanceStatus {
    GOING("Going"),
    INTERESTED("Interested"),
    MAYBE("Maybe");

    private final String displayName;

    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AttendanceStatus fromString(String s) {
        if (s == null) return null;
        switch (s.toUpperCase()) {
            case "GOING":      return GOING;
            case "INTERESTED": return INTERESTED;
            case "MAYBE":      return MAYBE;
            default:           return null;
        }
    }
}
