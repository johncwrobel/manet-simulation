/*
 * new generic model wrapper
 */
package Viewer;

/**
 * A class providing a simple graphics shell
 * @author David M. Smith
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import System.*;

/**
 * standard Graphics application
 */
public class GraphicApplet extends JApplet {

    private Thread mapThread;
    private Model myModel;
    private boolean isStandalone = false;
    public JRadioButton button[];

    /**
     *   basic constructor
     */
    @Override
    public void init() {
        MouseEventHandler eh;
        JButton b;

        Model.init(isStandalone);
        JPanel panel = new JPanel();
        JPanel notice = null;
        JPanel radioPanel = null;
        panel.setLayout(new GridLayout(2, 1));
        if (Model.useCopyright) {
            notice = new JPanel();
            notice.setLayout(new GridLayout(3, 1));
            Font tiny = new Font("SansSerif", 0, 8);
            JLabel top = new JLabel("(c)Copyright 2013 - 2014 David M. Smith");
            top.setFont(tiny);
            JLabel mid = new JLabel("          Unpublished Work");
            mid.setFont(tiny);
            JLabel bot = new JLabel("         All Rights Reserved");
            bot.setFont(tiny);
            notice.add(top);
            notice.add(mid);
            notice.add(bot);
        }
        JPanel stpanel = new JPanel();
        stpanel.setLayout(new FlowLayout());
        int Nb = Model.Buttons.length;
        if (Nb > 0) {
            button = new JRadioButton[Nb];
            //Create the radio buttons.
            for (int bt = 0; bt < Nb; bt++) {
                button[bt] = new JRadioButton(Model.Buttons[bt]);
                button[bt].setMnemonic(KeyEvent.VK_0 + bt);
                button[bt].setActionCommand(Model.Buttons[bt]);
                if (bt == 0) {
                    button[0].setSelected(true);
                }
            }
            //Group the radio buttons.
            Model.butGroup = new ButtonGroup();
            for (int bt = 0; bt < Nb; bt++) {
                Model.butGroup.add(button[bt]);
            }
            //Put the radio buttons in a panel.
            radioPanel = new JPanel(new FlowLayout());
            for (int bt = 0; bt < Nb; bt++) {
                radioPanel.add(button[bt]);
            }

        }
        b = new JButton("Start");
        eh = new MouseEventHandler(this, b);
        b.addMouseListener(eh);
        stpanel.add(b);
        if (Model.useCopyright) {
            stpanel.add(notice);
        }
        b = new JButton("Stop");
        eh = new MouseEventHandler(this, b);
        b.addMouseListener(eh);
        stpanel.add(b);
        JPanel usrpanel = new JPanel();
        int nbts = Model.Labels.length;
        usrpanel.setLayout(new FlowLayout());
        for (int i = 0; i < nbts; i++) {
            JButton B = new JButton(Model.Labels[i]);
            eh = new MouseEventHandler(this, B);
            B.addMouseListener(eh);
            usrpanel.add(B);
            if(Nb > 0 && (i + 1) == (nbts/2)) {
                usrpanel.add(radioPanel);
                Nb = 0;
            }
        }
        panel.add(usrpanel);
        panel.add(stpanel);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        Dimension d = getSize();
        myModel = new Model(d);
        Model.myPanel = new MyPanel(d, myModel);
        cp.add(Model.myPanel, BorderLayout.CENTER);
        cp.add(panel, BorderLayout.SOUTH);
        setVisible(true);
    }

    /**
     *   Event handler for mouse clicks
     */
    class MouseEventHandler implements MouseListener {

        GraphicApplet theFrame;
        JButton button;

        public MouseEventHandler(GraphicApplet theFrame, JButton button) {
            this.theFrame = theFrame;
            this.button = button;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            String com = button.getText();
            if (com.equals("Start")) {
                if (!Model.myPanel.isRunning) {
                    mapThread = new Thread(Model.myPanel);
                    Model.myPanel.isRunning = true;
                    mapThread.start();
                }
            } else if (com.equals("Stop")) {
                Model.myPanel.stop();
            } else {
                int i = 0;
                for (; i < myModel.Labels.length; i++) {
                    if (com.equals(myModel.Labels[i])) {
                        myModel.doButton(i);
                        repaint();
                        break;
                    }

                }
                if (i == myModel.Labels.length) {
                    System.out.println("Erroneous Action");
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }
    }

    /**
     * main program
     * @param s who knows
     */
    public static void main(String[] s) {
        JFrame frame = new JFrame(Model.title);
        GraphicApplet app = new GraphicApplet();
        frame.add(app, BorderLayout.CENTER);
        app.isStandalone = true;
        app.init();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 700);
        frame.setLocation(10, 10);
        frame.setVisible(true);
    }
}  // GraphicApp

