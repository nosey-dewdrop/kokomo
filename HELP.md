# HELP — Emergency & Troubleshooting Guide

> Quick reference for when things go wrong. Keep this file handy.

---

## 1. Database Connection Failures

**Symptom:** `MySQL connection error!` in the console.

**Check these first:**

1. Is MySQL running?
```bash
   # macOS
   brew services list | grep mysql

   # Linux
   sudo systemctl status mysql
```
   If it's stopped, start it:
```bash
   # macOS
   brew services start mysql

   # Linux
   sudo systemctl start mysql
```

2. Are the credentials correct? Open `credentials.properties` and verify:
```
   db.url=jdbc:mysql://localhost:3306/kokomo
   db.user=root
   db.password=YOUR_PASSWORD
```
   If `credentials.properties` doesn't exist, the app falls back to `AppConstants.java` defaults.

3. Does the database exist?
```bash
   mysql -u root -p -e "SHOW DATABASES;" | grep kokomo
```
   If not, create it:
```bash
   mysql -u root -p -e "CREATE DATABASE kokomo;"
```

---

## 2. Full Database Reset (Nuclear Option)

**When to use:** Corrupted data, broken foreign keys, or you just need a clean slate.
```sql
-- Connect to MySQL first
mysql -u root -p

-- Drop everything and start fresh
DROP DATABASE IF EXISTS kokomo;
CREATE DATABASE kokomo;
```

Then restart the application. All tables will be auto-created by `Database.createTables()`, and if the database is empty, sample data will be loaded automatically.

---

## 3. Selective Table Cleanup

If you only need to clear specific data without a full reset:
```sql
USE kokomo;

-- Clear all events and related data
DELETE FROM comments;
DELETE FROM attendance;
DELETE FROM event_tags;
DELETE FROM events;

-- Clear all users and related data
DELETE FROM messages;
DELETE FROM notifications;
DELETE FROM follows;
DELETE FROM user_interests;
DELETE FROM user_tag_filters;
DELETE FROM users;

-- Clear only messages
DELETE FROM messages;

-- Clear only notifications
DELETE FROM notifications;

-- Reset auto-increment counters after cleanup
ALTER TABLE events AUTO_INCREMENT = 1;
ALTER TABLE comments AUTO_INCREMENT = 1;
ALTER TABLE notifications AUTO_INCREMENT = 1;
ALTER TABLE messages AUTO_INCREMENT = 1;
```

---

## 4. Common Runtime Errors

### `NullPointerException` on launch
Most likely cause: the MySQL JDBC driver jar is missing from the classpath. Make sure `mysql-connector-j-8.3.0.jar` is in the project root and included in your run command:
```bash
java -cp ".:src:mysql-connector-j-8.3.0.jar:javax.mail.jar" MainFile
```

### `ClassNotFoundException: com.mysql.cj.jdbc.Driver`
Same issue — the connector jar is either missing or the classpath is wrong. Re-download it:
```bash
curl -L -o mysql-connector-j-8.3.0.jar \
  "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar"
```

### Events not showing up in the feed
Check that events actually exist in the database:
```sql
SELECT event_id, title, date_time FROM events ORDER BY event_id DESC LIMIT 10;
```
If the table is empty, the sample data might not have loaded. Verify with:
```sql
SELECT COUNT(*) FROM users;
```
If `0`, restart the app — it calls `SampleData.loadSampleData()` when no users exist.

### Email verification not sending
The email system requires `javax.mail.jar` in the classpath and valid SMTP credentials in `EmailSender.java`. If email isn't critical for your setup, users can still be verified manually:
```sql
UPDATE users SET verified = 1 WHERE username = 'TARGET_USERNAME';
```

### Poster images not appearing
Generated posters are saved to a local `posters/` directory. If this directory doesn't exist or isn't writable, poster generation silently fails. Fix:
```bash
mkdir -p posters
chmod 755 posters
```

---

## 5. User Account Emergencies

### Reset a user's password
```sql
-- This sets a temporary raw password. The user should change it after login.
-- Note: This bypasses the salt-based hashing and will only work if you
-- also update the salt. Best to do this programmatically through PasswordUtil.
-- Quick workaround: delete and re-register the user.

DELETE FROM users WHERE username = 'TARGET_USERNAME';
-- User will need to re-register. Their events/comments will remain orphaned.
```

