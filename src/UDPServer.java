import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class UDPServer {

	public static void main(String args[]) throws Exception {

		DatagramSocket serverSocket = new DatagramSocket(9876);
		System.out.println("UDP server rodando!");
		
		byte[] receivedData = new byte[1024];

		while(true) {
			
			Arrays.fill(receivedData, (byte)0);
			DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
			
			serverSocket.receive(receivePacket); 

			String sentence = new String(receivePacket.getData()); 

			InetAddress ipAddress = receivePacket.getAddress(); 
			int port = receivePacket.getPort(); 

			String capitalizedSentence = sentence.toUpperCase(); 

			byte[] sendData = capitalizedSentence.getBytes(); 
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port); 

			serverSocket.send(sendPacket); 
		}
	}
}