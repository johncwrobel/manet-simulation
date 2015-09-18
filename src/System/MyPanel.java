/**
 * A class providing the storage for a basic face
 *
 * @author David M. Smith
 */
package System;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.*;
import javax.swing.JPanel;

/**
 * display panel for the OTSP solution
 */
public class MyPanel extends JPanel implements Runnable,
        MouseListener,
        MouseMotionListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /*	*/
    /**
     * the screen size
     *//*
     private Dimension screenSize;
     */ /**
     * whether the solution is running
     */
    public boolean isRunning;
    /**
     * the solution
     */
    private Model myModel;
    Image image = null;

//------------------------------------------------------------------
//
    /**
     * basic constructor
     *
     * @param d the screen size
     * @param c the solution
     */
    public MyPanel(Dimension d, Model c) {
//        screenSize = d;
        myModel = c;
        addMouseListener(this);
        addMouseMotionListener(this);
        image = ImageMaker.createImage();
        /*
         * alternative if you want to read in an image
         * from the resource subdirectory
         */
//        Class metaObject = this.getClass();
//        URL url = metaObject.getResource("/OTSP/resource/Watermark.gif");
//        if(url != null) {
//            ImageIcon imageIcon = new ImageIcon(url);
//            image = imageIcon.getImage();
//        }
    }

    /**
     * stop the animation.
     */
    public void stop() {
        isRunning = false;
    }

    /**
     * run the animation.
     */
    @Override
    public void run() {
        do {
            myModel.doTimer();
            repaint();
            try {
                Thread.sleep((int) (1000 * Model.DT));
            } // millisecs
            catch (InterruptedException e) {
            }
        } while (isRunning);
    }

    /**
     * repaint the display
     */
    /**
     * blank fill the screen then paint it
     *
     * @param g the graphic environment
     */
    @Override
    public void paintComponent(Graphics g) {

        Dimension d = getSize();
        int imTop = 0;
        int imLeft = 0;
        int imHt = getHeight();
        int imWidth = getWidth();
        int dh, dw;
        double screenAspect = ((double) d.height) / d.width;
        if (screenAspect > Model.imageAspect) {
            // image is too short - fill in top and bottom
            dw = 0;
            imHt = (int) (d.width * Model.imageAspect);
            dh = d.height - imHt;
            imTop += dh / 2;
        } else {
            // image is too tall - fill in left and right
            dh = 0;
            imWidth = (int) (d.height / Model.imageAspect);
            dw = d.width - imWidth;
            imLeft += dw / 2;
        }
        g.drawImage(image, imLeft, imTop, imWidth, imHt, this);
        if (dw > 0) {
            g.setColor(Model.backColor);
            g.fillRect(0, 0, dw / 2, imHt);
            g.fillRect(dw / 2 + imWidth, 0, dw / 2, imHt);
        } else if (dh > 0) {
            g.setColor(Model.backColor);
            g.fillRect(0, 0, imWidth, dh / 2);
            g.fillRect(0, dh / 2 + imHt, imWidth, dh / 2);
        }
        myModel.doPaint(g, d);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        myModel.clicked(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        myModel.entered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        myModel.exited(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        myModel.released(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        myModel.pressed(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        myModel.dragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        myModel.moved(e);
    }
}
