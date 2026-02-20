import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class Event implements Searchable {

    private int id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime dateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime registrationDeadline;
    private int capacity;
    private String creatorUsername;
    private ArrayList<String> tags;
    private ArrayList<Comment> comments;
    private HashMap<String, AttendanceStatus> attendanceMap;

    // NEW fields
    private String imagePath;      // poster image file path
    private int xpReward;          // XP given to attendees
    private int minTierIndex;      // minimum tier required (0=anyone)

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");

    public Event(int id, String title, String description, String location,
                 LocalDateTime dateTime, LocalDateTime endDateTime,
                 LocalDateTime registrationDeadline, int capacity, String creatorUsername) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.dateTime = dateTime;
        this.endDateTime = endDateTime;
        this.registrationDeadline = registrationDeadline;
        this.capacity = capacity;
        this.creatorUsername = creatorUsername;
        this.tags = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.attendanceMap = new HashMap<>();
        this.imagePath = "";
        this.xpReward = AppConstants.DEFAULT_EVENT_XP;
        this.minTierIndex = 0;
    }

    public Event(int id, String title, String description, String location,
                 LocalDateTime dateTime, int capacity, String creatorUsername) {
        this(id, title, description, location, dateTime,
             dateTime.plusHours(2), dateTime.minusDays(1), capacity, creatorUsername);
    }

    // GETTERS
    public int getId()                        { return id; }
    public String getTitle()                  { return title; }
    public String getDescription()            { return description; }
    public String getLocation()               { return location; }
    public LocalDateTime getDateTime()        { return dateTime; }
    public LocalDateTime getEndDateTime()     { return endDateTime; }
    public LocalDateTime getRegistrationDeadline() { return registrationDeadline; }
    public int getCapacity()                  { return capacity; }
    public String getCreatorUsername()         { return creatorUsername; }
    public ArrayList<String> getTags()        { return tags; }
    public ArrayList<Comment> getComments()   { return comments; }
    public HashMap<String, AttendanceStatus> getAttendanceMap() { return attendanceMap; }
    public String getImagePath()              { return imagePath; }
    public int getXpReward()                  { return xpReward; }
    public int getMinTierIndex()              { return minTierIndex; }

    public String getDateStr()    { return dateTime.format(FMT); }
    public String getEndDateStr() { return endDateTime != null ? endDateTime.format(FMT) : ""; }
    public String getDeadlineStr(){ return registrationDeadline != null ? registrationDeadline.format(FMT) : ""; }

    public String getMinTierName() {
        if (minTierIndex <= 0) return "Anyone";
        return AppConstants.TIER_NAMES[Math.min(minTierIndex, AppConstants.TIER_NAMES.length - 1)];
    }

    public ArrayList<String> getAttendees() { return new ArrayList<>(attendanceMap.keySet()); }

    public ArrayList<String> getAttendeesByStatus(AttendanceStatus status) {
        ArrayList<String> list = new ArrayList<>();
        for (var entry : attendanceMap.entrySet())
            if (entry.getValue() == status) list.add(entry.getKey());
        return list;
    }

    public int getGoingCount()      { return getAttendeesByStatus(AttendanceStatus.GOING).size(); }
    public int getInterestedCount() { return getAttendeesByStatus(AttendanceStatus.INTERESTED).size(); }
    public int getMaybeCount()      { return getAttendeesByStatus(AttendanceStatus.MAYBE).size(); }
    public int getAttendeeCount()   { return attendanceMap.size(); }
    public boolean isFull()         { return getGoingCount() >= capacity; }
    public boolean isDeadlinePassed() { return registrationDeadline != null && LocalDateTime.now().isAfter(registrationDeadline); }
    public boolean isEventPast()    { return LocalDateTime.now().isAfter(dateTime); }

    // Check if user meets tier requirement
    public boolean canJoin(int userXP) {
        if (minTierIndex <= 0) return true;
        return AppConstants.getTierIndex(userXP) >= minTierIndex;
    }

    // SETTERS
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }
    public void setRegistrationDeadline(LocalDateTime deadline) { this.registrationDeadline = deadline; }
    public void setTags(ArrayList<String> tags) { this.tags = tags; }
    public void setComments(ArrayList<Comment> comments) { this.comments = comments; }
    public void setAttendanceMap(HashMap<String, AttendanceStatus> map) { this.attendanceMap = map; }
    public void setImagePath(String path) { this.imagePath = path; }
    public void setXpReward(int xp) { this.xpReward = xp; }
    public void setMinTierIndex(int idx) { this.minTierIndex = idx; }

    public void setAttendees(ArrayList<String> attendees) {
        for (String u : attendees)
            if (!attendanceMap.containsKey(u)) attendanceMap.put(u, AttendanceStatus.GOING);
    }

    public void addTag(String tag)         { if (!tags.contains(tag)) tags.add(tag); }
    public void setAttendance(String u, AttendanceStatus s) { attendanceMap.put(u, s); }
    public void removeAttendance(String u) { attendanceMap.remove(u); }
    public AttendanceStatus getAttendanceStatus(String u) { return attendanceMap.get(u); }
    public boolean isAttending(String u)   { return attendanceMap.containsKey(u); }
    public void addAttendee(String u)      { setAttendance(u, AttendanceStatus.GOING); }
    public void removeAttendee(String u)   { removeAttendance(u); }
    public void addComment(Comment c)      { comments.add(c); }

    @Override
    public boolean matchesSearch(String query) {
        return title.toLowerCase().contains(query) ||
               description.toLowerCase().contains(query) ||
               creatorUsername.contains(query) ||
               location.toLowerCase().contains(query) ||
               tags.stream().anyMatch(t -> t.toLowerCase().contains(query));
    }

    @Override
    public String getSearchSummary() { return title + " - " + location + " (" + getDateStr() + ")"; }

    @Override
    public String toString() { return "Event{" + id + ", " + title + ", by=" + creatorUsername + "}"; }
}
