/*
 * Group.java
 *
 * Created on December 31, 2004, 2:44 PM
 */
package System;

/**
 * Class containing a collection of station nodes
 *
 * @author dsmith
 */
public class Members {

    /**
     * circular list of devices
     */
    public Node first;
    private int size;
    private SubNet owner;

    /**
     * constructor given an array of stations
     *
     * @param orig the array
     */
    public Members(SubNet net) {
        owner = net;
        first = null;
        size = 0;
    }

    public int update(SubNet net) {
        int res = 0;
        Node here = first;
        for (int ih = 0; ih < length(); ih++) {
            if (here == null) {
                break;
            }
            here.checkRange(net);
            int ndx = here.findChannel(net, false);
            if (here.status[ndx].sending) {
                res++;
            }
            here = here.getPrev(net);
        }
        globally_optimize(net);
        return res;
    }

    public boolean containedIn(Members blst) {
        // neither list is empty
        boolean match = true;
        // Go through all nodes in this
        Node here = first;
        for (int am = 0; match && am < size; am++) {
            // go through all nodes in blst
            Node there = blst.first;
            boolean found = false;
            for (int bm = 0; !found && bm < blst.size; bm++) {
                if(here == null || there == null) {
                    System.out.println("gotha again");
                }
                found = here.ID == there.ID;
                there = there.getPrev(blst.owner);
            }
            match = found;
            here = here.getPrev(owner);
        }
        return match;
    }


    /**
     * 
     * @param A a subNet
     * @param r squared radius
     * @return true if all of this belongs in A
     */
    public boolean allBelongsIn(SubNet A, double r) {
        // neither list is empty
        boolean isIn = true;
        // Go through all nodes in this
        Node here = first;
        for (int am = 0; isIn && am < size; am++) {
            isIn = A.belongs(here, r);
            here = here.getPrev(owner);
        }
        return isIn;
    }


    public void add(Node nd, SubNet net, int nt) {
        nd.status[nt].net = net;
        nd.status[nt].host = nd;
        if (first == null) {
            nd.status[nt].prev = nd;
            first = nd;
        } else {
            nd.status[nt].prev = first.getPrev(net);
            first.setPrev(nd, net);
        }
        size++;
        if (!net.running) {
            net.running = true;
//            first.startSending(net,"Members add", null);
        }
        owner.changed = true;
    }

    private void globally_optimize(SubNet net) {
        Node doA = null;
        Node doB = null;
        Node doP1 = null;
        Node doP2 = null;
        Node doX = null;

        boolean keepGoing = size > 3;
        double best = -1;
        Node A = first;
        int done = 0;
        while (keepGoing && done++ <= size) {
            // Consider moving A (head's prev)
            // between P1 and P2, 2 adjacent nodes
            // do it if (A - APred) + (A - Asucc) + (P1 - P2)
            // > (APred - Asucc) + (A - P1) + (A - P2)
            // find A successor
            // find successor of A - stn whose prev is A
            Node X = A.getPrev(net);
            while (X.getPrev(net) != A) {
                X = X.getPrev(net);
            }
            Node B = A.getPrev(net);
            Node P1 = B;
            Node P2 = P1.getPrev(net);
            boolean keepDoingLoop = true;
            while (keepDoingLoop) {
                double was = A.distanceFrom(B);
                was += A.distanceFrom(X);
                was += P1.distanceFrom(P2);
                double couldBe = B.distanceFrom(X);
                couldBe += A.distanceFrom(P1);
                couldBe += A.distanceFrom(P2);
                if ((was - couldBe) > best) {
                    best = was - couldBe;
                    doA = A;
                    doB = B;
                    doP1 = P1;
                    doP2 = P2;
                    doX = X;
                }
                P1 = P2;
                P2 = P1.getPrev(net);
                keepDoingLoop = P2 != X;
            }
            A = A.getPrev(net);
        }
        if (best > 0) {
            doX.setPrev(doB, net);
            doP1.setPrev(doA, net);
            doA.setPrev(doP2, net);
            owner.changed = true;
        }
    }

    /**
     * get the size of the group data
     *
     * @return the size
     */
    public int length() {
        return size;
    }

    public Node getID(int ID) {
        Node res = null;
        for (Node nd : Model.node) {
            if (nd.ID == ID) {
                res = nd;
                break;
            }
        }
        return res;
    }

    public int nodeDistance(int ia, int ib) {
        Node A = getID(ia);
        Node B = getID(ib);
        return nodeDistance(A, B);
    }

    public int nodeDistance(int px, int py, Node B) {
        double x = (px - B.fx);
        double y = (py - B.fy);
        int res = (int) Math.sqrt(x * x + y * y);
//		Model.debug.println("Distance from " + A.ID + " to " + B.ID + " -> " + res);
        return res;
    }

    public int nodeDistance(Node A, Node B) {
        double x = (A.fx - B.fx);
        double y = (A.fy - B.fy);
        int res = (int) Math.sqrt(x * x + y * y);
//		Model.debug.println("Distance from " + A.ID + " to " + B.ID + " -> " + res);
        return res;
    }

    public static double nodeDistSq(Node A, Node B) {
        if (A == null || B == null) {
            System.out.println("ouch");
            return 100000000000.0;
        }
        double x = (A.fx - B.fx);
        double y = (A.fy - B.fy);
        double res = x * x + y * y;
//		Model.debug.println("Distance from " + A.ID + " to " + B.ID + " -> " + res);
        return res;
    }

    public int computeDistance() {
        int res = 0;
        for (int i = 1; i < length(); i++) {
            res += nodeDistance(i - 1, i);
        }
        return res;
    }

    public boolean closerThan(Node nd, double distSq) {
        boolean res = true;
        Node last = first;
        if (last == null) {
            res = false;
        } else {
            Node here = last.getPrev(owner);
            for (int ih = 0; ih < size; ih++) {
                double d = nodeDistSq(nd, here);
                if (d > distSq) {
                    res = false;
                    break;
                }
                last = here;
                here = here.getPrev(owner);
            }
        }
        return res;
    }

    /**
     * traverseRem traverses and removes an index from a network
     *
     * @param ID the root ID of the group
     * @param from index to remove
     * @param skip index to skip in the traversal
     */
    public void traverseRem(int ID) {
        // traverse the group removing instances of ID
        Node last = first;
        Node here = last.getPrev(owner);
        for (int ih = 0; ih < size && here.ID != ID; ih++) {
            last = here;
            here = here.getPrev(owner);
        }
        if (here.ID == ID) {
            size--;
            if (size == 0) {
                first = null;
            } else {
                Node nd = here.getPrev(owner);
                last.setPrev(nd, owner);
                if (first.ID == ID) {
                    first = nd;
                }
            }
            owner.changed = true;
        }
    }

    public void validate() {
        Node here = first;
        String str = "Net " + owner.ID + ": ";
        for (int ih = 0; ih < size; ih++) {
            if (here == null) {
                Model.debug.println("null in subNet members" + str);
                Model.debug.flush();
                throw new RuntimeException("null in subNet members");
            }
            str += here.ID + "->";
            here = here.getPrev(owner);
        }
        if (here != first) {
            Model.debug.println("subNet members bad last link" + str);
            Model.debug.flush();
            throw new RuntimeException("subNet members bad last link");
        }
    }

    /**
     * toString()
     *
     * @return string describing the object
     */
    @Override
    public String toString() {
        Node here = first;
        String res = "[";
        for (int ih = 0; ih < size; ih++) {
            res += here.ID;
            if (ih < (size - 1)) {
                res += ", ";
            }
            here = here.getPrev(owner);
        }
        res += "]";
        return res;
    }

}
