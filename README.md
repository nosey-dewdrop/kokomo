# League of Bilkent

A campus event discovery and social engagement platform designed for Bilkent University students and clubs. Students can create, discover, and attend campus events while earning XP and climbing tier ranks through participation.

## Features

- **Event Management** — Create events with title, location, date/time, capacity, tags, XP rewards, and auto-generated posters.
- **Event Discovery** — Browse events in a feed with filtering (All, Following, Clubs, This Week, custom tags) and sorting (Date, Location, XP, Popularity).
- **Personalized Recommendations** — "For You" suggestions based on user interests.
- **Gamification** — Earn XP by creating events, attending, commenting, and gaining followers. 5-tier ranking system: Newcomer, Active, Experienced, Trusted, Legend.
- **Social Features** — Follow users, send direct messages, comment on events with threaded replies, receive notifications.
- **RSVP System** — Mark attendance as Going, Interested, or Maybe with capacity and deadline enforcement.
- **Calendar View** — Monthly calendar displaying upcoming events.
- **Leaderboard** — Top users ranked by XP with tier badges.
- **Authentication** — Bilkent email verification via SMTP, SHA-256 password hashing with salt.

## Technologies

- **Language:** Java (JDK 17+)
- **UI Framework:** Java Swing (AWT/Graphics2D)
- **Database:** MySQL 8.0+ (JDBC)
- **Email Verification:** JavaMail API (Bilkent SMTP)
- **Security:** SHA-256 hashing with salt
- **Network:** Java-based Network Manager for multi-computer communication

## Project Structure

    leagueofbilkent/
    ├── model/          Domain classes, database operations, configuration
    ├── screens/        Main windows (login, register, home)
    ├── panels/         UI views (feed, profile, calendar, messaging, etc.)
    ├── tools/          Utilities (network manager, password hashing, email, poster generation)
    └── lib/            Dependencies

## Setup

1. Install **Java JDK 17+** and **MySQL 8.0+**
2. Create a MySQL database named `league_of_bilkent`
3. Create a `credentials.properties` file in the project root:

    db.url=jdbc:mysql://localhost:3306/league_of_bilkent
    db.user=YOUR_MYSQL_USERNAME
    db.password=YOUR_MYSQL_PASSWORD
    mail.address=YOUR_EMAIL
    mail.password=YOUR_EMAIL_APP_PASSWORD

4. Compile and run:

    javac -cp "lib/*" -d build model/*.java tools/*.java panels/*.java screens/*.java
    java -cp "build:lib/*" screens.MainFile

## Team

- **Osman** — User System, Authentication, Security
- **Damla** — Event Management, Poster Generator, Attendance
- **Emir** — Feed & Discover Panels, Calendar View, Search
- **Eylül** — Messaging System, Notifications, Profile Panel
- **Ege** — Experience (XP) System, Leaderboard, UI Helper
- **Evrim** — MySQL Database, Network Manager, Main Execution Flow

## License

MIT
