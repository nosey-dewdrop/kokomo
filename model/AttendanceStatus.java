package model;

/*
 * ┌──────────────────────────────────────────────────────────────┐
 * │                 <<enum>> AttendanceStatus                    │
 * ├──────────────────────────────────────────────────────────────┤
 * │ GOING("Going"), INTERESTED("Interested"), MAYBE("Maybe")    │
 * ├──────────────────────────────────────────────────────────────┤
 * │ + getDisplayName(): String                                   │
 * │ + fromString(s): AttendanceStatus (static) -> parses from DB │
 * ├──────────────────────────────────────────────────────────────┤
 * │ USED BY: Event (attendanceMap), Database,                    │
 * │          EventDetailPanel, HomeScreen.changeAttendance,       │
 * │          FeedPanel, SampleData                                │
 * └──────────────────────────────────────────────────────────────┘
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
