/*
 * Node.java
 *
 * Created on July 6, 2004, 2:50 PM
 */
package System;

import java.awt.Color;
import java.awt.Graphics;

/**
 * storage for one station (node)
 *
 * @author dsmith
 */
public class Node {

    /**
     * identity (node index)
     */
    public int ID;
    /**
     * display x location
     */
    public int px;
    /**
     * display y location
     */
    public int py;
    /**
     * physical location
     */
    public double fx;
    /**
     * physical location
     */
    public double fy;
    /**
     * physical x velocity
     */
    private double vx;
    /**
     * physical y velocity
     */
    private double vy;

    private boolean isMainHub;

    public int fromHereTo;
    public int shouldGoTo;

    public enum Mode {

        NONE, START, END, GO_TO, GO_FROM
    };
    public Mode marked;
    private double dvx = 0;
    private double dvy = 0;
    /**
     * network membership
     */
    public NetStatus status[];

    /**
     * default constructor
     */
    public Node() {
        this(0, 0, 0, 0);
        ID = -1;
    }

    /**
     * Creates a new instance of Station
     *
     * @param ix x value
     * @param iy y value
     */
    public Node(int ix, int iy) {
        this(ix, iy, 0, 0);
        ID = -1;
    }

    /**
     * Creates a new instance of Node
     *
     * @param ix x value
     * @param iy y value
     * @param fvx x velocity
     * @param fvy y velocity
     */
    public Node(int ix, int iy, double xv, double yv) {
        this.px = ix;
        this.py = iy;
        fx = ix;
        fy = iy;
        this.vx = xv;
        this.vy = yv;
        marked = Mode.NONE;
        status = new NetStatus[2];
        status[0] = new NetStatus();
        status[1] = new NetStatus();
        fromHereTo = -1;
        shouldGoTo = -1;
        isMainHub = false;
    }

    /**
     *
     * @param net - one of the nets to which this device belongs
     */
    public void startSending(SubNet net, String title, Node trig) {
        int ch = findChannel(net, false);
        status[ch].hasToken = true;
        status[ch].sending = true;
        status[ch].radius = -Model.radialSpeed;
        status[ch].cclx = px;
        status[ch].ccly = py;
        if (trig != null) {
            ch = trig.findChannel(net, false);
            trig.status[ch].hasToken = false;
        }
        Model.showNodes("startSending", false);
        if (Model.doDbg) {
            Model.debug.println("startSending " + title + " " + this
                    + " triggered by " + trig + " on " + net);
        }
        if (Model.doDbg) {
            Model.debug.println("startSending state = " + Path.pathState + "; fromHereTo = " + fromHereTo);
        }
        if (Path.pathState == Model.State.WAIT_FOR_HIT && shouldGoTo >= 0
                && shouldGoTo < Model.node.length) {
            //  set the hasBball and goingTo values
            fromHereTo = shouldGoTo;
            shouldGoTo = -1;
            Path.setBall(ID);
            Path.setGoingTo(fromHereTo);
            Path.setState(Model.State.RUN, "startSending");
        }
    }

    public int findChannel(SubNet net, boolean show) {
        int nt = 0;
        while (nt < Model.PORTAL_NET_CAPACITY && status[nt].net != net) {
            nt++;
        }
        if (nt >= Model.PORTAL_NET_CAPACITY) {
            nt = -1;
        }
        if (show) {
            if (Model.doDbg) {
                Model.debug.println("14. findNet:: " + this
                        + " has netID " + net.ID + " at " + nt);
            }
        }
        return nt;
    }

    public boolean checkRange(SubNet net) {
        int nt = findChannel(net, false);
        boolean res = status[nt].sending;
        if (res && status[nt].radius > (Model.screenSize.width + Model.screenSize.height)) {
            status[nt].sending = false;
        }
        return res;
    }

    public void setPrev(Node B, SubNet net) {
        int nt = findChannel(net, false);
        status[nt].prev = B;
    }

    public Node getPrev(SubNet net) {
        Node res = null;
        int nt = findChannel(net, false);
        if (nt < 0 || status[nt] == null) {
            if (Model.doDbg) {
                Model.debug.println("16. getPrev:: info[" + nt + "] is null for " + this);
            }
        } else {
            res = status[nt].prev;
        }
        return res;
    }

    public static void clearAll(boolean ends) {
        for (int i = 0; i < Model.node.length; i++) {
            if (ends || (Model.node[i] != Model.startNode && Model.node[i] != Model.endNode)) {
                Model.node[i].mark(Mode.NONE);
            }
        }
    }

    public void paintHubReceived(Graphics g) {

    }

