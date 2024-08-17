import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {

	public static void main(String args[]) throws Exception { 

		BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in)); 

		DatagramSocket clientSocket = new DatagramSocket();

		InetAddress ipAddress = InetAddress.getByName("localhost");
		int port = 9876;
		
		System.out.println("Connecting...");
		
		byte[] sendConnect = PlayerMessage.CONNECT.name().getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendConnect, sendConnect.length, ipAddress, port);

		clientSocket.send(sendPacket);
		
		System.out.println("Digite a ação desejada: ");
		System.out.println("1 - Pronto para partida");
		
		String userAction = keyboardReader.readLine();
		
		if(userAction.equals("1")) {
			byte[] readyMessage = PlayerMessage.READY.name().getBytes();
			DatagramPacket readyPacket = new DatagramPacket(readyMessage, readyMessage.length, ipAddress, port);
			
			clientSocket.send(readyPacket);
		}
		
		while(true) {
			byte[] receivedData = new byte[1024]; 
			DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length); 

			clientSocket.receive(receivePacket); 

			String serverResponse = new String(receivePacket.getData()); 
			System.out.println("FROM SERVER: " + serverResponse);
		}
	} 

}
