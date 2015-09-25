/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

 *  remaining isses:
 *    1. next message leaving ts source before the 
 *       previous message has been received
 *    2. if surce r destination dropped fr a sibnet, recompute
 */
package System;

import static System.Model.cornerX;
import static System.Model.cornerY;
import static System.Model.doDbg;
import static System.Model.endNode;
import static System.Model.inPtDistance;
import static System.Model.node;
import static System.Model.routeFrom;
import static System.Model.routeTo;
import static System.Model.screenSize;
import static System.Model.startNode;
import static System.Model.subNets;

/**
 *
 * @author David
 */
public class Path {

    public static Model.State pathState;
    private static int goingTo = -1;
    private static int hasBall = -1;
    public static Node goingFromNode = null;
    public static boolean running = false;
    public static boolean firstDraw = false;

    public Path() {
        setState(Model.State.WAIT, "Constructor");
        goingTo = -1;
        hasBall = -1;
        running = false;
        firstDraw = false;
    }

    public static void setBall(int nd) {
        setFlowing(nd);
        hasBall = nd;
    }

    public static int getBall() {
        return hasBall;
    }

    public static void setGoingTo(int nd) {
        goingTo = nd;
    }

    public static int getGoingTo() {
        return goingTo;
    }

    public static void setState(Model.State st, String why) {
        pathState = st;
        if (Model.doDbg) {
            Model.debug.println("22. setState:: pathState changed to " + getState()
                    + " - " + why);
        }
    }

    public static Model.State getState() {
        return pathState;
    }


