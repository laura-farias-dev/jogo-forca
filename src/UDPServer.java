import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class UDPServer {

	public static void main(String args[]) throws Exception {
		DatagramSocket serverSocket = new DatagramSocket(9876);
		System.out.println("UDP server rodando!");
		
		byte[] receivedData = new byte[1024];
		
		HashMap<InetAddress, Player> playersConnected = new HashMap<InetAddress, Player>();
		
		Hangman hm = new Hangman();
		
		System.out.println(hm.getEncodedWord());
		hm.guess("A");
		System.out.println(hm.getEncodedWord());

		while(true) {
			Arrays.fill(receivedData, (byte)0);
			DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
			
			serverSocket.receive(receivePacket);

			String sentence = new String(receivePacket.getData()).trim(); 

			InetAddress ipAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
						
			if(PlayerMessage.valueOf(sentence) == PlayerMessage.CONNECT) {
				System.out.println("Connection request received from ip: " + ipAddress + " port: " + port);
				playersConnected.put(ipAddress, new Player(ipAddress, port));
			} else if (PlayerMessage.valueOf(sentence) == PlayerMessage.READY) {
				playersConnected.get(ipAddress).ready();
				System.out.println("Player with IP " + ipAddress + " marked as ready");
			}
			
			String packetAck = sentence + " " + "ACK";

			byte[] sendData = packetAck.getBytes(); 
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);

			serverSocket.send(sendPacket); 
		}
	}
}