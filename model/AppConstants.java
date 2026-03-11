package model;

import java.awt.Color;
import java.awt.Font;

public class AppConstants {

    // COLORS - Notion Style
    public static final Color PRIMARY       = new Color(0x37, 0x35, 0x2F);
    public static final Color PRIMARY_LIGHT = new Color(0xF7, 0xF6, 0xF3);
    public static final Color ACCENT        = new Color(0x23, 0x83, 0xE2);
    public static final Color ACCENT_DARK   = new Color(0x1B, 0x6E, 0xC2);
    public static final Color SUCCESS       = new Color(0x0F, 0x7B, 0x6C);
    public static final Color DANGER        = new Color(0xEB, 0x57, 0x57);
    public static final Color WARNING       = new Color(0xCB, 0x91, 0x2F);
    public static final Color INTERESTED    = new Color(0x91, 0x65, 0xB8);
    public static final Color MAYBE_COLOR   = new Color(0xD9, 0x73, 0x0D);
    public static final Color BG_DARK       = Color.WHITE;
    public static final Color BG_CARD       = Color.WHITE;
    public static final Color BG_NAV        = new Color(0xFB, 0xFB, 0xFA);
    public static final Color BORDER        = new Color(0xE8, 0xE8, 0xE5);
    public static final Color TEXT_PRI      = new Color(0x37, 0x35, 0x2F);
    public static final Color TEXT_SEC      = new Color(0x78, 0x78, 0x74);
    public static final Color TEXT_LIGHT    = new Color(0xB0, 0xB0, 0xAC);

    // FONTS - Bigger and cleaner
    public static final Font F_HERO    = new Font("SansSerif", Font.BOLD, 36);
    public static final Font F_BIG     = new Font("SansSerif", Font.BOLD, 30);
    public static final Font F_TITLE   = new Font("SansSerif", Font.BOLD, 20);
    public static final Font F_SECTION = new Font("SansSerif", Font.BOLD, 15);
    public static final Font F_NORMAL  = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font F_SMALL   = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font F_TINY    = new Font("SansSerif", Font.PLAIN, 12);

    // UI - More spacious
    public static final int NAV_WIDTH = 230;
    public static final int CARD_RADIUS = 12;
    public static final int CARD_PADDING = 20;
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 820;
    public static final int LOGIN_WIDTH = 460;
    public static final int LOGIN_HEIGHT = 600;
    public static final int AVATAR_SIZE_SMALL = 32;
    public static final int AVATAR_SIZE_MEDIUM = 48;
    public static final int AVATAR_SIZE_LARGE = 72;
    public static final int FEED_COLUMNS = 2;
    public static final int DISCOVER_LIMIT = 5;
    public static final int FEED_DESC_PREVIEW = 100;
    public static final int PAGE_PAD_X = 56;
    public static final int PAGE_PAD_Y = 36;

    // TIER SYSTEM
    public static final String[] TIER_NAMES = {"Newcomer","Active","Experienced","Trusted","Legend"};
    public static final int[] TIER_THRESHOLDS = {0, 50, 150, 400, 1000};
    public static final Color[] TIER_COLORS = {TEXT_LIGHT, ACCENT, SUCCESS, INTERESTED, new Color(0xCB,0x91,0x2F)};

    public static int getTierIndex(int xp) {
        for (int i = TIER_THRESHOLDS.length - 1; i >= 0; i--)
            if (xp >= TIER_THRESHOLDS[i]) return i;
        return 0;
    }
    public static String getTierName(int xp)  { return TIER_NAMES[getTierIndex(xp)]; }
    public static Color  getTierColor(int xp) { return TIER_COLORS[getTierIndex(xp)]; }
    public static int getNextTierXP(int xp) {
        int i = getTierIndex(xp);
        return i < TIER_THRESHOLDS.length - 1 ? TIER_THRESHOLDS[i+1] : -1;
    }
    public static String getNextTierName(int xp) {
        int i = getTierIndex(xp);
        return i < TIER_NAMES.length - 1 ? TIER_NAMES[i+1] : "MAX";
    }

    // XP
    public static final int XP_CREATE_EVENT  = 10;
    public static final int XP_ATTEND_EVENT  = 5;
    public static final int XP_COMMENT       = 2;
    public static final int XP_GAIN_FOLLOWER = 3;
    public static final int XP_CANCEL_EVENT  = -15;
    public static final int XP_NO_SHOW       = -10;

    // EVENT DEFAULTS
    public static final int DEFAULT_CAPACITY      = 50;
    public static final int MIN_CAPACITY           = 5;
    public static final int MAX_CAPACITY           = 1000;
    public static final int CAPACITY_STEP          = 10;
    public static final int DEFAULT_DURATION_HRS   = 2;
    public static final int DEFAULT_DEADLINE_DAYS  = 1;
    public static final int DEFAULT_EVENT_XP       = 5;
    public static final int MIN_EVENT_XP           = 0;
    public static final int MAX_EVENT_XP           = 100;

