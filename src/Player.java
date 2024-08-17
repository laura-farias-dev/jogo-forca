import java.net.InetAddress;

public class Player {
	InetAddress ipAddress;
	int port;
	boolean isReady = false;
	
	public Player(InetAddress ip, int port) {
		this.ipAddress = ip;
		this.port = port;
	}
	
	public void ready() {
		this.isReady = true;
	}

	public boolean isReady() {
		return isReady;
	}
}
