package man.who.scan.my.app.die.a.mother.vpn.server.nat;

public class NATSession {
	public int RemoteIP;
    public short RemotePort;
    public String RemoteHost;
//    public int BytesSent;
//    public int PacketSent;
    public long LastNanoTime;
    
    @Override
    public boolean equals(Object obj) {
    	if( obj instanceof NATSession) {
    		NATSession session = (NATSession)obj;
    		if(this.RemoteIP == session.RemoteIP && this.RemotePort == session.RemotePort) {
    			return true;
    		}
    	}
    	return false;
    }
    
    @Override
    public int hashCode() {
		return RemotePort * 31 + RemoteIP;
    }
}
