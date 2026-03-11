package model;

import tools.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {

    public static Connection databaseConnection;

    public static void createConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = AppConstants.DB_URL;
            String dbUser = AppConstants.DB_USER;
            String dbPass = AppConstants.DB_PASS;
            try {
                java.util.Properties creds = new java.util.Properties();
                creds.load(new java.io.FileInputStream("credentials.properties"));
                dbUrl = creds.getProperty("db.url", dbUrl);
                dbUser = creds.getProperty("db.user", dbUser);
                dbPass = creds.getProperty("db.password", dbPass);
            } catch (Exception ignored) {}
            databaseConnection = DriverManager.getConnection(
                dbUrl, dbUser, dbPass);
            createTables();
            System.out.println("MySQL connection complete");
        } catch (SQLException e) {
            System.out.println("MySQL connection error!"); e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found!"); e.printStackTrace();
        }
    }

    private static void createTables() {
        try (Statement stmt = databaseConnection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                "username VARCHAR(50) PRIMARY KEY, display_name VARCHAR(100), " +
                "email VARCHAR(100), password VARCHAR(255), salt VARCHAR(64), " +
                "bio TEXT, is_club TINYINT DEFAULT 0, verified TINYINT DEFAULT 0, xp INT DEFAULT 0)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS user_interests (" +
                "username VARCHAR(50), interest VARCHAR(50), PRIMARY KEY (username, interest))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS events (" +
                "event_id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(200), " +
                "description TEXT, location VARCHAR(200), date_time VARCHAR(50), " +
                "end_date_time VARCHAR(50), registration_deadline VARCHAR(50), " +
                "capacity INT, creator_username VARCHAR(50), " +
                "image_path VARCHAR(500) DEFAULT '', xp_reward INT DEFAULT 5, min_tier INT DEFAULT 0)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS event_tags (" +
                "event_id INT, tag VARCHAR(50), PRIMARY KEY (event_id, tag))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS attendance (" +
                "event_id INT, username VARCHAR(50), status VARCHAR(20) DEFAULT 'GOING', " +
                "PRIMARY KEY (event_id, username))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments (" +
                "comment_id INT AUTO_INCREMENT PRIMARY KEY, event_id INT, " +
                "username VARCHAR(50), text TEXT, time VARCHAR(20), parent_comment_id INT DEFAULT 0)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS follows (" +
                "follower_username VARCHAR(50), following_username VARCHAR(50), " +
                "PRIMARY KEY (follower_username, following_username))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS notifications (" +
                "notif_id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50), message TEXT)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS messages (" +
                "msg_id INT AUTO_INCREMENT PRIMARY KEY, sender VARCHAR(50), receiver VARCHAR(50), " +
                "text TEXT, time VARCHAR(30), is_read TINYINT DEFAULT 0)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS user_tag_filters (" +
                "username VARCHAR(50), tag VARCHAR(50), PRIMARY KEY (username, tag))");

            // Migrations
            migrateAttendeesTable(stmt);
            addColumnIfNotExists(stmt, "users", "salt", "VARCHAR(64) DEFAULT ''");
            addColumnIfNotExists(stmt, "users", "xp", "INT DEFAULT 0");
            addColumnIfNotExists(stmt, "events", "end_date_time", "VARCHAR(50)");
            addColumnIfNotExists(stmt, "events", "registration_deadline", "VARCHAR(50)");
            addColumnIfNotExists(stmt, "events", "image_path", "VARCHAR(500) DEFAULT ''");
            addColumnIfNotExists(stmt, "events", "xp_reward", "INT DEFAULT 5");
            addColumnIfNotExists(stmt, "events", "min_tier", "INT DEFAULT 0");
            addColumnIfNotExists(stmt, "comments", "parent_comment_id", "INT DEFAULT 0");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void migrateAttendeesTable(Statement stmt) {
        try {
            ResultSet rs = databaseConnection.getMetaData().getTables(null, null, "attendees", null);
            if (rs.next()) {
                stmt.executeUpdate("INSERT IGNORE INTO attendance (event_id, username, status) " +
                    "SELECT event_id, username, 'GOING' FROM attendees");
                System.out.println("attendees -> attendance migration done");
            }
        } catch (SQLException ignored) {}
    }

    private static void addColumnIfNotExists(Statement stmt, String table, String col, String def) {
        try {
            ResultSet rs = databaseConnection.getMetaData().getColumns(null, null, table, col);
            if (!rs.next()) {
                stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + col + " " + def);
                System.out.println("Added column " + col + " to " + table);
            }
        } catch (SQLException ignored) {}
    }

    // ==================== ADD ====================

    public static void addToDatabase(User user) {
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "INSERT IGNORE INTO users (username,display_name,email,password,salt,bio,is_club,verified) VALUES(?,?,?,?,?,?,?,?)");
            ps.setString(1, user.getUsername()); ps.setString(2, user.getDisplayName());
            ps.setString(3, user.getEmail()); ps.setString(4, user.getPassword());
            ps.setString(5, user.getSalt()); ps.setString(6, user.getBio());
            ps.setInt(7, user.isClub() ? 1 : 0); ps.setInt(8, user.isVerified() ? 1 : 0);
            ps.executeUpdate();
            for (String interest : user.getInterests()) addInterest(user.getUsername(), interest);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static int addToDatabase(Event event) {
        int generatedId = -1;
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "INSERT INTO events (title,description,location,date_time,end_date_time," +
                "registration_deadline,capacity,creator_username,image_path,xp_reward,min_tier) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, event.getTitle()); ps.setString(2, event.getDescription());
            ps.setString(3, event.getLocation()); ps.setString(4, event.getDateTime().toString());
            ps.setString(5, event.getEndDateTime() != null ? event.getEndDateTime().toString() : null);
            ps.setString(6, event.getRegistrationDeadline() != null ? event.getRegistrationDeadline().toString() : null);
            ps.setInt(7, event.getCapacity()); ps.setString(8, event.getCreatorUsername());
            ps.setString(9, event.getImagePath()); ps.setInt(10, event.getXpReward());
            ps.setInt(11, event.getMinTierIndex());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) generatedId = rs.getInt(1);
            for (String tag : event.getTags()) addEventTag(generatedId, tag);
        } catch (SQLException e) { e.printStackTrace(); }
        return generatedId;
    }

    public static int addToDatabase(Comment comment, int eventId) {
        int generatedId = -1;
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "INSERT INTO comments (event_id,username,text,time,parent_comment_id) VALUES(?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, eventId); ps.setString(2, comment.getUsername());
            ps.setString(3, comment.getText()); ps.setString(4, comment.getTime());
            ps.setInt(5, comment.getParentId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) generatedId = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return generatedId;
    }

    // ==================== DELETE ====================

    public static void deleteFromDatabase(User user) {
        try {
            exec("DELETE FROM user_interests WHERE username=?", user.getUsername());
            exec("DELETE FROM follows WHERE follower_username=? OR following_username=?",
                 user.getUsername(), user.getUsername());
            exec("DELETE FROM notifications WHERE username=?", user.getUsername());
            exec("DELETE FROM attendance WHERE username=?", user.getUsername());
            exec("DELETE FROM users WHERE username=?", user.getUsername());
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void deleteFromDatabase(Event event) {
        try {
            exec("DELETE FROM comments WHERE event_id=?", event.getId());
            exec("DELETE FROM attendance WHERE event_id=?", event.getId());
            exec("DELETE FROM event_tags WHERE event_id=?", event.getId());
            exec("DELETE FROM events WHERE event_id=?", event.getId());
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void exec(String sql, Object... params) throws SQLException {
        PreparedStatement ps = databaseConnection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof Integer) ps.setInt(i+1, (Integer)params[i]);
            else ps.setString(i+1, params[i].toString());
        }
        ps.executeUpdate();
    }

    // ==================== ATTENDANCE ====================

    public static void setAttendance(int eventId, String username, AttendanceStatus status) {
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "INSERT INTO attendance (event_id,username,status) VALUES(?,?,?) ON DUPLICATE KEY UPDATE status=?");
            ps.setInt(1, eventId); ps.setString(2, username);
            ps.setString(3, status.name()); ps.setString(4, status.name());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void removeAttendance(int eventId, String username) {
        try { exec("DELETE FROM attendance WHERE event_id=? AND username=?", eventId, username); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    public static HashMap<String, AttendanceStatus> getAttendanceMap(int eventId) {
        HashMap<String, AttendanceStatus> map = new HashMap<>();
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "SELECT username,status FROM attendance WHERE event_id=?");
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AttendanceStatus s = AttendanceStatus.fromString(rs.getString("status"));
                if (s != null) map.put(rs.getString("username"), s);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    public static void addAttendee(int eid, String u) { setAttendance(eid, u, AttendanceStatus.GOING); }
    public static void deleteAttendee(int eid, String u) { removeAttendance(eid, u); }
    public static ArrayList<String> getAttendees(int eid) { return new ArrayList<>(getAttendanceMap(eid).keySet()); }

    // ==================== FOLLOWS ====================

    public static void addFollow(String follower, String following) {
        try {
            PreparedStatement ps = databaseConnection.prepareStatement("INSERT IGNORE INTO follows VALUES(?,?)");
            ps.setString(1, follower); ps.setString(2, following); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void deleteFollow(String follower, String following) {
        try { exec("DELETE FROM follows WHERE follower_username=? AND following_username=?", follower, following); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    public static ArrayList<String> getFollowers(String u) {
        return qList("SELECT follower_username FROM follows WHERE following_username=?", "follower_username", u);
    }
    public static ArrayList<String> getFollowing(String u) {
        return qList("SELECT following_username FROM follows WHERE follower_username=?", "following_username", u);
    }

    // ==================== TAGS / INTERESTS / NOTIFS ====================

    public static void addEventTag(int eid, String tag) {
        try { PreparedStatement ps = databaseConnection.prepareStatement("INSERT IGNORE INTO event_tags VALUES(?,?)");
            ps.setInt(1, eid); ps.setString(2, tag); ps.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); }
    }
    public static ArrayList<String> getEventTags(int eid) { return qList("SELECT tag FROM event_tags WHERE event_id=?", "tag", eid); }

    public static void addInterest(String u, String i) {
        try { PreparedStatement ps = databaseConnection.prepareStatement("INSERT IGNORE INTO user_interests VALUES(?,?)");
            ps.setString(1, u); ps.setString(2, i); ps.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); }
    }
    public static void removeInterest(String u, String i) {
        try { exec("DELETE FROM user_interests WHERE username=? AND interest=?", u, i); } catch (SQLException e) { e.printStackTrace(); }
    }
    public static ArrayList<String> getInterests(String u) { return qList("SELECT interest FROM user_interests WHERE username=?", "interest", u); }
    public static void setInterests(String u, ArrayList<String> list) {
        try { exec("DELETE FROM user_interests WHERE username=?", u);
            for (String i : list) addInterest(u, i); } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void addNotification(String u, String msg) {
        try { PreparedStatement ps = databaseConnection.prepareStatement("INSERT INTO notifications (username,message) VALUES(?,?)");
            ps.setString(1, u); ps.setString(2, msg); ps.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); }
    }
    public static ArrayList<String> getNotifications(String u) {
        return qList("SELECT message FROM notifications WHERE username=? ORDER BY notif_id ASC", "message", u);
    }

    // ==================== MESSAGES (DM) ====================

    public static void sendMessage(String sender, String receiver, String text) {
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "INSERT INTO messages (sender,receiver,text,time) VALUES(?,?,?,?)");
            ps.setString(1, sender); ps.setString(2, receiver); ps.setString(3, text);
            ps.setString(4, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static ArrayList<String[]> getMessages(String user1, String user2) {
        ArrayList<String[]> list = new ArrayList<>();
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "SELECT sender,text,time FROM messages WHERE (sender=? AND receiver=?) OR (sender=? AND receiver=?) ORDER BY msg_id ASC");
            ps.setString(1, user1); ps.setString(2, user2);
            ps.setString(3, user2); ps.setString(4, user1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new String[]{rs.getString("sender"), rs.getString("text"), rs.getString("time")});
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static ArrayList<String> getConversationPartners(String username) {
        ArrayList<String> partners = new ArrayList<>();
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "SELECT DISTINCT CASE WHEN sender=? THEN receiver ELSE sender END as partner " +
                "FROM messages WHERE sender=? OR receiver=? ORDER BY msg_id DESC");
            ps.setString(1, username); ps.setString(2, username); ps.setString(3, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { String p = rs.getString("partner"); if (!partners.contains(p)) partners.add(p); }
        } catch (SQLException e) { e.printStackTrace(); }
        return partners;
    }

    // ==================== TAG FILTERS ====================

    public static void setUserTagFilters(String username, ArrayList<String> tags) {
        try {
            exec("DELETE FROM user_tag_filters WHERE username=?", username);
            for (String tag : tags) {
                PreparedStatement ps = databaseConnection.prepareStatement("INSERT IGNORE INTO user_tag_filters VALUES(?,?)");
                ps.setString(1, username); ps.setString(2, tag); ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static ArrayList<String> getUserTagFilters(String username) {
        return qList("SELECT tag FROM user_tag_filters WHERE username=?", "tag", username);
    }

    // ==================== USER QUERIES ====================

    public static ArrayList<User> getAllUsers() {
        ArrayList<User> all = new ArrayList<>();
        try {
            ResultSet rs = databaseConnection.prepareStatement("SELECT * FROM users").executeQuery();
            while (rs.next()) all.add(buildUser(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return all;
    }

    public static User getUserWithUsername(String username) {
        try {
            PreparedStatement ps = databaseConnection.prepareStatement("SELECT * FROM users WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return buildUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private static User buildUser(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        boolean isClub = rs.getInt("is_club") == 1;
        String salt = rs.getString("salt"); if (salt == null) salt = "";
        User user;
        if (isClub) user = new ClubUser(username, rs.getString("display_name"),
            rs.getString("email"), rs.getString("password"), salt, rs.getString("bio"));
        else user = new User(username, rs.getString("display_name"),
            rs.getString("email"), rs.getString("password"), salt, rs.getString("bio"));
        user.setVerified(rs.getInt("verified") == 1);
        try { user.setXp(rs.getInt("xp")); } catch (SQLException ignored) {}
        user.setInterests(getInterests(username));
        user.setFollowing(getFollowing(username));
        user.setFollowers(getFollowers(username));
        user.setNotifications(getNotifications(username));
        return user;
    }

    public static void updateUserVerified(String u, boolean v) {
        try { PreparedStatement ps = databaseConnection.prepareStatement("UPDATE users SET verified=? WHERE username=?");
            ps.setInt(1, v?1:0); ps.setString(2, u); ps.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); }
    }
    public static void updateUserPassword(String u, String hash, String salt) {
        try { PreparedStatement ps = databaseConnection.prepareStatement("UPDATE users SET password=?,salt=? WHERE username=?");
            ps.setString(1, hash); ps.setString(2, salt); ps.setString(3, u); ps.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); }
    }
    public static void updateUserBio(String u, String bio) {
        try { PreparedStatement ps = databaseConnection.prepareStatement("UPDATE users SET bio=? WHERE username=?");
            ps.setString(1, bio); ps.setString(2, u); ps.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); }
    }

    // ==================== EVENT QUERIES ====================

    public static ArrayList<Event> getAllEvents() {
        ArrayList<Event> all = new ArrayList<>();
        try {
            ResultSet rs = databaseConnection.prepareStatement("SELECT * FROM events ORDER BY event_id DESC").executeQuery();
            while (rs.next()) all.add(buildEvent(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return all;
    }

    private static Event buildEvent(ResultSet rs) throws SQLException {
        int eid = rs.getInt("event_id");
        java.time.LocalDateTime dt = java.time.LocalDateTime.parse(rs.getString("date_time"));
        java.time.LocalDateTime endDt = parseOptDT(rs.getString("end_date_time"));
        java.time.LocalDateTime deadline = parseOptDT(rs.getString("registration_deadline"));
        Event ev = new Event(eid, rs.getString("title"), rs.getString("description"),
            rs.getString("location"), dt, endDt, deadline, rs.getInt("capacity"), rs.getString("creator_username"));
        try { ev.setImagePath(rs.getString("image_path")); } catch (SQLException ignored) {}
        try { ev.setXpReward(rs.getInt("xp_reward")); } catch (SQLException ignored) {}
        try { ev.setMinTierIndex(rs.getInt("min_tier")); } catch (SQLException ignored) {}
        ev.setTags(getEventTags(eid));
        ev.setAttendanceMap(getAttendanceMap(eid));
        ev.setComments(getComments(eid));
        return ev;
    }

    private static java.time.LocalDateTime parseOptDT(String s) {
        if (s == null || s.isEmpty() || s.equals("null")) return null;
        try { return java.time.LocalDateTime.parse(s); } catch (Exception e) { return null; }
    }

    public static ArrayList<Comment> getComments(int eid) {
        ArrayList<Comment> list = new ArrayList<>();
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "SELECT * FROM comments WHERE event_id=? ORDER BY comment_id ASC");
            ps.setInt(1, eid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int pid = 0; try { pid = rs.getInt("parent_comment_id"); } catch (Exception ignored) {}
                list.add(new Comment(rs.getInt("comment_id"), rs.getString("username"),
                    rs.getString("text"), rs.getString("time"), pid));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static ArrayList<Integer> getPopularEventIds(int limit) {
        ArrayList<Integer> ids = new ArrayList<>();
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "SELECT event_id, COUNT(*) as cnt FROM attendance GROUP BY event_id ORDER BY cnt DESC LIMIT ?");
            ps.setInt(1, limit); ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("event_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return ids;
    }

    public static ArrayList<Integer> getRecommendedEventIds(String username, int limit) {
        ArrayList<Integer> ids = new ArrayList<>();
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "SELECT DISTINCT e.event_id FROM events e " +
                "JOIN event_tags et ON e.event_id=et.event_id " +
                "JOIN user_interests ui ON et.tag=ui.interest " +
                "WHERE ui.username=? AND e.creator_username!=? " +
                "AND e.event_id NOT IN (SELECT event_id FROM attendance WHERE username=?) " +
                "ORDER BY e.event_id DESC LIMIT ?");
            ps.setString(1, username); ps.setString(2, username);
            ps.setString(3, username); ps.setInt(4, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("event_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return ids;
    }

    // ==================== LEADERBOARD ====================

    public static ArrayList<User> getLeaderboard(int limit) {
        ArrayList<User> list = new ArrayList<>();
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "SELECT * FROM users ORDER BY xp DESC LIMIT ?");
            ps.setInt(1, limit); ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(buildUser(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ==================== XP ====================

    public static int getUserXP(String u) {
        try { PreparedStatement ps = databaseConnection.prepareStatement("SELECT xp FROM users WHERE username=?");
            ps.setString(1, u); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("xp"); } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static void addXP(String u, int amount) {
        try { PreparedStatement ps = databaseConnection.prepareStatement("UPDATE users SET xp=GREATEST(xp+?,0) WHERE username=?");
            ps.setInt(1, amount); ps.setString(2, u); ps.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); }
    }

    public static boolean isEmailTaken(String email) {
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE email=?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static void updateEventImage(int eventId, String path) {
        try { PreparedStatement ps = databaseConnection.prepareStatement("UPDATE events SET image_path=? WHERE event_id=?");
            ps.setString(1, path); ps.setInt(2, eventId); ps.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); }
    }

    public static boolean isDatabaseEmpty() {
        try { ResultSet rs = databaseConnection.prepareStatement("SELECT COUNT(*) FROM users").executeQuery();
            if (rs.next()) return rs.getInt(1) == 0; } catch (SQLException e) { e.printStackTrace(); }
        return true;
    }

    private static ArrayList<String> qList(String sql, String col, Object param) {
        ArrayList<String> list = new ArrayList<>();
        try { PreparedStatement ps = databaseConnection.prepareStatement(sql);
            if (param instanceof Integer) ps.setInt(1, (Integer)param);
            else ps.setString(1, param.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString(col)); } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
