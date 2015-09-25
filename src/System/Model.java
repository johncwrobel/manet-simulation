/**
 * A class providing the storage for the OTSP solution
 */
package System;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.ButtonGroup;
import java.io.*;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * an essentially static class containing the model
 */
/**
 *
 * @author dsmith Major Upgrade: need to show flow while network is in motion
 * Each device may be on more than one network For each of those networks, it
 * has a route to send messages Establish the concept of taking turns - when
 * network created, head will transmit - transmit authority moves round the
 * network ** working with timer moving the send authority
 *
 * -
 *
 */
public class Model {

    /*
     * Generic data Storage
     */
    /**
     * Title of the main window *
     */
    public static String title = "Network Traffic Flow";
    /**
     * Labels on the custom buttons *
     */
    public static String Labels[] = {"Reset", "Go", " Add", "Radius",
        "Details", "Flow", "Speed", "Preset", "Print Info"};
    // typedefs for buttons
    private static final int RESET = 0;
    private static final int GO = 1;
    private static final int ADD = 2;
    private static final int RADIUS = 3;
    private static final int DETAILS = 4;
    private static final int FLOW = 5;
    private static final int SET_SPEED = 6;
    private static final int PRESET = 7;
    private static final int PRINT_NODE_INFO = 8;
    /**
     * want to include copyright?
     */
    public static boolean useCopyright = true;
    public static String Buttons[] = {};
    public static ButtonGroup butGroup = null;
    /**
     * screen size
     */
    public static Dimension screenSize;
    /**
     * repainting the screen
     */
    public static boolean doRepaint;
    /**
     * enable the mouse
     */
    public static boolean mouseEnabled = false;
    /**
     * painting the screen to protect the data while painting
     */
    private static boolean painting;
    /**
     * the host panel
     */
    public static MyPanel myPanel;
    /**
     * diagnostic print stream
     */
    public static PrintStream debug;
    /**
     * buffered input stream
     */
    public static BufferedReader input = null;
    /**
     * diagnostic print stream enabler
     */
    public static boolean initialized = false;
    /**
     * display initialized? set by first call to dopaint
     */
    public static boolean doDbg = true;
    public static boolean doDbg1 = true;
    /**
     * time tick
     */
    public static double DT = 0.03333;
    public static int t;
    /**
     * Model Application specific data storage
     */
    /**
     * enable the mouse for network selection
     */
    public static boolean detailsEnabled = false;
    /**
     * output print stream enabler
     */
    public static boolean doOut = false;
    /**
     * enable display of flow
     */
    public static boolean doFlow = false;
    /**
     * enable dropping a device
     */
    public static boolean doDrop = false;
    /**
     * enable a device rejoining
     */
    public static boolean doReJoin = false;
    /**
     * the ratio of leaving to entering a network
     */
    private static boolean moveThem;
    public static Color stnColor[] = null;
    public static int radius;
    public static final int PORTAL_NET_CAPACITY = 2;
    private static final double MINSPEED = 0.2;
    private static int INITIAL_RADIUS = 2000;
    /**
     * the array of all nodes
     */
    public static Node node[];
    /**
     * the content of each node network
     */
    public static ArrayList<SubNet> subNets;
    /**
     * the max radius of a node network
     */
    public static int activeNetworks;
    /**
     * the max radius of a node network
     */
    public static double radiusSq;
    /**
     * the max radius to leave a node network
     */
    public static double remRadiusSq;
    /**
     * the max radius to merge a node pair
     */
    public static double dRadiusSq;
    /**
     * the ratio of leave distance to join distance
     */
    public static double remFactor = 1.33;
    /**
     * the edge of the window
     */
    public static final int EDGE = 20;
    /**
     * the maximum node velocity
     */
    public static double MAXV = 0.05;
    /**
     * the trunk location
     */
    public static double trunkX = 0;
    /**
     * the trunk location
     */
    public static double trunkY = 0;
    /**
     * enable path checking
     */
    public static boolean checkPath = true;
    /**
     * enable network linking
     */
    public static int networksThreshold;
    /**
     * route count
     */
    public static int routeCount;
    /**
     * route count
     */
    public static int showRouteCount = 0;
    /* ATTS operational mode - 0 = no rev; 1 = rev */
    public static double minSpeed, maxSpeed;
    public static boolean checkDetails = false;