    // DATABASE
    public static final String DB_URL  = "jdbc:mysql://localhost:3306/league_of_bilkent";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "";

    // EMAIL
    public static final String EMAIL_SENDER    = "noreply@bilkent.edu.tr";
    public static final String EMAIL_PASSWORD  = "";
    public static final String EMAIL_SMTP_HOST = "asmtp.bilkent.edu.tr";
    public static final int    EMAIL_SMTP_PORT = 587;

    // VALIDATION
    public static final int MIN_USERNAME_LENGTH    = 3;
    public static final int MIN_PASSWORD_LENGTH    = 4;
    public static final int MAX_DESCRIPTION_LEN    = 500;
    public static final int VERIFICATION_CODE_MIN  = 100000;
    public static final int VERIFICATION_CODE_MAX  = 999999;

    // INTEREST CATEGORIES
    public static final String[] INTEREST_CATEGORIES = {
        "Software","Algorithms","GameDev","AI","Data Science","Cybersecurity",
        "Robotics","Web Dev","Mobile Dev","Music","Art","Photography","Cinema",
        "Theater","Literature","Philosophy","History","Math","Physics",
        "Sports","Fitness","Football","Basketball","Swimming",
        "Entrepreneurship","Finance","Marketing","Volunteering","Environment","Food","Travel"
    };

    public static final String DEFAULT_POSTER = "default_poster.png";

    public static final double EVENT_SCORE_PENALTY_CANCEL = 0.0;
    public static final double EVENT_SCORE_SUCCESS        = 1.0;
    public static final double EVENT_SCORE_MIN_ATTENDEES  = 1;

    // ============ UI STRINGS ============
    public static final String APP_NAME = "League of Bilkent";

    // Page titles
    public static final String PAGE_FEED = "Events";
    public static final String PAGE_FEED_SUB = "See what\u2019s happening on campus.";
    public static final String PAGE_DISCOVER = "Discover";
    public static final String PAGE_DISCOVER_SUB = "Trending events and personalized recommendations.";
    public static final String PAGE_LEADERBOARD = "Leaderboard";
    public static final String PAGE_LEADER_SUB = "Top users ranked by XP.";
    public static final String PAGE_CREATE = "New Event";
    public static final String PAGE_CREATE_SUB = "Fill in the details below to create your event.";
    public static final String PAGE_NOTIF = "Notifications";
    public static final String PAGE_MESSAGES = "Messages";

    // Buttons
    public static final String BTN_BACK = "\u2190  Back";
    public static final String BTN_VIEW = "View";
    public static final String BTN_SEND = "Send";
    public static final String BTN_CREATE = "Create Event";
    public static final String BTN_DELETE = "Delete Event";
    public static final String BTN_CANCEL = "Cancel";
    public static final String BTN_GOING = "Going";
    public static final String BTN_INTERESTED = "Interested";
    public static final String BTN_MAYBE = "Maybe";
    public static final String BTN_FOLLOW = "Follow";
    public static final String BTN_UNFOLLOW = "Unfollow";
    public static final String BTN_REPLY = "Reply";
    public static final String BTN_MESSAGE = "Message";
    public static final String BTN_EDIT_BIO = "Edit Bio";
    public static final String BTN_EDIT_INT = "Edit Interests";
    public static final String BTN_CHOOSE_IMG = "Choose Image...";
    public static final String BTN_NEW_MSG = "+ New Message";
    public static final String BTN_LOGIN = "Log in";
    public static final String BTN_REGISTER = "Register";
    public static final String BTN_SEND_CODE = "Send Code";
    public static final String BTN_CHANGE_PASS = "Change Password";
    public static final String BTN_CONFIRM = "Confirm";

    // Attendance colors
    public static final Color COLOR_GOING = SUCCESS;
    public static final Color COLOR_INTERESTED = INTERESTED;
    public static final Color COLOR_MAYBE = MAYBE_COLOR;

    // Sections
    public static final String SEC_TRENDING = "Trending";
    public static final String SEC_FOR_YOU = "\u2728  For You";
    public static final String SEC_YOUR_STATUS = "Your Status";
    public static final String SEC_ATTENDEES = "Attendees";
    public static final String SEC_COMMENTS = "Comments";
    public static final String SEC_DATE_TIME = "Date & Time";
    public static final String SEC_SETTINGS = "Settings";
    public static final String SEC_FOLLOWERS = "Followers";
    public static final String SEC_FOLLOWING = "Following";
    public static final String SEC_INTERESTS = "Interests";
    public static final String SEC_EVENTS = "Events";
    public static final String SEC_USERS = "Users";

