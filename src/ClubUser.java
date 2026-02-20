/**
 * Kulup hesabi - User sinifini extend eder (INHERITANCE).
 * isClub() ve getProfileBadge() override edilir (POLYMORPHISM).
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
