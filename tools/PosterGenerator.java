package tools;

import model.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/*
 * +--------------------------------------------------------------+
 * |                 <<class>> postergenerator                     |
 * |             auto-generates event poster images                |
 * +--------------------------------------------------------------+
 * | - width=600, height=340 -> poster dimensions                  |
 * | - pastels: color[] -> pastel color palette for backgrounds    |
 * +--------------------------------------------------------------+
 * | + generate(title, tags, datestr, location, xp): string        |
 * |   -> creates poster png, returns file path                    |
 * | + generatedefault(event): string -> shortcut for generate     |
 * | + getcolorfortags(tags): color -> tag-based color selection   |
 * | + getcolorforevent(eventid): color -> id-based color rotation |
 * | + getcategoryemoji(tags): string -> tag-to-emoji mapping      |
 * | - pickcolor(tags): int -> internal color index selector       |
 * +--------------------------------------------------------------+
 * | uses:    event (for generatedefault)                          |
 * | used by: sampledata, createeventpanel, feedpanel              |
 * +--------------------------------------------------------------+
 */
public class PosterGenerator {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 340;
    private static final Random rand = new Random();

    private static final Color[] PASTELS = {
        new Color(0xCE, 0xF3, 0xF6), new Color(0xB8, 0xDF, 0xF5),
        new Color(0xD4, 0xEE, 0xD1), new Color(0xE3, 0xD5, 0xF0),
        new Color(0xFF, 0xE0, 0xC8), new Color(0xD0, 0xF0, 0xF0),
        new Color(0xF0, 0xFB, 0xFC), new Color(0xE3, 0xF1, 0xF8),
        new Color(0xD0, 0xE8, 0xF2), new Color(0xCE, 0xF3, 0xE0)
    };

    private static int pickColor(ArrayList<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return rand.nextInt(PASTELS.length);
        }
        String t = tags.get(0).toLowerCase();
        if (t.contains("software") || t.contains("ai") || t.contains("algorithm")) {
            return 0;
        }
        if (t.contains("music") || t.contains("concert")) {
            return 3;
        }
        if (t.contains("sport") || t.contains("fitness")) {
            return 2;
        }
        if (t.contains("art") || t.contains("photo")) {
            return 4;
        }
        if (t.contains("game")) {
            return 5;
        }
        if (t.contains("food")) {
            return 6;
        }
        if (t.contains("environment")) {
            return 9;
        }
        return rand.nextInt(PASTELS.length);
    }

    public static Color getColorForTags(ArrayList<String> tags) {
        return PASTELS[pickColor(tags)];
    }

    public static Color getColorForEvent(int eventId) {
        return PASTELS[Math.abs(eventId) % PASTELS.length];
    }

    public static String getCategoryEmoji(ArrayList<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "\uD83C\uDFAD";
        }
        String firstTag = tags.get(0).toLowerCase();
        if (firstTag.contains("music")) {
            return "\uD83C\uDFB5";
        }
        if (firstTag.contains("sport") || firstTag.contains("fitness")) {
            return "\uD83C\uDFC3";
        }
        if (firstTag.contains("food")) {
            return "\uD83C\uDF55";
        }
        if (firstTag.contains("software") || firstTag.contains("ai") || firstTag.contains("algorithm")) {
            return "\uD83D\uDCBB";
        }
        if (firstTag.contains("art") || firstTag.contains("photo")) {
            return "\uD83C\uDFA8";
        }
        if (firstTag.contains("game")) {
            return "\uD83C\uDFAE";
        }
        if (firstTag.contains("education") || firstTag.contains("workshop")) {
            return "\uD83D\uDCDA";
        }
        return "\uD83C\uDFAD";
    }

    public static String generate(String title, ArrayList<String> tags, String dateStr, String location, int xp) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg = PASTELS[pickColor(tags)];
        g.setColor(bg);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(new Color(255, 255, 255, 35));
        g.fillOval(WIDTH - 130, -40, 220, 220);
        g.fillOval(-60, HEIGHT - 110, 180, 180);
        g.setColor(new Color(255, 255, 255, 20));
        g.fillOval(WIDTH / 2 - 70, HEIGHT / 2 - 70, 140, 140);
        g.dispose();

        try {
            File dir = new File("posters");
            dir.mkdirs();
            String fname = "posters/poster_" + System.currentTimeMillis() + "_" + rand.nextInt(9999) + ".png";
            javax.imageio.ImageIO.write(img, "png", new File(fname));
            return fname;
        } catch (Exception e) {
            return null;
        }
    }

    public static String generateDefault(model.Event ev) {
        return generate(ev.getTitle(), ev.getTags(), ev.getDateStr(), ev.getLocation(), ev.getXpReward());
    }
}
