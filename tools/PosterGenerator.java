package tools;

import model.*;
import model.Event;
import model.Event;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class PosterGenerator {
    private static final int WIDTH=600,HEIGHT=340;
    private static final Random rand=new Random();
    private static final Color[] PASTELS={
        new Color(173,216,255),new Color(255,182,193),new Color(176,226,172),
        new Color(200,170,230),new Color(255,200,150),new Color(150,220,210),
        new Color(255,240,150),new Color(255,190,200),new Color(210,190,240),
        new Color(170,235,200),new Color(255,170,160),new Color(170,210,255)};

    private static int pickColor(ArrayList<String> tags){
        if(tags==null||tags.isEmpty())return rand.nextInt(PASTELS.length);
        String t=tags.get(0).toLowerCase();
        if(t.contains("software")||t.contains("ai")||t.contains("python"))return 0;
        if(t.contains("algorithm")||t.contains("cyber"))return 11;
        if(t.contains("music")||t.contains("concert"))return 3;
        if(t.contains("sport")||t.contains("fitness"))return 2;
        if(t.contains("art")||t.contains("photo"))return 7;
        if(t.contains("game"))return 4;
        if(t.contains("food")||t.contains("social"))return 5;
        if(t.contains("environment"))return 9;
        if(t.contains("cinema")||t.contains("theater"))return 10;
        if(t.contains("education")||t.contains("workshop"))return 8;
        return rand.nextInt(PASTELS.length);
    }

    public static String generate(String title,ArrayList<String> tags,String dateStr,String location,int xp){
        BufferedImage img=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=(Graphics2D)img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg=PASTELS[pickColor(tags)];
        g.setColor(bg);
        g.fillRect(0,0,WIDTH,HEIGHT);
        g.setColor(new Color(255,255,255,40));
        g.fillOval(WIDTH-120,-30,200,200);
        g.fillOval(-50,HEIGHT-100,160,160);
        g.setColor(new Color(255,255,255,25));
        g.fillOval(WIDTH/2-60,HEIGHT/2-60,120,120);
        g.dispose();
        try{
            File dir=new File("posters");dir.mkdirs();
            String fname="posters/poster_"+System.currentTimeMillis()+"_"+rand.nextInt(9999)+".png";
            javax.imageio.ImageIO.write(img,"png",new File(fname));
            return fname;
        }catch(Exception e){return null;}
    }

    public static String generateDefault(Event ev){
        return generate(ev.getTitle(),ev.getTags(),ev.getDateStr(),ev.getLocation(),ev.getXpReward());
    }
}