    // background info
    public final static Color backColor = new Color(188, 230, 176);
    public final static int imageWidth = 1200;
    public final static int imageHeight = 900;
    public static double imageAspect = ((double) imageHeight) / imageWidth;

    private static boolean showBox = true;

    /**
     * the maximum node velocity
     */
    public static final double SPEED = 0.015;   // **************************
    public static final double RADIAL_SPEED = 6;
    public static double radialSpeed;
    public static double speed;

    public static final int COLORS = 64;

    public enum State {

        WAIT, FIND_ENDS, FIND_ROUTE, WAIT_FOR_HIT, RUN, FINISHED
    };
    public static Node startNode = null;
    public static Node endNode = null;
    public static int cornerX, cornerY;
    public static int routeFrom = -1;
    public static int routeTo = -1;
    private static Path thePath = null;

    /**
     * basic constructor
     *
     * @param d screen size
     */
    public Model(Dimension d) {
        screenSize = d;
        initialized = false;
        reset();
    }

    /**
     * initialize Global data System reset called at startup
     *
     * @param isStandalone - true if running alone, false if an applet that
     * can't write to files
     */
    public static void init(boolean isStandalone) {
        if (isStandalone) {
            try {
                debug = new PrintStream(new FileOutputStream("debug.log"));
            } catch (IOException e) {
                if (doDbg) {
                    Model.debug.println("Error " + e + " opening debug.log");
                }
            }
            debug.println("debug log opened");
            doDbg = false; // initial debug setting
            doDbg1 = true; // initial debug setting
        } else {
            doDbg = false;
        }
        reset();
    }

    /**
     * Model reset called by the user
     */
    private static void reset() {
        DT = 0.0333333333333;
        doFlow = false;
        doRepaint = false;
        checkPath = true;
        painting = false;
        moveThem = false;
        radius = INITIAL_RADIUS;
        setRadii(radius);
        subNets = new ArrayList<SubNet>();
        activeNetworks = 0;
        node = new Node[0];
        networksThreshold = 15;
        stnColor = new Color[COLORS];
        int val = 21;
        int intensity = 60;
        stnColor[0] = new Color(0, 0, 190);
        stnColor[1] = new Color(0, 190, 0);
        stnColor[2] = new Color(0, 190, 190);
        stnColor[3] = new Color(190, 0, 190);
        stnColor[4] = new Color(190, 190, 0);

        for (int i = 5; i < COLORS; i++) {
            int rv = (val & 3) * intensity;
            int gv = ((val & 0xc) >> 2) * intensity;
            int bv = ((val & 0x30) >> 4) * intensity;
            stnColor[i] = new Color(rv, gv, bv);
            int c6 = val & 1;
            int c1 = (val & 0x20) >> 5;
            int next = ((c6 + c1) & 1) << 6;
            val = (val + next) >> 1;
        }
        Path.setState(State.WAIT, "reset");
        cornerX = 0;
        cornerY = 0;
        radialSpeed = RADIAL_SPEED;
        speed = SPEED;
        doDrop = false;
        doReJoin = false;
        thePath = null;
        Path.setBall(-1);
        //		debug.println("System reset\n");
    }

    int lookAt = 0;
    boolean doSoloCheck = false;
    boolean timerWorking = false;

    /**
     * hit here each clock tick
     */
    public void doTimer() {
        if (!timerWorking) {
            timerWorking = true;
            t++;
            if (t % 5 == 0) {
                showBox = !showBox;
            }
            if (!painting) {
                doRepaint = true;
            }
            if (butGroup != null) {
            }
            if (node.length > 0) {
                MAXV = 30.0 / (node.length * node.length);
                if (MAXV < MINSPEED) {
                    MAXV = MINSPEED;
                }
                if (moveThem && !painting) {
                    if (doSoloCheck && node.length > 0) {
                        if (++lookAt >= node.length) {
                            lookAt = 0;
                        }
                        Node n = node[lookAt];
                        n.checkSolo();
                        doSoloCheck = false;
                    }
                    for (int i = 0; i < node.length; i++) {
                        node[i].update();
                    }
                    if (doFlow) {
                        if (Path.getState() == State.WAIT) {
                            Path.setState(State.FIND_ENDS, "Start flow");
                        }
                    }
                    for (SubNet it : subNets) {
                        it.update();
                    }
                    if (doFlow) {
                        if (thePath == null) {
                            thePath = new Path();
                        }
                        thePath.update();
                    }
                    checkNetworks();
                    if (t % 3 == 2) {
                        doSoloCheck = true;
                    }
                }
            }
        }
        timerWorking = false;
    }

