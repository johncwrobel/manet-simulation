/*
 * Network.java
 *
 * Created on December 27, 2005, 11:08 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package System;

import java.awt.*;

/**
 * container for once complete cluster
 *
 * @author dsmith
 */
public class SubNet {

    /**
     * the cluster identity
     */
    public int ID;
    /**
     * the cluster centroid
     */
    public int cx;
    /**
     * the cluster centroid
     */
    public int cy;
    /**
     * int array of the path indices
     */
    public Members members;

    public boolean changed;
    /**
     * mode of operation of path checking <br>
     * 0 - set up the iteration <br>
     * 1 - perform one check <br>
     * 2 - set up the repair <br>
     * 3 - repair iteration<br>
     */
    public int mode = 0;
    /**
     * details
     */
    public int details;
    public static int IDs = 0;
    public boolean running;
    public boolean flowing;

    /**
     * Creates a new instance of Network
     *
     * @param id its identity
     */
    public SubNet(int id) {
        ID = id;
        nullify();
        details = 0;
        mode = 0;
        running = false;
        changed = true;
        flowing = false;
    }

    public int first() {
        if (length() == 0) {
            return -1;
        } else {
            return members.first.ID;
        }
    }
    
    public void validate() {
        members.validate();
    }

    /**
     * Updates the flow messages
     */
    public void update() {
        // restart a dead SubNet
        int count = members.update(this);    
        if(count == 0 && length() > 0 && Model.doFlow) {
            Node doThis = Model.node[first()];
            doThis.startSending(this, "restart dead SubNet", null);
        }
    }

    public boolean hasMarkedRoute(Model.State state, int fr, int t) {
        boolean res = state == Model.State.FIND_ROUTE;
        return res;
    }

    /**
     * zero out data contents
     */
    public void nullify() {
        cx = -10000;
        cy = -10000;
        members = new Members(this);
        mode = 0;
        changed = true;
        running = false;
    }

    public int length() {
        return members.length();
    }

    /**
     * insert a node
     *
     * @param nd the node
     * @param index the ID of the station
     */
    public void insertNode(Node nd, int nt) {
        members.add(nd, this, nt);
        centroid();
        if(Model.doDbg)
            Model.debug.println("Inserted " + nd + " in " + this + " at " + nt);
    }

    /**
     * does a station belong in this network?
     *
     * @param st the station
     * @param radiusSq the max permitted distance squared to avoid a lot of
     * square roots
     * @return the answer
     */
    public boolean belongs(Node st, double distSq) {
        boolean res = st != null && members.closerThan(st, distSq);
        return res;
    }

    public boolean containedIn(SubNet B) {
        return members.containedIn(B.members);
    }

    /**
     * 
     * @param A a subNet
     * @param r squared radius
     * @return true if all of this belongs in A
     */
    public boolean allBelongsIn(SubNet A, double r) {
        return members.allBelongsIn(A, r);
    }

    /**
     * remove an index from this sub-network
     *
     * @param cl the index
     * @param rt the root index
     */
    public void removeNode(int ID) {
        members.traverseRem(ID);
    }

    /**
     *
     * @param n the details
     */
    public void setDetails(int n) {
        details = n;
    }

    /**
     * check the path path to shorten it where able
     */
    public void checkBestPath() {

        centroid();
    }

    /**
     * recompute the centroid
     */
    private void centroid() {
        try {
        double x = 0;
        double y = 0;
        int n = length();
        if (n == 0) {
            nullify();
        } else {
            Node d = members.first;
            for (int ih = 0; ih < n; ih++) {
                x += d.fx;
                y += d.fy;
                d = d.getPrev(this);
            }
            cx = (int) (x / n);
            cy = (int) (y / n);
        }
        } catch(Exception e) {}
    }

    static int c_radius = 6;
    static int dia = 12;

    /**
     * draw the chain connecting this network
     *
     * @param c the color
     * @param off the offset
     * @param g the graphic environment
     */
    public void drawChain(Color c, int off, Graphics g) {

        if (length() > 0) {
            centroid();
            g.setColor(c);
            //g.fillOval(cx - c_radius, cy - c_radius, dia, dia);
            int[] x = new int[3];
            int[] y = new int[3];
            x[0]=cx; x[1]=cx-5; x[2] = cx+5;
            y[0] = cy-5;
            y[1] = cy+5;
            y[2] = cy+5;
            Polygon p = new Polygon(x, y, 3);
            g.fillPolygon(p);
            Node node = members.first;
            Node here = node.getPrev(this);
            for (int hi = 0; hi < members.length(); hi++) {
                if(here == null || node == null) break;
                g.drawLine(node.px + off, node.py + off,
                        here.px + off, here.py + off);
                node = here;
                here = here.getPrev(this);
            }
            g.setColor(Color.black);
            String str = Integer.toString(ID);
            g.drawString(str, cx + 5, cy - 5);
        }
    }

    /**
     * determine if a value is in an int array
     *
     * @param v the value
     * @param va the array
     * @return yes or no
     */
    public boolean isin(int v) {
        boolean found = false;
        for (int i = 0; (i < members.length() && !found); i++) {
            found = contains(v);
        }
        return found;
    }

    /**
     * does this network contain a given index?
     *
     * @param target the index to find
     * @return whether it is there
     */
    public boolean contains(int target) {
        boolean res = false;
        Node here = members.first;
        for (int ih = 0; ih < members.length() && !res; ih++) {
            int si = here.ID;
            if (si == target) {
                res = true;
            } else {
                int nt = here.findChannel(this, false);
                here = here.status[nt].prev;
            }
        }
        return res;
    }

    /**
     * string representation
     *
     * @return the string
     */
    @Override
    public String toString() {
        String res = "subNet " + ID;
        res += " containing node IDs ";
        String ms = members.toString();
        res += ms;
        return res;
    }
}