### Remove a problematic user entirely
```sql
DELETE FROM messages WHERE sender = 'TARGET' OR receiver = 'TARGET';
DELETE FROM notifications WHERE username = 'TARGET';
DELETE FROM follows WHERE follower_username = 'TARGET' OR following_username = 'TARGET';
DELETE FROM attendance WHERE username = 'TARGET';
DELETE FROM comments WHERE username = 'TARGET';
DELETE FROM user_interests WHERE username = 'TARGET';
DELETE FROM user_tag_filters WHERE username = 'TARGET';
DELETE FROM users WHERE username = 'TARGET';
```

### Promote a user to club account
```sql
UPDATE users SET is_club = 1 WHERE username = 'TARGET_USERNAME';
```

---

## 6. Database Schema Reference

| Table | Purpose | Primary Key |
|-------|---------|-------------|
| `users` | All user & club accounts | `username` |
| `user_interests` | User interest tags | `(username, interest)` |
| `events` | All events | `event_id` (auto) |
| `event_tags` | Tags assigned to events | `(event_id, tag)` |
| `attendance` | RSVP status per event | `(event_id, username)` |
| `comments` | Event comments (threaded) | `comment_id` (auto) |
| `follows` | Follow relationships | `(follower, following)` |
| `notifications` | User notifications | `notif_id` (auto) |
| `messages` | Direct messages | `msg_id` (auto) |
| `user_tag_filters` | Feed filter preferences | `(username, tag)` |

---

## 7. Quick Health Check Script

Run this to verify everything is working:
```bash
#!/bin/bash
echo "=== Kokomo Health Check ==="

# MySQL running?
if mysql -u root -p -e "SELECT 1" &>/dev/null; then
    echo "[OK] MySQL is running"
else
    echo "[FAIL] MySQL is not reachable"
    exit 1
fi

# Database exists?
if mysql -u root -p -e "USE kokomo" &>/dev/null; then
    echo "[OK] Database exists"
else
    echo "[FAIL] Database 'kokomo' not found"
    exit 1
fi

# Tables exist?
TABLE_COUNT=$(mysql -u root -p -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='kokomo'")
echo "[INFO] $TABLE_COUNT tables found"

# Users?
USER_COUNT=$(mysql -u root -p -N -e "SELECT COUNT(*) FROM kokomo.users")
echo "[INFO] $USER_COUNT users in database"

# Events?
EVENT_COUNT=$(mysql -u root -p -N -e "SELECT COUNT(*) FROM kokomo.events")
echo "[INFO] $EVENT_COUNT events in database"

echo "=== Done ==="
```

---

## 8. Logs & Debugging

The application prints all SQL errors to `stderr`. To capture logs:
```bash
java -cp ".:src:mysql-connector-j-8.3.0.jar:javax.mail.jar" MainFile 2> error.log
```

To watch logs in real time while the app runs:
```bash
java -cp ".:src:mysql-connector-j-8.3.0.jar:javax.mail.jar" MainFile 2>&1 | tee app.log
```

---

## 9. Migration Notes

The app handles schema migrations automatically on startup. If you're upgrading from an older version, `Database.createTables()` will add any missing columns (like `salt`, `xp`, `end_date_time`, `registration_deadline`, `image_path`, `xp_reward`, `min_tier`, `parent_comment_id`). It will also migrate the legacy `attendees` table to the new `attendance` table if it exists.

If a migration fails silently, you can run it manually:
```sql
ALTER TABLE users ADD COLUMN salt VARCHAR(64) DEFAULT '';
ALTER TABLE users ADD COLUMN xp INT DEFAULT 0;
ALTER TABLE events ADD COLUMN end_date_time VARCHAR(50);
ALTER TABLE events ADD COLUMN registration_deadline VARCHAR(50);
ALTER TABLE events ADD COLUMN image_path VARCHAR(500) DEFAULT '';
ALTER TABLE events ADD COLUMN xp_reward INT DEFAULT 5;
ALTER TABLE events ADD COLUMN min_tier INT DEFAULT 0;
ALTER TABLE comments ADD COLUMN parent_comment_id INT DEFAULT 0;
```

---

*Last updated: February 2026*
