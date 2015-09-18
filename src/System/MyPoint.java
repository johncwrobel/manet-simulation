/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package System;

/**
 *
 * @author dsmith
 */
public class MyPoint {
    public double x;
    public double y;

    public MyPoint(double px, double py) {
        x = px;
        y = py;
    }

    public void add(MyPoint p) {
        x += p.x;
        y += p.y;
    }

    public void add(double xv, double yv) {
        x += xv;
        y += yv;
    }

    public double size() {
        return Math.sqrt(x*x + y*y);
    }
}