    /**
     * respond to custom button press
     *
     * @param code the number of the button pressed (0 ... (Labels.length-1) )
     */
    private static int X_Value[] = {150, 250, 350, 450, 550, 650, 650, 550, 450, 350, 250, 150};
    private static int Y_Value[] = {350, 350, 350, 350, 350, 350, 450, 450, 450, 450, 450, 450};
    private static int AX_Value[] = {150, 250, 350, 450, 550,
        200, 64, 186, 240, 716, 472, 628, 124};
    private static int AY_Value[] = {225, 125, 25, 125, 225,
        157, 155, 186, 263, 245, 431, 179, 0};
//                                     12    13   14   15   16   17   18   19   20   21
//    private static int AX_Value[] = {150, 250, 350, 450, 550, 730, 682, 399, 344, 596,
//         22   23   24    25   26   27   28   29   30   31   32   33 
//        741, 637, 200,   64, 382, 749, 186, 240, 716, 472, 628, 124};
//    private static int AY_Value[] = {225, 125, 25, 125, 225, 319, 25, 328, 332, 124,
//        252, 102, 157, 155, 372, 339, 186, 263, 245, 431, 179, 0};

    private static int addedThis = 0;
    private static boolean fixedAdds = true;

    public void doButton(int code) {
        String numStr;
        int repeat = 0;
        Node it = null;
        switch (code) {
            case PRESET: // preset multiple sub-nets
                reset();
                activeNetworks = networksThreshold + 1;
                repeat = X_Value.length;
                radius = 350;
                setRadii(radius);
                for (int i = 0; i < repeat; i++) {
                    it = new Node(X_Value[i], Y_Value[i]);
                    if (Model.doDbg) {
                        debug.println(" 0. doButton:: added " + it);
                    }
                    insertNode(it, true);
                    showNets("PRESET");
                }
                speed = 0;
                validateNets();
                checkAllNetworks();
                showNodes("preset", false);
                break;
            case RESET: // reset the members
                reset();
                numStr = JOptionPane.showInputDialog("Number of Nodes: ");
                activeNetworks = networksThreshold + 1;
                if (numStr != null) {
                    repeat = Integer.parseInt(numStr);
                } else {
                    repeat = 1;
                }
                for (int i = 0; i < repeat; i++) {
                    it = randomPoint();
                    if (Model.doDbg) {
                        debug.println(" 0. doButton:: added " + it);
                    }
                    insertNode(it, true);
                    showNets("RESET");
                }
                checkAllNetworks();
                if (doDbg) {
                    showNodes("reset", false);
                }
                break;
            case GO: // Go
                moveThem = true;
                break;
            case ADD: // add a node
                doAdd();
                break;
            case RADIUS: // radius
                String ini = "" + radius;
                String s = JOptionPane.showInputDialog("Enter radius: ", ini);
                try {
                    int wl = Integer.parseInt(s);
                    radius = wl;
                    setRadii(radius);
                } catch (Exception e) {
                }
                Path.setState(State.WAIT, "Set radius to " + radius);
                cornerX = 0;
                cornerY = 0;
                if (doDbg) {
                    debug.println(">>>Set radius to " + radius);
                }
                Node.clearAll(true);
                break;
            case DETAILS: // Details Pick
                detailsEnabled = true;
                break;
            case FLOW: // Show message flow
                doFlow = !doFlow;
                Path.setState(State.WAIT, "Start flow");
                cornerX = 0;
                cornerY = 0;
                for (int i = 0; i < node.length; i++) {
                    Node nd = node[i];
                    nd.mark(Node.Mode.NONE);
                    Path.setBall(-1);
                }
                break;
            case SET_SPEED:
                String fst = "" + radialSpeed;
                String st = JOptionPane.showInputDialog("Enter Message speed: ",
                        fst);
                try {
                    double wl = Double.parseDouble(st);
                    radialSpeed = wl;
                } catch (Exception e) {
                }
                break;

            case PRINT_NODE_INFO:
                //doTimer();
                for (int i = 0; i < node.length; i++) {
                    System.out.println("Node info: ");
                    System.out.println(node[i].toString());
                    System.out.println(node[i].status[0].toString());
                    System.out.println(node[i].status[1].toString());
                }
                break;
        }
//		debug.println("dobutton(" + code + ")");
    }

