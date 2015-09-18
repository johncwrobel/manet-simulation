package System;



/**
 *
 * @author David
 */
public class NetStatus {
    public SubNet net;
    public Node prev;           // 
    public boolean hasToken;
    public boolean sending;
    public double radius;
    public Node host;
    public int cclx, ccly;
    
    public NetStatus() {
        this(null, null);
    }
    
    public NetStatus(Node nd, SubNet p) {
        net = p;
        host = nd;
        prev = null;
        hasToken = false;
        sending = false;
        radius = 0; 
    }
    
    public String toString() {
        String res =  "NetStatus:: host = " + host + ": net = " + net;
        if(host != null) {
            res += "; prev.ID = " + prev.ID;
             res += "; sending = " + sending;
            res += "; radius = " + radius;     
        }
        return res;
    }
}
