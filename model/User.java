package model;

import tools.*;

import java.util.ArrayList;

/*
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                     <<class>> User                             │
 * │                 implements Searchable                          │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ - username, displayName, email, password, salt, bio            │
 * │ - verified, xp                                                 │
 * │ - interests, following, followers, attending, notifications    │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ + User(username, displayName, email, password, salt, bio)      │
 * │ + User(username, displayName, email, plainPassword, bio)       │
 * │   -> auto-generates salt and hashes password                   │
 * │ + User(username, password) -> convenience constructor          │
 * │ + isClub(): boolean -> overridden by ClubUser (polymorphism)   │
 * │ + getProfileBadge(): String -> overridden by ClubUser          │
 * │ + checkPassword(plain): boolean -> hash comparison             │
 * │ + matchesSearch(query): boolean -> Searchable interface        │
 * │ + getSearchSummary(): String -> Searchable interface           │
 * │ + add/remove: Interest, Following, Follower, Attending, Notif  │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ INHERITANCE: ClubUser extends User                             │
 * │ IMPLEMENTS:  Searchable (matchesSearch, getSearchSummary)      │
 * │ USED BY:     Database, HomeScreen, ProfilePanel, SearchPanel,  │
 * │              LoginScreen, RegisterScreen, LeaderboardPanel     │
 * └─────────────────────────────────────────────────────────────────┘
 */
public class User implements Searchable {

    private String username;
    private String displayName;
    private String email;
    private String password;     // Hashed password
    private String salt;         // Password salt
    private String bio;
    private boolean verified;
    private ArrayList<String> interests;
    private ArrayList<String> following;
    private ArrayList<String> followers;
    private ArrayList<Integer> attending;
    private ArrayList<String> notifications;
    private int xp;

    // ==========================================
    // CONSTRUCTORS
    // ==========================================

    public User(String username, String displayName, String email,
                String password, String salt, String bio) {
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.bio = bio;
        this.verified = false;
        this.interests = new ArrayList<>();
        this.following = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.attending = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.xp = 0;
    }

    /**
     * Convenience constructor: salt is auto-generated, password is hashed.
     */
    public User(String username, String displayName, String email,
                String plainPassword, String bio) {
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.salt = PasswordUtil.generateSalt();
        this.password = PasswordUtil.hashPassword(plainPassword, this.salt);
        this.bio = bio;
        this.verified = false;
        this.interests = new ArrayList<>();
        this.following = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.attending = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.xp = 0;
    }

    public User(String username, String password) {
        this(username, username, "", password, "");
    }

    // ==========================================
    // GETTERS
    // ==========================================

    public String getUsername()     { return username; }
    public String getDisplayName()  { return displayName; }
    public String getEmail()       { return email; }
    public String getPassword()    { return password; }
    public String getSalt()        { return salt; }
    public String getBio()         { return bio; }
    public boolean isVerified()    { return verified; }

    public ArrayList<String> getInterests()      { return interests; }
    public ArrayList<String> getFollowing()       { return following; }
    public ArrayList<String> getFollowers()       { return followers; }
    public ArrayList<Integer> getAttending()      { return attending; }
    public ArrayList<String> getNotifications()   { return notifications; }
    public int getXp()                             { return xp; }

    /**
     * Is this a club account? Returns false for base User.
     * ClubUser overrides this (polymorphism).
     */
    public boolean isClub() {
        return false;
    }

    /**
     * Profile badge text. ClubUser overrides this (polymorphism).
     */
    public String getProfileBadge() {
        String badge = "@" + username;
        if (verified) badge += " \u2713";
        return badge;
    }

    // ==========================================
    // SETTERS
    // ==========================================

    public void setUsername(String username)        { this.username = username; }
    public void setDisplayName(String displayName)  { this.displayName = displayName; }
    public void setEmail(String email)              { this.email = email; }
    public void setPassword(String password)        { this.password = password; }
    public void setSalt(String salt)                { this.salt = salt; }
    public void setBio(String bio)                  { this.bio = bio; }
    public void setVerified(boolean verified)        { this.verified = verified; }

    public void setInterests(ArrayList<String> interests)        { this.interests = interests; }
    public void setFollowing(ArrayList<String> following)         { this.following = following; }
    public void setFollowers(ArrayList<String> followers)         { this.followers = followers; }
    public void setAttending(ArrayList<Integer> attending)        { this.attending = attending; }
    public void setNotifications(ArrayList<String> notifications) { this.notifications = notifications; }
    public void setXp(int xp)                                    { this.xp = xp; }

    // ==========================================
    // BUSINESS LOGIC
    // ==========================================

    public void addInterest(String interest) {
        if (!interests.contains(interest)) interests.add(interest);
    }

    public void removeInterest(String interest) {
        interests.remove(interest);
    }

    public void addFollowing(String username) {
        if (!following.contains(username)) following.add(username);
    }

    public void removeFollowing(String username) {
        following.remove(username);
    }

    public void addFollower(String username) {
        if (!followers.contains(username)) followers.add(username);
    }

    public void removeFollower(String username) {
        followers.remove(username);
    }

    public void addAttending(int eventId) {
        if (!attending.contains(eventId)) attending.add(eventId);
    }

    public void removeAttending(int eventId) {
        attending.remove(Integer.valueOf(eventId));
    }

    public void addNotification(String message) {
        notifications.add(message);
    }

    public boolean isFollowing(String username) {
        return following.contains(username);
    }

    /**
     * Password verification (hash comparison).
     */
    public boolean checkPassword(String plainPassword) {
        return PasswordUtil.verifyPassword(plainPassword, this.password, this.salt);
    }

    // ==========================================
    // SEARCHABLE INTERFACE (polymorphism)
    // ==========================================

    @Override
    public boolean matchesSearch(String query) {
        return username.contains(query) ||
               displayName.toLowerCase().contains(query) ||
               interests.stream().anyMatch(i -> i.toLowerCase().contains(query));
    }

    @Override
    public String getSearchSummary() {
        return getProfileBadge() + " - " + bio;
    }

    @Override
    public String toString() {
        return "User{" + username + ", " + displayName + ", club=" + isClub() + "}";
    }
}
