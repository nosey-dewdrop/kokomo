import java.awt.Color;

/**
 * XP/Tier utility - delegates to AppConstants for configuration.
 */
public class ExperienceSystem {

    public static String getTierName(int xp) { return AppConstants.getTierName(xp); }
    public static Color getTierColor(int xp) { return AppConstants.getTierColor(xp); }
    public static int getTierIndex(int xp)   { return AppConstants.getTierIndex(xp); }

    public static double getOrganizerScore(String username) {
        java.util.ArrayList<Event> events = Database.getAllEvents();
        int total = 0, successful = 0;
        for (Event ev : events) {
            if (ev.getCreatorUsername().equals(username)) {
                total++;
                if (ev.getGoingCount() >= AppConstants.EVENT_SCORE_MIN_ATTENDEES)
                    successful++;
            }
        }
        if (total == 0) return 0;
        return (double) successful / total * 100;
    }
}
