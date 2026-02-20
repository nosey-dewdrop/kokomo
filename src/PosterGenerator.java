import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;

public class PosterGenerator {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private static final Random rand = new Random();

    // Gradient renk semalari: {sol-ust, sag-ust, sol-alt, sag-alt, text}
    // 4 noktali gecisli gradientler
    private static final Color[][] SCHEMES = {
        // Ocean Breeze
        {new Color(0,198,255), new Color(0,114,255), new Color(0,114,255), new Color(45,54,122), Color.WHITE},
        // Sunset Vibes
        {new Color(255,154,0), new Color(255,85,85), new Color(255,85,85), new Color(148,0,211), Color.WHITE},
        // Forest Magic
        {new Color(0,210,150), new Color(0,150,100), new Color(0,100,70), new Color(0,60,60), Color.WHITE},
        // Purple Galaxy
        {new Color(138,43,226), new Color(75,0,130), new Color(75,0,130), new Color(25,0,60), Color.WHITE},
        // Cherry Blossom
        {new Color(255,105,180), new Color(255,20,147), new Color(200,40,120), new Color(120,20,80), Color.WHITE},
        // Electric Blue
        {new Color(0,200,255), new Color(0,100,255), new Color(80,0,255), new Color(40,0,120), Color.WHITE},
        // Golden Hour
        {new Color(255,200,50), new Color(255,140,0), new Color(255,80,0), new Color(180,30,0), Color.WHITE},
        // Mint Fresh
        {new Color(100,255,218), new Color(0,230,180), new Color(0,180,160), new Color(0,100,100), Color.WHITE},
        // Fire & Ice
        {new Color(255,80,80), new Color(200,40,120), new Color(80,40,200), new Color(40,80,255), Color.WHITE},
        // Northern Lights
        {new Color(0,255,150), new Color(0,200,255), new Color(100,0,255), new Color(50,0,100), Color.WHITE},
        // Peach Dream
        {new Color(255,180,150), new Color(255,120,100), new Color(200,80,120), new Color(100,40,80), Color.WHITE},
        // Cyber Neon
        {new Color(0,255,255), new Color(0,200,100), new Color(200,0,255), new Color(100,0,150), Color.WHITE},
    };

    // Tag'e gore emoji + renk
    private static String getEmojiForTags(ArrayList<String> tags) {
        if (tags == null || tags.isEmpty()) return "\u2728";  // sparkles
        String tag = tags.get(0).toLowerCase();
        if (tag.contains("software") || tag.contains("python") || tag.contains("web")) return "\uD83D\uDCBB";  // laptop
        if (tag.contains("ai") || tag.contains("data")) return "\uD83E\uDD16";  // robot
        if (tag.contains("algorithm") || tag.contains("contest")) return "\uD83E\uDDE0";  // brain
        if (tag.contains("gamedev") || tag.contains("esport") || tag.contains("game")) return "\uD83C\uDFAE";  // gamepad
        if (tag.contains("music") || tag.contains("concert") || tag.contains("acoustic")) return "\uD83C\uDFB5";  // music note
        if (tag.contains("sport") || tag.contains("fitness") || tag.contains("run")) return "\u26BD";  // soccer
        if (tag.contains("basketball")) return "\uD83C\uDFC0";  // basketball
        if (tag.contains("art") || tag.contains("exhibition")) return "\uD83C\uDFA8";  // palette
        if (tag.contains("photo")) return "\uD83D\uDCF7";  // camera
        if (tag.contains("food") || tag.contains("social")) return "\uD83C\uDF55";  // pizza
        if (tag.contains("cinema") || tag.contains("theater")) return "\uD83C\uDFAC";  // clapper
        if (tag.contains("book") || tag.contains("literature")) return "\uD83D\uDCDA";  // books
        if (tag.contains("philosophy") || tag.contains("history")) return "\uD83E\uDDD0";  // thinking
        if (tag.contains("environment") || tag.contains("nature")) return "\uD83C\uDF3F";  // leaf
        if (tag.contains("education") || tag.contains("workshop")) return "\uD83D\uDCDD";  // memo
        if (tag.contains("finance") || tag.contains("entrepreneur")) return "\uD83D\uDCB0";  // money bag
        if (tag.contains("travel")) return "\u2708\uFE0F";  // airplane
        if (tag.contains("robot")) return "\uD83E\uDD16";  // robot
        if (tag.contains("volunteer")) return "\u2764\uFE0F";  // heart
        if (tag.contains("cyber")) return "\uD83D\uDD12";  // lock
        return "\u2728";  // default sparkles
    }