    // Field labels
    public static final String FIELD_EVENT_TITLE = "Event Title";
    public static final String FIELD_DESCRIPTION = "Description";
    public static final String FIELD_LOCATION = "Location";
    public static final String FIELD_POSTER = "Poster Image (optional)";
    public static final String FIELD_START = "Start";
    public static final String FIELD_END = "End";
    public static final String FIELD_DEADLINE = "Registration Deadline";
    public static final String FIELD_CAPACITY = "Capacity";
    public static final String FIELD_XP_REWARD = "XP Reward";
    public static final String FIELD_MIN_TIER = "Minimum Tier Required";
    public static final String FIELD_TAGS = "Tags (comma separated)";

    // Placeholders
    public static final String PH_SEARCH = "Search events, users...";
    public static final String PH_EVENT_TITLE = "Give your event a name...";
    public static final String PH_LOCATION = "Where is it happening?";
    public static final String PH_TAGS = "software, music, sports...";
    public static final String PH_AUTO_POSTER = "Auto-generated if empty";

    // Empty states
    public static final String EMPTY_EVENTS = "No events match your filter.";
    public static final String EMPTY_COMMENTS = "No comments yet. Be the first!";
    public static final String EMPTY_ATTENDEES = "No attendees yet";
    public static final String EMPTY_NOTIF = "No notifications yet.";
    public static final String EMPTY_TRENDING = "No trending events yet.";
    public static final String EMPTY_RECOMMEND = "No recommendations. Try adding more interests!";
    public static final String EMPTY_CHAT = "Select a conversation or start a new one.";
    public static final String EMPTY_FOLLOWERS = "No followers yet.";
    public static final String EMPTY_FOLLOWING = "Not following anyone yet.";

    // Errors
    public static final String ERR_TITLE_REQUIRED = "Event title is required!";
    public static final String ERR_LOC_REQUIRED = "Location is required!";
    public static final String ERR_INVALID_DATE = "Invalid date! Please check your input.";
    public static final String ERR_EVENT_FULL = "Event is full!";
    public static final String ERR_DEADLINE_PASSED = "Registration closed!";
    public static final String ERR_USER_NOT_FOUND = "User not found!";
    public static final String ERR_USER_PASS = "Please enter username and password!";
    public static final String ERR_WRONG_PASS = "Wrong password!";
    public static final String ERR_NOT_VERIFIED = "Account not verified. Please check your email.";
    public static final String ERR_PASS_MISMATCH = "Passwords do not match!";
    public static final String ERR_WRONG_VERIFY = "Wrong verification code!";
    public static final String ERR_NO_ACCOUNT = "No account found with this email!";
    public static final String ERR_BAD_BILKENT = "Please enter a valid Bilkent email!";

    // Success
    public static final String SUC_EVENT_CREATED = "Event created!";
    public static final String SUC_EVENT_DELETED = "Event deleted.";
    public static final String SUC_PASS_CHANGED = "Password changed successfully!";

    // Confirm
    public static final String CONFIRM_DELETE = "Are you sure you want to delete this event?";

    // Dialogs
    public static final String DLG_REPLY = "Reply";
    public static final String DLG_NEW_MSG = "New Message";
    public static final String DLG_NEW_MSG_Q = "Enter username to message:";
    public static final String DLG_INTERESTS = "Select Your Interests";
    public static final String DLG_INTERESTS_Q = "Choose topics you're interested in:";

    // Notifications
    public static final String NOTIF_NEW_EVENT = "%s created a new event: %s";
    public static final String NOTIF_COMMENT = "%s commented on: %s";
    public static final String NOTIF_REPLY = "%s replied to your comment on: %s";
    public static final String NOTIF_FOLLOW = "%s started following you!";

    // Login labels
    public static final String LOGIN_TITLE = "League of Bilkent";
    public static final String LOGIN_SUBTITLE = "Sign in to continue";
    public static final String LABEL_USERNAME = "Username";
    public static final String LABEL_PASSWORD = "Password";
    public static final String FORGOT_PASSWORD = "Forgot Password?";
    public static final String RESET_TITLE = "Reset Password";
    public static final String LABEL_BILKENT_EMAIL = "Bilkent Email Address";
    public static final String LABEL_VERIFY_CODE = "Verification Code";
    public static final String LABEL_NEW_PASS = "New Password";
    public static final String LABEL_CONFIRM = "Confirm Password";

    // Nav
    public static final String NAV_FEED = "\uD83D\uDCCB  Feed";
    public static final String NAV_DISCOVER = "\u2728  Discover";
    public static final String NAV_LEADERBOARD = "\uD83C\uDFC6  Leaderboard";
    public static final String NAV_NEW_EVENT = "\u2795  New Event";
    public static final String NAV_MESSAGES = "\uD83D\uDCAC  Messages";
    public static final String NAV_NOTIF = "\uD83D\uDD14  Notifications";
    public static final String NAV_LOGOUT = "\uD83D\uDEAA  Log out";
}