    private void doAdd() {
        Node it = null;
        if (fixedAdds && addedThis < AX_Value.length) { //the preset button was pressed so we fix the positions.
            it = new Node(AX_Value[addedThis], AY_Value[addedThis]);
            addedThis++;
        } else {
            it = randomPoint(); //preset wasn't pressed so we just place at random point.
        }
        if (Model.doDbg) {
            debug.println("doButton:: adding " + it);
            debug.flush();
        }
        insertNode(it, true);
        if (Model.doDbg) {
            debug.println("doButton:: added " + it);
            debug.flush();
        }
        if (doDbg) {
            showNodes("add", false);
        }
    }

    public static void validateNets() {
        if (doDbg) {
            try {
                for (SubNet net : subNets) {
                    net.validate();
                }
            } catch (ConcurrentModificationException e) {
                debug.println(e);
                debug.flush();
            }
        }
    }

    public static void showNets(String title) {
        if (Model.doDbg) {
            debug.println("showNets:: " + title);
            int n = subNets.size();
            for (int i = 0; i < n; i++) {
                SubNet it = subNets.get(i);
                debug.println("     Subnet " + i + ": " + it);
            }
        }
    }

    public static void setRadii(int r) {
        radiusSq = r * r;
        remRadiusSq = r * remFactor * r * remFactor;
//        dRadiusSq = r * (remFactor - 1) * r * (remFactor - 1) / 8;
    }

    /**
     * insert a station
     *
     * @param st the station
     */
    public static void insertNode(Node nd, boolean newID) {
        int ist = node.length;
        if (newID) { //hardcoded to be true when we call doAdd()
            //Kevin's Add
            if (ist == 0) {
                nd.setObserverNode(true);
            }
            //End Kevin's Add
            nd.ID = ist; //set the id to its correponding index
            node = bumpNodes(node, nd);
        }
        findSubNets(nd, "insertNode:: ");
    }

    public static void findSubNets(Node nd, String title) {
        boolean done = false;
        SubNet net = null; //initialize a subnet
        //if node alredy exists within a subnet then we are done
        for (int i = 0; i < subNets.size(); i++) {
            net = (SubNet) subNets.get(i);
            if (nd.isIn(net)) {
                done = true;
            }
        }
        if (!done) {
            if (nd.status[0].net == null) {
                done = putNodeInNet(nd, 0);
            }
            if (!done && nd.status[1].net == null) {
                done = putNodeInNet(nd, 1);
            }
        }
        if (!done) {
            net = nd.findANetwork();
        }
        checkAllNetworks();
    }

    public static boolean putNodeInNet(Node nd, int ndx) {
        boolean done = false;
        SubNet notNet = nd.status[1 - ndx].net;
        for (int i = 0; !done && i < subNets.size(); i++) {
            SubNet net = (SubNet) subNets.get(i);
            if (net != null && net != notNet && net.belongs(nd, radiusSq)) {
                net.insertNode(nd, ndx);
                nd.status[ndx].net = net;
                done = true;
            }
        }
        return done;
    }