    private static int getSchemeIndex(ArrayList<String> tags) {
        if (tags == null || tags.isEmpty()) return rand.nextInt(SCHEMES.length);
        String tag = tags.get(0).toLowerCase();
        if (tag.contains("software") || tag.contains("ai") || tag.contains("python")) return 5;  // electric blue
        if (tag.contains("algorithm") || tag.contains("cyber")) return 0;  // ocean
        if (tag.contains("music") || tag.contains("concert")) return 3;  // purple galaxy
        if (tag.contains("sport") || tag.contains("fitness")) return 2;  // forest
        if (tag.contains("art") || tag.contains("photo")) return 4;  // cherry blossom
        if (tag.contains("game") || tag.contains("esport")) return 11;  // cyber neon
        if (tag.contains("food") || tag.contains("social")) return 6;  // golden hour
        if (tag.contains("environment")) return 7;  // mint
        if (tag.contains("cinema") || tag.contains("theater")) return 1;  // sunset
        if (tag.contains("education") || tag.contains("workshop")) return 9;  // northern lights
        return rand.nextInt(SCHEMES.length);
    }

    // 4 koseli gecisli gradient olustur
    private static void drawMeshGradient(Graphics2D g, Color tl, Color tr, Color bl, Color br) {
        for (int y = 0; y < HEIGHT; y++) {
            float fy = (float) y / HEIGHT;
            Color left = blendColor(tl, bl, fy);
            Color right = blendColor(tr, br, fy);
            GradientPaint gp = new GradientPaint(0, y, left, WIDTH, y, right);
            g.setPaint(gp);
            g.fillRect(0, y, WIDTH, 1);
        }
    }

    private static Color blendColor(Color c1, Color c2, float ratio) {
        float ir = 1.0f - ratio;
        int r = (int)(c1.getRed() * ir + c2.getRed() * ratio);
        int g = (int)(c1.getGreen() * ir + c2.getGreen() * ratio);
        int b = (int)(c1.getBlue() * ir + c2.getBlue() * ratio);
        return new Color(
            Math.min(255, Math.max(0, r)),
            Math.min(255, Math.max(0, g)),
            Math.min(255, Math.max(0, b))
        );
    }

    public static String generate(String title, String location, LocalDateTime dateTime,
                                   String creator, ArrayList<String> tags) {
        try {
            File dir = new File("posters");
            if (!dir.exists()) dir.mkdirs();

            BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Renk semasi sec
            Color[] scheme = SCHEMES[getSchemeIndex(tags)];
            Color textColor = scheme[4];

            // 4-koseli mesh gradient (gecisli renkler!)
            drawMeshGradient(g, scheme[0], scheme[1], scheme[2], scheme[3]);

            // Dekoratif parlak daireler (bokeh efekti)
            for (int i = 0; i < 12; i++) {
                int x = rand.nextInt(WIDTH);
                int y = rand.nextInt(HEIGHT);
                int size = 30 + rand.nextInt(100);
                float alpha = 0.03f + rand.nextFloat() * 0.06f;
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g.setColor(Color.WHITE);
                g.fillOval(x - size/2, y - size/2, size, size);
            }

            // Buyuk dekoratif daire sag ust
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.07f));
            g.setColor(Color.WHITE);
            g.fillOval(WIDTH - 180, -100, 380, 380);

            // Kucuk daire sol alt
            g.fillOval(-80, HEIGHT - 120, 250, 250);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // ====== UST KISIM ======