    public void update() {
        int closest;
        int furthest;
        boolean found;
        SubNet net = null;
        switch (pathState) {
            case FIND_ENDS:
                closest = inPtDistance(0, cornerX, cornerY);
                furthest = closest;
                startNode = node[0];
                endNode = node[0];
                //gets the closest and furthest node distance wise from the current node.
                for (int i = 0; i < node.length; i++) {
                    int d = inPtDistance(i, cornerX, cornerY);
                    if (d < Integer.MAX_VALUE && d > furthest) {
                        furthest = d;
                        endNode = node[i];
                    } else if (d < closest) {
                        closest = d;
                        startNode = node[i];
                    }
                }
                setFlowing(startNode.ID);
                Node.clearAll(true);
                startNode.mark(Node.Mode.START);
                endNode.mark(Node.Mode.END);
                if (Model.doDbg) {
                    Model.debug.println("24. update FIND_ENDS:: ends are from " + startNode.ID
                            + "  to " + endNode.ID);
                }
                updateCorners();
                goingFromNode = startNode;
                setState(Model.State.FIND_ROUTE, "Found ends");
                break;
            case FIND_ROUTE:
                // can we get to the endDevice?
                found = false;
                for (int i = 0; i < Model.PORTAL_NET_CAPACITY; i++) {
                    net = goingFromNode.status[i].net;
                    if (net != null && net.isin(endNode.ID)) {
                        routeFrom = goingFromNode.ID;
                        //KEVIN's CHANGE ROUTETO
                        //routeTo = 0;
                        routeTo = endNode.ID;
                        if (Model.doDbg) {
                            Model.debug.println("25. update FIND_ROUTE:: Final route from " + routeFrom + " to " + routeTo);
                        }
                        found = true;
                        setFlowing(routeFrom);
                        setGoingTo(routeTo);
                        break;
                    }
                }
                routeFrom = goingFromNode.ID;
                Node nd = Model.node[routeFrom];
                if (!found) {
                    // A* algorithm - find the node to go to that minimizes
                    // the length of the route to that node and the crow
                    // flies distance to the endStation.
                    int minDist = Integer.MAX_VALUE;
                    routeTo = -1;
                    net = null;
                    for (int nt = 0; nt < Model.PORTAL_NET_CAPACITY; nt++) {
                        SubNet tryNet = goingFromNode.status[nt].net;
                        if (tryNet != null) {
                            Node here = tryNet.members.first;
                            for (int ih = 0; ih < tryNet.length(); ih++) {
                                int stnndx = here.ID;
                                if (stnndx != goingFromNode.ID && stnndx != routeFrom) {
                                    if (here.activeNets() > 1) {
                                        int dist = (int) (here.distanceFrom(endNode));
                                        if (dist < minDist) {
                                            minDist = dist;
                                            routeTo = stnndx;
                                            net = tryNet;
                                            if (Model.doDbg) {
                                                Model.debug.println("26. update FIND_ROUTE:: Route from " + routeFrom + " to " + routeTo);
                                            }
                                        }
                                    }
                                }
                                here = here.getPrev(tryNet);
                            }
                        }
                    }
                    if (routeTo >= node.length || routeTo < 0) {
                        setState(Model.State.FIND_ROUTE, "Didn't find routeTo");
                    } else {
                        // mark routeTo
                        node[routeTo].mark(Node.Mode.GO_TO);
                        setFlowing(routeTo);
                    }
                }
                // OK, we know we need to get from Device routeFrom to Device
                // routeTo using network 'net'
                if (Model.doDbg) {
                    Model.debug.println("28. update FIND_ROUTE:: Make a route from "
                            + routeFrom + " to "
                            + routeTo + " using network " + net);
                }
                int ch = nd.findChannel(net, false);
                Model.showNodes("is " + nd.ID + " running", false);
                if (nd.status[ch].sending) {
                    setState(Model.State.WAIT_FOR_HIT, "Started a message");
                    nd.shouldGoTo = routeTo;
                } else {
                    nd.fromHereTo = routeTo;
                    if (routeTo < node.length && routeTo >= 0) {
                        //  set the hasBball and goingTo values
                        setBall(routeFrom);
                        setGoingTo(routeTo);
                        setState(Model.State.RUN, "Started a message");
                    }
                }
                // route machinery switches to WAIT_FOR_ROUTE when the
                // route segment starts, then to FINISHED_ROUTE when it ends
                break;
            case WAIT_FOR_HIT:
            case RUN:
                // make sure the route is still feasible - node going to has more than one net
                found = routeTo == endNode.ID || subNets.size() > 1;
                if (!found | routeTo == -1) {
                    // can't reach routeTo any more - go back to BEST_ROUTE
                    if (Model.doDbg) {
                        Model.debug.println("29. update FIND_ROUTE:: Route not feasible - pick another");
                    }
                    setState(Model.State.FIND_ROUTE,
                            "Path to " + routeTo + " with end " + endNode.ID + " nn = " + subNets.size() + " became unfeasible");
                    Node.clearAll(false);
                } else {
                    int gtb = getBall();
                        if(gtb >= 0) {
                        Node there = Model.node[getBall()];
                        for(int i = 0; i < 2; i++) {
                            SubNet nt = there.status[i].net;
                            if(nt != null && !nt.flowing) {
                                nt.flowing = true;
                            }
                        }
                    }
                }
                break;
            case FINISHED:
                if (endNode.ID != routeTo) {
                    setState(Model.State.FIND_ROUTE, "Another segment");
                } else {
                    routeFrom = -1;
                    routeTo = -1;
                }
                break;
        }
    }

    private void updateCorners() {
        if (cornerX == 0) {
            // we are on the left edge
            if (cornerY == 0) {
                // we are top left - go to bottom left
                cornerY = screenSize.height;
            } else {
                // we are bottom left - go to bottom right
                cornerX = screenSize.width;
            }
        } else {
            // we are on the right edge
            if (cornerY == 0) {
                // we are top right - go to top left
                cornerX = 0;
            } else {
                // we are bottom right go to top right
                cornerY = 0;
            }
        }
    }

    public static void setFlowing(int ind) {
        SubNet A = null;
        SubNet B = null;
        if(ind >= 0) {
            A = Model.node[ind].status[0].net;
            B = Model.node[ind].status[1].net;
        }
        for (int i = 0; i < subNets.size(); i++) {
            SubNet it = subNets.get(i);
            if (it == A || it == B) {
                it.flowing = true;
            } else {
                it.flowing = false;
            }
        }
    }
}