    private static void checkAllNetworks() {
        boolean doOutput = false;
        SubNet net;
        for (int nt = 0; !doOutput && nt < subNets.size(); nt++) {
            net = subNets.get(nt);
            if (net.changed) {
                doOutput = true;
            }
        }
        if (doOutput) {
            for (int nt = 0; nt < subNets.size(); nt++) {
                net = subNets.get(nt);
                if (Model.doDbg) {
                    debug.println(" 2. checkAllNetworks:: " + net + " first " + net.first());
                }
                net.changed = false;
            }
            for (int id = 0; id < node.length; id++) {
                Node nd = node[id];
                if (Model.doDbg) {
                    debug.println(" 3. checkAllNetworks:: " + nd);
                }
                for (int sn = 0; sn < Model.PORTAL_NET_CAPACITY; sn++) {
                    if (Model.doDbg) {
                        debug.println("   --   " + nd.status[sn]);
                    }
                }
            }
        }
    }

    /**
     * highlight the network closest to [x, y]
     *
     * @param x x
     * @param y y
     */
    public void highlightNetwork(int x, int y) {
        SubNet n = findClosestNet(x, y);
        n.setDetails(90);
    }

    /**
     * generate a station with random location
     *
     * @return the station
     */
    public static Node randomPoint() {
        int x = (int) (Math.random() * screenSize.width);
        int y = (int) (Math.random() * (screenSize.height - 100));
        return new Node(x, y);
    }

    /**
     * compute the path lengths of all networks
     *
     * @return the length
     */
    public int computeAllDistances() {
        int sum = 0;
        for (int i = 0; i < subNets.size(); i++) {
            SubNet net = (SubNet) subNets.get(i);
            sum += net.members.computeDistance();
        }
        return sum;
    }

    private void drawGrid(Graphics g, Dimension d) {
        int w = d.width;
        int h = d.height;
        g.setColor(Color.GRAY);
        for (int x = 0; x < w; x += 100) {
            g.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 100) {
            g.drawLine(0, y, w, y);
        }
        g.drawString("(" + w + "*" + h + ")", 0, 0);
    }

    /**
     * paint the screen
     *
     * @param g the graphic environment
     * @param d the screen size
     */
    public void doPaint(Graphics g, Dimension d) {
        screenSize = d;
        int bot = screenSize.height - 60;
        int mid = screenSize.width / 2;
        routeCount = 0;
        painting = true;
        drawGrid(g, d);
        // make sure the current origin and local destination are on the 
        // network that is drawing circles
        //       try {
        if (moveThem && doRepaint && node.length > 0) {
            activeNetworks = subNets.size();
            for (SubNet n : subNets) {
                if (n.length() == 0) {
                    activeNetworks--;
                }
            }
            paintText(g, Color.black, "" + subNets.size() + " networks ( "
                    + activeNetworks + " active); doFlow: " + doFlow, 20, bot + 40);
            paintText(g, Color.black, "Mode: " + Path.getState()
                    + " from " + routeFrom + " to " + routeTo, 20, bot + 10);
            for (int ic = subNets.size() - 1; ic >= 0; ic--) {
                SubNet net = subNets.get(ic);
                if (net != null) {
                    if (net.details > 0) {
                        net.drawChain(Color.red, 0, g);
                        net.details--;
                    } else {
                        net.drawChain(stnColor[ic % COLORS], 0, g);
                    }
                }
            }
        }
        if (node.length > 0) {
            int i = 0;
            for (Node nd : node) {
                int id = nd.ID;
                nd.doPaint(g);
            }
            paintText(g, Color.black, "" + node.length + " total nodes", 20, bot + 25);
            if (t % (int) (1 / DT) == 0) {
                showRouteCount = routeCount;
            }
            if (showRouteCount > 0) {
                g.drawString(
                        "" + showRouteCount + " active message routes", 20,
                        bot + 55);
            }
        }
        if (showBox && !moveThem) {
            g.setColor(Color.green);
            g.fillRect(mid - 120, bot + 50, 50, 5);
        }
//        } catch (Exception e) {
//        }
        doRepaint = false;
        painting = false;
    }

    public static void paintText(Graphics g, Color clr, String str, int x, int y) {
        g.setColor(clr);
        g.drawString(str, x, y);
    }

    /**
     * Mouse clicked
     *
     * @param e
     */
    public void clicked(MouseEvent e) {
        System.out.println("Mouse Clicked");
    }