            // Emoji fontu (Mac icin Apple Color Emoji, diger icin fallback)
            Font emojiFont;
            try {
                emojiFont = new Font("Apple Color Emoji", Font.PLAIN, 40);
            } catch (Exception e) {
                emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 40);
            }

            // Buyuk emoji sag ust kose
            String emoji = getEmojiForTags(tags);
            g.setFont(emojiFont);
            g.drawString(emoji, WIDTH - 75, 55);

            // "BILKENT EVENT" etiketi
            g.setColor(new Color(255, 255, 255, 200));
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString("\u2605  BILKENT EVENT", 30, 32);

            // Ince parlak cizgi
            g.setColor(new Color(255, 255, 255, 50));
            g.fillRect(30, 42, WIDTH - 60, 1);

            // ====== BASLIK ======
            g.setColor(textColor);
            int fontSize = title.length() > 30 ? 30 : (title.length() > 20 ? 34 : 38);
            Font titleFont = new Font("SansSerif", Font.BOLD, fontSize);
            g.setFont(titleFont);
            FontMetrics fm = g.getFontMetrics();

            // Word wrap
            String[] words = title.split(" ");
            ArrayList<String> lines = new ArrayList<>();
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                String test = currentLine.length() == 0 ? word : currentLine + " " + word;
                if (fm.stringWidth(test) > WIDTH - 80) {
                    if (currentLine.length() > 0) lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(test);
                }
            }
            if (currentLine.length() > 0) lines.add(currentLine.toString());
            if (lines.size() > 3) lines = new ArrayList<>(lines.subList(0, 3));

            int titleY = 85;
            for (String line : lines) {
                // Golge
                g.setColor(new Color(0, 0, 0, 70));
                g.drawString(line, 32, titleY + 2);
                // Ana metin
                g.setColor(textColor);
                g.drawString(line, 30, titleY);
                titleY += fm.getHeight();
            }

            // ====== ALT BILGI ALANI ======
            int infoY = HEIGHT - 120;

            // Yari saydam kart
            g.setColor(new Color(0, 0, 0, 80));
            g.fillRoundRect(20, infoY - 5, WIDTH - 40, 110, 16, 16);

            // Ince parlak border
            g.setColor(new Color(255, 255, 255, 30));
            g.setStroke(new BasicStroke(1));
            g.drawRoundRect(20, infoY - 5, WIDTH - 40, 110, 16, 16);

            // Tarih emojili
            g.setColor(new Color(255, 255, 255, 230));
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            if (dateTime != null) {
                String dateStr = dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy  \u2022  HH:mm"));
                String dateEmoji = "\uD83D\uDCC5";  // calendar emoji
                try {
                    Font smallEmoji = new Font("Apple Color Emoji", Font.PLAIN, 15);
                    g.setFont(smallEmoji);
                    g.drawString(dateEmoji, 35, infoY + 18);
                } catch (Exception e) {}
                g.setFont(new Font("SansSerif", Font.BOLD, 15));
                g.drawString("  " + dateStr, 52, infoY + 18);
            }

            // Konum emojili
            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g.setColor(new Color(255, 255, 255, 200));
            if (location != null && !location.isEmpty()) {
                String locEmoji = "\uD83D\uDCCD";  // pin emoji
                try {
                    Font smallEmoji = new Font("Apple Color Emoji", Font.PLAIN, 13);
                    g.setFont(smallEmoji);
                    g.drawString(locEmoji, 35, infoY + 42);
                } catch (Exception e) {}
                g.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g.drawString("  " + location, 52, infoY + 42);
            }

            // Organizator
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(new Color(255, 255, 255, 150));
            g.drawString("\uD83D\uDC64  by @" + creator, 35, infoY + 65);

            // Tag'ler sag alt
            if (tags != null && !tags.isEmpty()) {
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                StringBuilder tagStr = new StringBuilder();
                for (int i = 0; i < Math.min(tags.size(), 3); i++) {
                    tagStr.append("#").append(tags.get(i)).append("  ");
                }
                int tagWidth = g.getFontMetrics().stringWidth(tagStr.toString());
                g.setColor(new Color(255, 255, 255, 130));
                g.drawString(tagStr.toString(), WIDTH - tagWidth - 35, infoY + 65);
            }

            // Kucuk tag emojileri (dekoratif, sag alt)
            if (tags != null && tags.size() > 1) {
                try {
                    Font tinyEmoji = new Font("Apple Color Emoji", Font.PLAIN, 16);
                    g.setFont(tinyEmoji);
                    g.setColor(new Color(255, 255, 255, 100));
                    // 2. ve 3. tag icin de emoji koy
                    for (int i = 1; i < Math.min(tags.size(), 3); i++) {
                        ArrayList<String> singleTag = new ArrayList<>();
                        singleTag.add(tags.get(i));
                        String tagEmoji = getEmojiForTags(singleTag);
                        g.drawString(tagEmoji, WIDTH - 40 - (i * 25), infoY + 85);
                    }
                } catch (Exception e) {}
            }

            // XP badge (parlak gold)
            GradientPaint goldGrad = new GradientPaint(WIDTH-85, 18, new Color(255,215,0),
                                                        WIDTH-30, 42, new Color(255,180,0));
            g.setPaint(goldGrad);
            g.fillRoundRect(WIDTH - 85, 18, 58, 26, 13, 13);
            // XP border
            g.setColor(new Color(255, 255, 255, 80));
            g.drawRoundRect(WIDTH - 85, 18, 58, 26, 13, 13);
            // XP text
            g.setColor(new Color(60, 40, 0));
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString("+XP", WIDTH - 73, 36);

            g.dispose();

            String filename = "posters/poster_" + System.currentTimeMillis() + ".png";
            ImageIO.write(img, "png", new File(filename));
            return filename;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateDefault(Event event) {
        return generate(event.getTitle(), event.getLocation(), event.getDateTime(),
                        event.getCreatorUsername(), event.getTags());
    }
}
