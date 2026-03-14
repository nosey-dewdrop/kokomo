package model;

/*
 * ┌──────────────────────────────────────────────────────────────┐
 * │                  <<class>> ClubUser                          │
 * │                   extends User                               │
 * ├──────────────────────────────────────────────────────────────┤
 * │ (all fields inherited from User)                             │
 * ├──────────────────────────────────────────────────────────────┤
 * │ + ClubUser(username, displayName, email, password, salt, bio)│
 * │ + ClubUser(username, displayName, email, plainPassword, bio) │
 * │ + isClub(): boolean -> always true (POLYMORPHISM)            │
 * │ + getProfileBadge(): String -> "@user [CLUB] ✓" (OVERRIDE)  │
 * ├──────────────────────────────────────────────────────────────┤
 * │ INHERITS:   User (all fields + methods)                      │
 * │ IMPLEMENTS: Searchable (via User)                            │
 * │ USED BY:    RegisterScreen, SampleData, Database.buildUser   │
 * └──────────────────────────────────────────────────────────────┘
 */
public class ClubUser extends User {

    public ClubUser(String username, String displayName, String email,
                    String password, String salt, String bio) {
        super(username, displayName, email, password, salt, bio);
    }

    public ClubUser(String username, String displayName, String email,
                    String plainPassword, String bio) {
        super(username, displayName, email, plainPassword, bio);
    }

    @Override
    public boolean isClub() {
        return true;
    }

    @Override
    public String getProfileBadge() {
        String badge = "@" + getUsername() + " [CLUB]";
        if (isVerified()) badge += " \u2713";
        return badge;
    }

    @Override
    public String toString() {
        return "ClubUser{" + getUsername() + ", " + getDisplayName() + "}";
    }
}