    /**
     * Mouse entered
     *
     * @param e
     */
    public void entered(MouseEvent e) {
        // System.out.println("Mouse Entered");
    }

    /**
     * Mouse exited
     *
     * @param e
     */
    public void exited(MouseEvent e) {
        // System.out.println("Mouse Exited");
    }

    /**
     * Mouse released
     *
     * @param e
     */
    public void released(MouseEvent e) {
        System.out.println("Mouse Released");
        if (doDrop || doReJoin) {
            int ndx = findClosest(e.getX(), e.getY());
            Node nd = node[ndx];
            if (doDrop) {
                // traverse all its active networks and drop it from each
                for (int i = 0; i < subNets.size(); i++) {
                    SubNet net = subNets.get(i);
                    net.removeNode(ndx);
                    nd.dropFromNet(net);
                }
                if (doDbg) {
                    Model.debug.println(" 4. released::  Deleting node " + ndx);
                }
                doDrop = false;
            } else {
                insertNode(nd, false);
                if (doDbg) {
                    Model.debug.println(" 5. released:: Node " + ndx + " rejoining");
                }
                doReJoin = false;
            }
        } else if (detailsEnabled) {
            highlightNetwork(e.getX(), e.getY());
        }
    }

    /**
     * Mouse pressed
     *
     * @param e
     */
    public void pressed(MouseEvent e) {
        System.out.println("Mouse Pressed");
    }

    /**
     * Mouse dragged
     *
     * @param e
     */
    public void dragged(MouseEvent e) {
        System.out.println("Mouse Dragged");
    }

    /**
     * Mouse moved
     *
     * @param e
     */
    public void moved(MouseEvent e) {
        // System.out.println("Mouse Moved");
    }

    /**
     * compute a random delta between two limits
     *
     * @param inner - the lower ring radius
     * @param outer - the upper ring radius
     * @return yes or no
     */
    public static boolean someLost() {
        boolean res = false;
        for (int i = 0; i < node.length && !res; i++) {
            Node nd = node[i];
            res = nd.status[0].host == null
                    && nd.status[1].host == null;
        }
        return res;
    }

    public static int randBand(double inner, double outer) {
        double d = outer - inner;
        if (d <= 0) {
            d = inner / 2;
        }
        int shot = (int) ((Math.random() - 0.5) * d / 10.0);
        if (shot < 0) {
            shot = -shot;
        }
        // shot = 0; // DMS 8/28/09
        return shot;
    }

    private static void checkNetworks() {  // run from the timer
        for (Node nd : node) {
            SubNet A = nd.status[0].net;
            SubNet B = nd.status[1].net;
            if (A != null && B != null) {
                check(A, B);
                if(B.length() > 0)
                    check(B, A);
            }
        }
    }

    private static void check(SubNet A, SubNet B) {
        // if all of A are in B, delete A
        boolean useless = A.containedIn(B);
        if (doDbg) {
            debug.println("is " + A.ID + " in " + B.ID
                    + " -> " + useless);
        }
        if (useless) {
            deleteSubNet(A);
            if (doDbg) {
                debug.println("deleted " + A);
            }
        } else {
            // true if all of B belongs in A
            boolean mergeThem = A.allBelongsIn(B, remRadiusSq);
            if (doDbg) {
                debug.println("does " + A.ID + " belong in " + B.ID
                        + " -> " + mergeThem);
            }
            if (mergeThem) {
                if (doDbg) {
                    debug.println("merging " + A.ID + " and " + B.ID);
                }
                mergeNetworks(A, B);
            }
        }
    }


    /**
     * delete network a
     *
     * @param A a Subnet
     */
    private static void deleteSubNet(SubNet A) {
        A.nullify();
        for (Node n : node) {
            for (int i = 0; i < PORTAL_NET_CAPACITY; i++) {
                if (n.status[i].net == A) {
                    n.status[i].net = null;
                }
            }
        }
    }

