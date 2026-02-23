# How to Use — League of Bilkent

## What You Need

1. **Java JDK 17 or higher** — to compile and run the app
2. **MySQL 8.0 or higher** — the app stores all data in a MySQL database
3. **A terminal** — Terminal (Mac), Command Prompt or PowerShell (Windows)

### Check if you have them
```
java -version
javac -version
mysql --version
```

If any of these fail, install them first:
- Java: https://adoptium.net
- MySQL: https://dev.mysql.com/downloads/mysql/

---

## Step 1: Set Up the Database

League of Bilkent uses **MySQL** as its database. MySQL is a relational database — it stores data in tables with rows and columns. The app creates the following tables automatically on first run:

| Table | What it stores |
|-------|---------------|
| users | Usernames, emails, hashed passwords, bios, XP |
| events | Event title, description, location, dates, capacity |
| attendance | Who is Going/Interested/Maybe for each event |
| follows | Who follows whom |
| comments | Threaded comments under events |
| notifications | User notifications |
| messages | Direct messages between users |
| interests | User interest tags |
| tag_filters | Custom feed tag filters |

### Create the database

Open your terminal and run:
```
mysql -u root -p
```

Enter your MySQL root password when prompted. Then type:
```sql
CREATE DATABASE IF NOT EXISTS league_of_bilkent;
EXIT;
```

### Configure the connection

Open `model/AppConstants.java` and check these lines:
```java
public static final String DB_URL  = "jdbc:mysql://localhost:3306/league_of_bilkent";
public static final String DB_USER = "root";
public static final String DB_PASS = "";
```

If your MySQL root password is not empty, change `DB_PASS` to your password.

---

## Step 2: Compile

Navigate to the project folder in your terminal:
```
cd path/to/our_project_lastversion
```

Then compile all Java files:

**Mac / Linux:**
```
javac -cp ".:lib/*" model/*.java tools/*.java panels/*.java screens/*.java
```

**Windows:**
```
javac -cp ".;lib/*" model/*.java tools/*.java panels/*.java screens/*.java
```

You should see no errors. If you see errors about missing MySQL connector, make sure the `lib/` folder contains `mysql-connector-j.jar`.

---

## Step 3: Run

**Mac / Linux:**
```
java -cp ".:lib/*:model:panels:screens:tools" screens.MainFile
```

**Windows:**
```
java -cp ".;lib/*;model;panels;screens;tools" screens.MainFile
```

The login window will appear. On first run, the app automatically loads sample data (demo users, events, comments).

---

## Step 4: Log In

Use one of the demo accounts:

| Username | Password | Role |
|----------|----------|------|
| damla | 1234 | Student (IEEE President) |
| ali_k | 1234 | Student (Sports Enthusiast) |
| elif_s | 1234 | Student (Art Lover) |
| ieee_bilkent | 1234 | Club (IEEE Bilkent) |
| music_club | 1234 | Club (Music Club) |

Or click the **Register** tab to create your own account. Registration requires a Bilkent email address (@ug.bilkent.edu.tr or @bilkent.edu.tr). A verification code will be shown if email sending is not configured.

---

## Using the App

### Home Screen Layout
```
┌──────────────────────────────────────────────┐
│  [Search Bar]                [Calendar] [Profile] │
├────────────┬─────────────────────────────────┤
│            │                                 │
│  📋 Feed   │   Event cards with posters      │
│  ✨ Discover│   Filters: All/Following/Clubs  │
│  🏆 Board  │   Sort: Date/Location/XP        │
│  ➕ New     │                                 │
│  💬 Messages│                                 │
│  🔔 Notifs │                                 │
│            │                                 │
│  🚪 Logout │                                 │
└────────────┴─────────────────────────────────┘
```

### Feed
- Browse all campus events as poster cards
- Filter by: All, Following, Clubs, This Week, or custom tags
- Sort by: Date, Location, XP Reward, Popularity
- "For You" section shows personalized recommendations based on your interests
- Click any event card to see its details

### Creating an Event
- Click **New Event** in the sidebar
- Fill in: title, description, location, dates, capacity, XP reward
- Optionally set a minimum tier requirement and upload a poster image
- If no poster is uploaded, one is auto-generated with pastel colors
- Add tags (comma separated) so your event appears in the right searches

### Attending an Event
- Open any event and click **Going**, **Interested**, or **Maybe**
- Going adds the event to your calendar and gives you XP
- You can cancel attendance at any time
- Events with a passed deadline will not accept new RSVPs

### Profile
- Click **Profile** in the top bar to see your profile
- View your XP, tier, bio, interests, followers, and events
- Edit your bio and interests anytime
- Click on any username anywhere in the app to visit their profile
- Follow/unfollow other users from their profile

### XP and Tiers
- Earn XP through actions on the platform:

| Action | XP |
|--------|-----|
| Create an event | +10 |
| Attend an event | +5 |
| Post a comment | +2 |
| Gain a follower | +3 |
| Cancel your event | -15 |

- Tiers based on total XP:

| Tier | XP Required |
|------|-------------|
| Newcomer | 0 |
| Active | 50 |
| Experienced | 150 |
| Trusted | 400 |
| Legend | 1000 |

- Some events may require a minimum tier to join

### Search
- Use the search bar at the top to find events and users
- Results show matching events (by title, description, location, tags) and users (by username, display name, interests)
- Click tag chips for quick topic-based search

### Calendar
- Click **Calendar** in the top bar
- See all events for the current month in a grid view
- Navigate between months with arrow buttons
- Click any event in the calendar to see its details

### Messaging
- Click **Messages** in the sidebar
- Start a new conversation by clicking **+ New Message**
- Enter a username to message them directly
- Chat history is saved in the database

### Notifications
- Click **Notifications** in the sidebar
- You get notified when:
  - Someone follows you
  - A followed account creates a new event
  - Someone comments on your event
  - Someone replies to your comment

---

## Email Configuration (Optional)

Email verification works out of the box for testing — if email sending fails, the verification code is shown in a popup.

To enable real email sending, edit `credentials.properties`:
```
email.sender=your_email@gmail.com
email.password=your_app_password
email.smtp.host=smtp.gmail.com
email.smtp.port=587
```

For Gmail, you need to generate an App Password in your Google Account settings.

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| MySQL connection refused | Make sure MySQL is running: `brew services start mysql` (Mac) or `sudo systemctl start mysql` (Linux) |
| ClassNotFoundException for MySQL | Check that `lib/mysql-connector-j.jar` exists |
| Email not sending | This is normal for testing. The code is shown in a popup instead |
| Compile errors | Make sure you are in the project root folder, not inside a subfolder |
| Tables not created | The app creates tables automatically on first run via `Database.createConnection()` |
