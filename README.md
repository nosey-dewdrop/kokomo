# Squirrel

A campus event management platform for Bilkent University students. Clubs and individuals can create events, RSVP, follow each other, and engage in discussions.

## Features

- **Create & Discover Events** — Concerts, tournaments, study groups, social meetups
- **Attendance System** — Going / Interested / Maybe with capacity tracking
- **Personal Calendar** — Attending events auto-added to your monthly calendar
- **Follow System** — Follow clubs and friends, get notified about their events
- **XP & Tier System** — Earn XP by creating events, attending, commenting → level up
- **Smart Recommendations** — Personalized event suggestions based on your interests
- **Threaded Comments** — Discussions and Q&A under each event
- **Direct Messaging** — Chat with other users
- **Leaderboard** — See the most active users on campus

## Requirements

- Java JDK 17+
- MySQL 8.0+

## Setup

### 1. Create the database
```
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS squirrel_db;"
```

### 2. Compile
```
javac -cp ".:lib/*" model/*.java tools/*.java panels/*.java screens/*.java
```
> Windows: use `;` instead of `:`

### 3. Run
```
java -cp ".:lib/*:model:panels:screens:tools" screens.MainFile
```
> Windows: `java -cp ".;lib/*;model;panels;screens;tools" screens.MainFile`

## Demo Accounts

| Username | Password | Type |
|----------|----------|------|
| damla | 1234 | Student |
| ali_k | 1234 | Student |
| elif_s | 1234 | Student |
| ieee_bilkent | 1234 | Club |
| music_club | 1234 | Club |

## Project Structure
```
├── screens/        Main windows
│   ├── MainFile.java           Entry point
│   ├── HomeScreen.java         Main screen (sidebar + content)
│   ├── LoginScreen.java        Login
│   └── RegisterScreen.java     Registration
│
├── panels/         Content panels
│   ├── FeedPanel.java          Event feed with poster cards
│   ├── EventDetailPanel.java   Event details + attendance + comments
│   ├── CreateEventPanel.java   Event creation form
│   ├── DiscoverPanel.java      Trending + recommendations
│   ├── CalendarPanel.java      Monthly calendar view
│   ├── LeaderboardPanel.java   XP rankings
│   ├── ProfilePanel.java       User profile
│   ├── SearchPanel.java        Search results
│   ├── MessagingPanel.java     Direct messages
│   ├── NotificationsPanel.java Notifications
│   ├── UIHelper.java           UI utility methods
│   ├── ForgotPasswordDialog.java
│   └── InterestSelectionDialog.java
│
├── model/          Data models & database
│   ├── AppConstants.java       Colors, fonts, strings, config
│   ├── User.java               User class (implements Searchable)
│   ├── ClubUser.java           Club account (extends User)
│   ├── Event.java              Event class (implements Searchable)
│   ├── Comment.java            Threaded comments
│   ├── AttendanceStatus.java   Going/Interested/Maybe enum
│   ├── Searchable.java         Search interface (polymorphism)
│   ├── Database.java           MySQL connection & CRUD
│   └── SampleData.java         Demo data loader
│
├── tools/          Utilities
│   ├── PasswordUtil.java       SHA-256 password hashing
│   ├── EmailSender.java        Email verification
│   ├── PosterGenerator.java    Auto poster generation
│   └── ExperienceSystem.java   XP/Tier calculations
│
├── lib/            Dependencies (.jar files)
├── credentials.properties
└── .gitignore
```

## Tech Stack

- **Java Swing** — GUI
- **MySQL** — Database
- **SHA-256 + Salt** — Password security
- **JavaMail API** — Email verification
- **MVC Pattern** — screens (view) / model (data) / tools (service)

## Design Inspirations

| Platform | What we took |
|----------|-------------|
| Strava | Community-driven feed, follow system |
| Eventbrite | Event creation, RSVP, capacity |
| X (Twitter) | Timeline structure |
| Instagram | Profile layout, Discover page |
| Eksi Sozluk | Threaded comment structure |
| Facebook Events | Going/Interested/Maybe system |

## OOP Concepts Used

- **Inheritance** — ClubUser extends User
- **Polymorphism** — Searchable interface, isClub() override
- **Encapsulation** — Private fields with getters/setters
- **Abstraction** — Searchable interface for unified search

## License

MIT License
