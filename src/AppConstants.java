import java.awt.Color;
import java.awt.Font;

public class AppConstants {

    // COLORS - Notion Style
    public static final Color PRIMARY      = new Color(0x37, 0x35, 0x2F);
    public static final Color PRIMARY_LIGHT= new Color(0xF0, 0xF0, 0xF0);
    public static final Color ACCENT       = new Color(0x23, 0x83, 0xE2);
    public static final Color ACCENT_DARK  = new Color(0x1B, 0x6E, 0xC2);
    public static final Color SUCCESS      = new Color(0x0F, 0x7B, 0x6C);
    public static final Color DANGER       = new Color(0xEB, 0x57, 0x57);
    public static final Color WARNING      = new Color(0xCB, 0x91, 0x2F);
    public static final Color INTERESTED   = new Color(0x91, 0x65, 0xB8);
    public static final Color MAYBE_COLOR  = new Color(0xD9, 0x73, 0x0D);
    public static final Color BG_DARK      = Color.WHITE;
    public static final Color BG_CARD      = Color.WHITE;
    public static final Color BG_NAV       = new Color(0xFB, 0xFB, 0xFA);
    public static final Color BORDER       = new Color(0xE9, 0xE9, 0xE7);
    public static final Color TEXT_PRI     = new Color(0x37, 0x35, 0x2F);
    public static final Color TEXT_SEC     = new Color(0x91, 0x91, 0x8E);
    public static final Color TEXT_LIGHT   = new Color(0xC4, 0xC4, 0xC0);

    // FONTS
    public static final Font F_BIG     = new Font("SansSerif", Font.BOLD, 24);
    public static final Font F_TITLE   = new Font("SansSerif", Font.BOLD, 16);
    public static final Font F_SECTION = new Font("SansSerif", Font.BOLD, 13);
    public static final Font F_NORMAL  = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font F_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font F_TINY    = new Font("SansSerif", Font.PLAIN, 10);

    // UI
    public static final int NAV_WIDTH = 220;
    public static final int CARD_RADIUS = 4;
    public static final int CARD_PADDING = 16;
    public static final int WINDOW_WIDTH = 1100;
    public static final int WINDOW_HEIGHT = 750;
    public static final int LOGIN_WIDTH = 460;
    public static final int LOGIN_HEIGHT = 600;
    public static final int AVATAR_SIZE_SMALL = 28;
    public static final int AVATAR_SIZE_MEDIUM = 48;
    public static final int AVATAR_SIZE_LARGE = 72;
    public static final int FEED_COLUMNS = 3;
    public static final int DISCOVER_LIMIT = 5;
    public static final int FEED_DESC_PREVIEW = 100;

    // TIER SYSTEM - fully configurable arrays
    public static final String[] TIER_NAMES = {"Newcomer","Active","Experienced","Trusted","Legend"};
    public static final int[] TIER_THRESHOLDS = {0, 50, 150, 400, 1000};
    public static final Color[] TIER_COLORS = {TEXT_LIGHT, ACCENT, SUCCESS, INTERESTED, new Color(0xCB,0x91,0x2F)};

    public static int getTierIndex(int xp) {
        for (int i = TIER_THRESHOLDS.length - 1; i >= 0; i--)
            if (xp >= TIER_THRESHOLDS[i]) return i;
        return 0;
    }
    public static String getTierName(int xp) { return TIER_NAMES[getTierIndex(xp)]; }
    public static Color getTierColor(int xp) { return TIER_COLORS[getTierIndex(xp)]; }
    public static int getNextTierXP(int xp) {
        int i = getTierIndex(xp);
        return i < TIER_THRESHOLDS.length - 1 ? TIER_THRESHOLDS[i+1] : -1;
    }
    public static String getNextTierName(int xp) {
        int i = getTierIndex(xp);
        return i < TIER_NAMES.length - 1 ? TIER_NAMES[i+1] : "MAX";
    }

    // XP
    public static final int XP_CREATE_EVENT = 10;
    public static final int XP_ATTEND_EVENT = 5;
    public static final int XP_COMMENT = 2;
    public static final int XP_GAIN_FOLLOWER = 3;
    public static final int XP_CANCEL_EVENT = -15;
    public static final int XP_NO_SHOW = -10;

    // EVENT DEFAULTS
    public static final int DEFAULT_CAPACITY = 50;
    public static final int MIN_CAPACITY = 5;
    public static final int MAX_CAPACITY = 1000;
    public static final int CAPACITY_STEP = 10;
    public static final int DEFAULT_DURATION_HRS = 2;
    public static final int DEFAULT_DEADLINE_DAYS = 1;
    public static final int DEFAULT_EVENT_XP = 5;
    public static final int MIN_EVENT_XP = 0;
    public static final int MAX_EVENT_XP = 100;

    // DATABASE
    public static final String DB_URL  = "jdbc:mysql://localhost:3306/league_of_bilkent";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "";

    // EMAIL
    public static final String EMAIL_SENDER   = "noreply@bilkent.edu.tr";
    public static final String EMAIL_PASSWORD  = "";
    public static final String EMAIL_SMTP_HOST = "asmtp.bilkent.edu.tr";
    public static final int EMAIL_SMTP_PORT    = 587;

    // VALIDATION
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MIN_PASSWORD_LENGTH = 4;
    public static final int MAX_DESCRIPTION_LEN = 500;
    public static final int VERIFICATION_CODE_MIN = 100000;
    public static final int VERIFICATION_CODE_MAX = 999999;

    // INTEREST CATEGORIES
    public static final String[] INTEREST_CATEGORIES = {
        "Software","Algorithms","GameDev","AI","Data Science","Cybersecurity",
        "Robotics","Web Dev","Mobile Dev","Music","Art","Photography","Cinema",
        "Theater","Literature","Philosophy","History","Math","Physics",
        "Sports","Fitness","Football","Basketball","Swimming",
        "Entrepreneurship","Finance","Marketing","Volunteering","Environment","Food","Travel"
    };

    public static final String DEFAULT_POSTER = "default_poster.png";
    
    // EVENT SCORE
    public static final double EVENT_SCORE_PENALTY_CANCEL = 0.0;
    public static final double EVENT_SCORE_SUCCESS = 1.0;
    public static final double EVENT_SCORE_MIN_ATTENDEES = 1;
}