    /**
     * merge network b into network a
     *
     * @param ia index of a
     * @param ib index of b
     */
    public static void mergeNetworks(SubNet A, SubNet B) {
        A.changed = true;
        B.changed = true;
        Members bl = B.members;
        // add to A all nodes from B not already in A
        // and remove them all from B
        Node here = bl.first;
        int bln = bl.length();
        Model.validateNets();
        for (int ih = 0; ih < bln; ih++) {
            int id = here.ID;
            Node next = here.getPrev(B);
            if (!A.isin(id)) {
                B.removeNode(id);
                int emptied = here.dropFromNet(B);
                A.insertNode(here, emptied);
                if (Model.doDbg) {
                    debug.println(" 7.   -- added " + id + " to " + A);
                }
            } else {
                B.removeNode(id);
                here.dropFromNet(B);
            }
            if (Model.doDbg) {
                debug.println(" 8.  -- removed " + id + " from " + B);
            }
            here = next;
        }
        // then nullify b;
        B.nullify();
        Model.validateNets();
    }

    public static int inPtDistance(int id, int px, int py) {
        int res = Integer.MAX_VALUE;
        if (node[id].status[0].host != null
                || node[id].status[1].host != null) {
            res = ptDistance(id, px, py);
        }
        return res;
    }

    /**
     * distance of a node from an x-y location
     *
     * @param id the node index
     * @param px x
     * @param py y
     * @return distance
     */
    public static int ptDistance(int id, int px, int py) {
        Node s = node[id];
        double x = s.px - px;
        double y = s.py - py;
        return (int) Math.sqrt(x * x + y * y);
    }

    /**
     * distance of a node from an x-y location
     *
     * @param id the node index
     * @param px x
     * @param py y
     * @return distance
     */
    public static int ntDistance(int id, int px, int py) {
        SubNet n = (SubNet) subNets.get(id);
        double x = (n.cx - px);
        double y = (n.cy - py);
        int res = (int) Math.sqrt(x * x + y * y);
        return res;
    }

    /**
     * find the node closest to an x-y location
     *
     * @param x x
     * @param y y
     * @return node index
     */
    public static int findClosest(int x, int y) {
        // initialize distance
        double dist = ptDistance(0, x, y);
        int res = 0;
        for (int i = 1; i < node.length; i++) {
            double d = ptDistance(i, x, y);
            if (d < dist) {
                dist = d;
                res = i;
            }
        }
        return res;
    }

    /**
     * find the network closest to an x-y location
     *
     * @param x x
     * @param y y
     * @return node index
     */
    public static SubNet findClosestNet(int x, int y) {
        // initialize distance
        int dist = ntDistance(0, x, y);
        int res = 0;
        for (int i = 1; i < subNets.size(); i++) {
            int d = ntDistance(i, x, y);
            if (d < dist) {
                dist = d;
                res = i;
            }
        }
        return subNets.get(res);
    }

    /**
     * find the distance between two nodes
     *
     * @param from one of them
     * @param to the other
     * @param showit display the result?
     * @return the distance
     */
    public static int nodeDistance(int from, int to, boolean showit) {
        int res = Integer.MAX_VALUE;
        if (from < node.length && to < node.length) {
            double x = (node[from].fx - node[to].fx);
            double y = (node[from].fy - node[to].fy);
            res = (int) Math.sqrt(x * x + y * y);
            // if (showit && doDbg) {
            // debug.println("9. nodeDistance( " + from + ", " + to + ") -> " +
            // res);
            // }
        }
        return res;
    }

    /**
     * make a new station with a node added
     *
     * @param orig original station
     * @param toAdd node to add
     * @return new station
     */
    public static Node[] bumpNodes(Node[] orig, Node toAdd) {
        // if (doDbg) {
        // debug.println("11. bumpNodes:: " + toAdd);
        // }
        Node res[] = new Node[orig.length + 1]; //copy over old array and then add new node to end of array
        for (int i = 0; i < orig.length; i++) {
            res[i] = orig[i];
        }
        res[orig.length] = toAdd;
        return res;
    }

    /**
     * print out all the nodes
     *
     * @param txt header text
     */
    public static void showNodes(String txt, boolean override) {
        if (override || doDbg) {
            String res = txt + "; has ball: " + Path.getBall();
            for (Node nd : node) {
                res += "\n" + nd;
            }
            debug.println("showNodes:: " + res);
        }
    }
}