    public void doPaint(Graphics g) {
//        Model.paintText(g, Color.black, toString(), 20, 20 + ID * 20);
        if (Model.doFlow) {
            // for all networks
            for (int ch = 0; ch < Model.PORTAL_NET_CAPACITY; ch++) {
                // if you are sending
                try {
                    if (Model.subNets.size() == 1
                            || (status[ch].net != null
                            && status[ch].net.flowing
                            && status[ch].sending)) {
                        // find the network color
                        SubNet net = status[ch].net;
                        if (net != null) {
                            Color clr = Model.stnColor[net.ID % Model.COLORS];
                            // if that net is really sending,
                            if (status[ch].sending) {
                                // draw the circle at the right radius
                                g.setColor(clr);
                                int rad = (int) status[ch].radius;
                                int rad2 = rad + rad;
                                g.drawOval(status[ch].cclx - rad, status[ch].ccly - rad, rad2,
                                        rad2);
                                // check for next node
                                // if this node has the ball
                                if (ID == Path.getBall()) {
                                    // and if it has a destination
                                    int toN = Path.getGoingTo();
                                    if (toN >= 0) {
                                        // calculate where on the circle
                                        Node to = Model.node[toN];
                                        double dpx = to.px;
                                        double dpy = to.py;
                                        double radians = angleTo(status[ch].cclx, status[ch].ccly,
                                                dpx, dpy);
                                        // find the spot on the circle
                                        double px = status[ch].cclx + status[ch].radius
                                                * Math.cos(radians);
                                        double py = status[ch].ccly + status[ch].radius
                                                * Math.sin(radians);
                                        // and draw the ball
                                        if (!Path.firstDraw) {
                                            if (Model.doDbg) {
                                                Model.debug.println("17. doPaint:: SubNet "
                                                        + net.ID + " first draw at radius "
                                                        + status[ch].radius);
                                            }
                                            Path.firstDraw = true;
                                        }
                                        // put the node's spot on 
                                        g.setColor(Color.red);
                                        g.fillOval((int) px - 4, (int) py - 4, 8, 8);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error " + e);
                }
            }
        }
//        for (int ch = 0; ch < Model.PORTAL_NET_CAPACITY; ch++) {
//            if (status[ch].hasToken) {
//                SubNet itsNet = status[ch].net;
//                if (itsNet != null) {
//                    int ndx = itsNet.ID % Model.stnColor.length;
//                    g.setColor(Model.stnColor[ndx]);
//                    g.fillOval(px - 6, py - 6, 12, 12);
//                }
//            }
//        }
        boolean go = true;
        switch (marked) {
            case NONE:
                go = false;
                break;
            case START:
                g.setColor(Color.green);
                break;
            case GO_TO:
                //g.setColor(Color.magenta);
                break;
            case END:
                //g.setColor(Color.red);
                break;
            case GO_FROM:
                //g.setColor(Color.cyan);
                break;
        }
        if (go) {
            g.fillOval(px - 8, py - 8, 16, 16);
        }
        g.setColor(Color.red);
        g.fillOval(px - 2, py - 2, 4, 4);
        String str = "" + ID;
        if (isMainHub) {
            str = "Main Hub";
        }
        g.drawString(str, px + 10, py - 5);
    }

    private double angleTo(int cx, int cy, double px, double py) {
        double dx = px - cx;
        double dy = py - cy;
        return Math.atan2(dy, dx);
    }

    public void mark(Mode value) {
        marked = value;
    }

    /**
     *
     * distance from another Station
     *
     * @param s - the station
     * @return the distance
     */
    public double distanceFrom(Node s) {
        double x = fx - s.fx;
        double y = fy - s.fy;
        return Math.sqrt(x * x + y * y);
    }

    /**
     *
     * distance from a point
     *
     * @param px x location
     * @param py y location
     * @param pz z location
     * @return distance from this point
     */
    public double sqDistanceFrom(double px, double py, double pz) {
        double x = fx - px;
        double y = fy - py;
        return x * x + y * y;
    }

    /**
     * find an empty network for this station
     *
     * @param ist the station index
     */
    public SubNet findANetwork() {
        boolean done = false;
        SubNet net = null;
        for (int i = 0; !done && (i < Model.subNets.size()); i++) {
            net = (SubNet) Model.subNets.get(i);
            if (net.length() == 0) {
            if (status[0].net == null) {
                    net.insertNode(this, 0);
                } else if (status[1].net == null) {
                    net.insertNode(this, 1);
                } else {
                    throw new RuntimeException("No node space for new net");
                }
                done = true;
            }
        }
        if (!done) {
            int n = Model.subNets.size();
            net = new SubNet(n);
            Model.subNets.add(net);
            if (status[0].net == null) {
                net.insertNode(this, 0);
            } else if (status[1].net == null) {
                net.insertNode(this, 1);
            } else {
                throw new RuntimeException("No node space for new net");
            }
            if (Model.doDbg) {
                Model.debug.println("findANetwork:: Created Net " + net);
            }
        }
        return net;
    }

    /**
     * check all this node's networks
     */
    public void update() {
        updatePosition();
        updateMessage();
        Model.validateNets();
        updateNetMembership();
        Model.validateNets();
    }

    private static boolean shouldGo = false;

    private void updatePosition() {
        Node sender = this;
        boolean collide = false;
        int sendCh = -1;
        SubNet net = null;
        for (int nt = 0; nt < Model.PORTAL_NET_CAPACITY; nt++) {
            if (status[nt].net != null && status[nt].net.running) {
                sendCh = nt;
                net = status[nt].net;
                if (shouldGo && sender.ID == 8) {
//                    Model.debug.println("Sender " + this
//                            + "\n    on network " + net);
                }
                for (Node receiver : Model.node) {
                    boolean found = false;
                    if (receiver.ID != sender.ID) {
                        int d = Model.nodeDistance(receiver.ID, sender.ID, false);
                        if (d < 20) {
                            if (d == 0) {
                                d = 10;
                            }
                            dvx = Model.speed * Model.radialSpeed * (fx - receiver.fx) / d;
                            dvy = Model.speed * Model.radialSpeed * (fy - receiver.fy) / d;
                            collide = true;
                        }
                        if (Model.doFlow && sendCh > -1) {
                            double er = Math.abs(d - sender.status[sendCh].radius);
                            if (er < Model.radialSpeed) {
                                // something arrived somewhere
                                // sender might be receiver's prev on this channel
                                int rcvCh = receiver.findChannel(net, false);
                                if (rcvCh > -1 && sender == receiver.status[rcvCh].prev
                                        // sender must have the token on this channel
                                        && sender.status[sendCh].hasToken) {
                                    // find its channel
                                    if (Model.doDbg) {
                                        Model.debug.println("Sender " + sender.ID
                                                + "; " + Path.getBall() + " has the ball; receiver " + receiver.ID);
                                    }
                                    if (receiver.status[rcvCh].prev == sender) {
                                        if (Model.doDbg) {
                                            Model.debug.println("token from " + sender.ID
                                                    + " reached " + receiver.ID
                                                    + " on network " + net.ID);
                                        }
                                        receiver.startSending(net, "Node updatePosition", sender);
                                        found = true;
                                    }
                                }
                                if (Model.doDbg) {
                                    Model.debug.println(">>> sender " + sender.ID
                                            + " hasBall " + Path.getBall()
                                            + " net.ID " + net.ID
                                            + " receiver " + receiver.ID
                                            + "; Model.routeFrom " + Model.routeFrom
                                            + "; Model.routeTo " + Model.routeTo
                                            + "; Path.pathState " + Path.pathState
                                            + "<<<<<");
                                }
                                if (sender.ID == Path.getBall()
                                        && sender.fromHereTo == receiver.ID) {
                                    if (Model.doDbg) {
                                        Model.debug.println("Ball arrived at " + receiver.ID
                                                + " from " + sender.ID);
                                    }

                                    Path.firstDraw = false;
                                    net.flowing = false;
                                    //                            Model.node[Model.routeFrom].stopSending(channel);
                                    sender.fromHereTo = -1;
                                    // if finished the path, go back to wait
                                    if (Model.endNode.ID == Model.routeTo) {
                                        Path.setBall(-1);
                                        clearAll(true);
                                        Path.setState(Model.State.WAIT, "Finished the Path");
                                        // otherwise, move to next node
                                    } else {
                                        Path.setState(Model.State.FINISHED, "Finished a Leg");
                                        Path.setBall(-1);
                                        clearAll(false);
                                        Path.goingFromNode = Model.node[Model.routeTo];
                                        Path.goingFromNode.mark(Mode.GO_FROM);
                                        // check if the new from node is waiting to start
                                        if (ID == Path.getBall()) {
                                            // if so, enable ball drawing
                                            Path.running = true;
                                            net.flowing = true;
                                            Path.setState(Model.State.RUN, "started running");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
        }
        if (!isMainHub) {
            if (!collide) {
                dvx = Model.speed * Model.radialSpeed * (Math.random() - 0.5);
                dvy = Model.speed * Model.radialSpeed * (Math.random() - 0.5);
            }
            if ((px < Model.EDGE) && (dvx < 0)) {
                dvx = -dvx;
            }
            if ((py < Model.EDGE) && (dvy < 0)) {
                dvy = -dvy;
            }
            if ((px > (Model.screenSize.width - Model.EDGE)) && (dvx > 0)) {
                dvx = -dvx;
            }
            if ((py > (Model.screenSize.height - Model.EDGE)) && (dvy > 0)) {
                dvy = -dvy;
            }
            vx += dvx / 10;
            vy += dvy / 10;
            double avx = Math.abs(vx);
            if (avx > Model.MAXV) {
                vx = Model.MAXV * avx / vx;
            }
            double avy = Math.abs(vy);
            if (avy > Model.MAXV) {
                vy = Model.MAXV * avy / vy;
            }
            fx += vx;
            fy += vy;
            px = (int) fx;
            py = (int) fy;
        }
    }

    public boolean isIn(SubNet net) {
        boolean res = false;
        Members dl = net.members;
        Node here = dl.first;
        for (int i = 0; !res && i < dl.length(); i++) {
//            Model.debug.println("33. nd.isin(" + net + ") needs ID: " + this.ID);
            if (here == null) {
                throw new RuntimeException("" + this + ".isIn(" + net + ") broke");
            }
            res = here.ID == this.ID;
            here = here.getPrev(net);
        }
        return res;
    }

    private void updateMessage() {
        if (Model.doFlow) {
            for (int nt = 0; nt < Model.PORTAL_NET_CAPACITY; nt++) {
                if (status[nt].sending) {
                    status[nt].radius += Model.radialSpeed;
                }
            }
        }
    }

    private void updateNetMembership() {
        SubNet net = null;
        Model.validateNets();
        for (int nt = 0; nt < Model.PORTAL_NET_CAPACITY; nt++) {
            net = status[nt].net;
            if (net != null && !net.belongs(this, Model.remRadiusSq)) {
                net.removeNode(ID);
                dropFromNet(net);
                status[nt].net = null;
            }
        }
        for (int nt = 0; nt < Model.PORTAL_NET_CAPACITY; nt++) {
            net = status[nt].net;
            if (net == null) {
                if (Model.putNodeInNet(this, nt)
                        && Model.doDbg) {
                    Model.showNets("Node.updateNetMembership");
                }
            }
        }
    }

    private int alreadyLinked(Node tryIt) {
        int res = -1;
        // space on this
        if (status[0].net == null) {
            if (status[1].net == null
                    || (status[1].net != tryIt.status[0].net
                    && status[1].net != tryIt.status[1].net)) {
                res = 0;
            }
        } else if (status[1].net == null) {
            if (status[0].net != null && status[0].net != tryIt.status[0].net
                    && status[0].net != tryIt.status[1].net) {
                res = 1;
            }
        }
        return res;
    }

    private int mightWork(Node tryIt) {
        int res = -1;
        boolean harder = true;
        if (tryIt.ID == ID) {
            harder = false;
        } else if (distanceFrom(tryIt) >= Model.radius) {
            harder = false;
        }
        if (harder) {
            res = alreadyLinked(tryIt);
        }
        return res;
    }

    /**
     * if this node belongs to no nets, find a node closer than radius with less
     * than 2 networks and make a new network
     */
    public void checkSolo() {
        for (int ti = 0; ti < Model.node.length; ti++) {
            Node tryIt = Model.node[ti];
            if (Model.doDbg) {
                Model.debug.println("" + this + ".checkSolo on " + tryIt);
            }
            int thisNdx = mightWork(tryIt);
            if (thisNdx >= 0) {
                boolean done = false;
                for (int n = 0; !done && n < Model.PORTAL_NET_CAPACITY; n++) {
                    if (tryIt.status[n].net == null) {
                        if (Model.doDbg) {
                            Model.debug.println("Combine " + ID + " with " + tryIt.ID
                                    + " on " + n);
                            Model.debug.flush();
                        }
                        SubNet net = findANetwork();
                        Model.debug.flush();
                        net.insertNode(tryIt, n);
                        tryIt.status[n].net = net;
                        done = true;
                        Model.validateNets();
                        Model.showNets("Node.checkSolo");
                    }
                }
            }
        }
    }

    public int dropFromNet(SubNet net) {
        int res = -1;
        for (int i = 0; i < Model.PORTAL_NET_CAPACITY; i++) {
            if (status[i].net == net) {
                status[i].net = null;
                res = i;
            }
        }
        return res;
    }

    public int activeNets() {
        int res = 0;
        if (status[0].net != null) {
            res++;
        }
        if (status[1].net != null) {
            res++;
        }
        return res;
    }

    public void setIsMainHub(boolean isMainHub) {
        this.isMainHub = isMainHub;
    }

    /**
     * string representation
     *
     * @return the string
     */
    @Override
    public String toString() {
        String res = "Node " + ID + "[" + px + "," + py + "]";
        for (int ch = 0; ch < Model.PORTAL_NET_CAPACITY; ch++) {
            if (status[ch].net == null) {
                res += " -";
            } else {
                res += " " + status[ch].net.ID;
            }
        }
        return res;
    }
}
