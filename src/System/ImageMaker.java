package System;
/*
 * ImageMaker.java
 *
 * Created on April 21, 2008, 11:16 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Josiah Hester, September 11, 2007
 * http://www.javalobby.org/articles/ultimate-image/
 */
public class ImageMaker {


    public static BufferedImage createImage() {
        BufferedImage img = null;

        try {
            img = ImageIO.read(new File(ImageMaker.class.getResource("../images/background.png").toURI()));
        } catch (Exception e) {
            img = new BufferedImage(Model.imageWidth, Model.imageHeight, BufferedImage.TYPE_INT_RGB);
        }
        img.createGraphics();
        Graphics2D g = (Graphics2D) img.getGraphics();
        fixColors(g);
        return img;
    }
    
    private static void fixColors(Graphics2D g) {
        
    }
    
    
    public static BufferedImage loadImage(String ref) {
        BufferedImage bimg = null;
        try {
            bimg = ImageIO.read(new File(ref));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bimg;
    }

}
