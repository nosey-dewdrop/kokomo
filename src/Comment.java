import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Yorum modeli - threaded yorum destegi.
 * parentId ile yanit zincirleri olusturulur.
 */
public class Comment {

    private int id;
    private String username;
    private String text;
    private String time;
    private int parentId;  // 0 = ust seviye yorum, >0 = yanit

    public Comment(String username, String text) {
        this.id = 0;
        this.username = username;
        this.text = text;
        this.time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
        this.parentId = 0;
    }

    public Comment(String username, String text, String time) {
        this.id = 0;
        this.username = username;
        this.text = text;
        this.time = time;
        this.parentId = 0;
    }

    public Comment(int id, String username, String text, String time, int parentId) {
        this.id = id;
        this.username = username;
        this.text = text;
        this.time = time;
        this.parentId = parentId;
    }

    // Getters
    public int getId()           { return id; }
    public String getUsername()   { return username; }
    public String getText()      { return text; }
    public String getTime()      { return time; }
    public int getParentId()     { return parentId; }

    // Setters
    public void setId(int id)              { this.id = id; }
    public void setParentId(int parentId)  { this.parentId = parentId; }

    public boolean isReply() {
        return parentId > 0;
    }

    @Override
    public String toString() {
        return "Comment{" + id + ", " + username + ": " + text + (isReply() ? " (reply to " + parentId + ")" : "") + "}";
    }
}
