import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {

	public static void main(String args[]) throws Exception { 

		BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in)); 

		DatagramSocket clientSocket = new DatagramSocket();

		InetAddress ipAddress = InetAddress.getByName("localhost");
		int port = 9876;

		//Ler do teclado a String a ser enviada
		System.out.println("Digite o texto a ser enviado");
		String sentence = keyboardReader.readLine();
		
		//Criar o segmento UDP com a String como payload (campo de dados)
		byte[] sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);

		//Enviar o segmento UDP
		clientSocket.send(sendPacket);

		//Criar o objeto que armazenar� o segmento UDP de resposta
		byte[] receivedData = new byte[1024]; 
		DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length); 

		//Receber o segmento UDP
		clientSocket.receive(receivePacket); 

		String modifiedSentence = new String(receivePacket.getData()); 
		System.out.println("FROM SERVER:" + modifiedSentence);
		
		clientSocket.close(); 
	} 

}
