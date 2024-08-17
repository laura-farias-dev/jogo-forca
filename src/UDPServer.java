import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;

public class UDPServer {

    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9881);
        System.out.println("UDP server rodando!");

        byte[] receivedData = new byte[1024];
        HashMap<String, Player> playersConnected = new HashMap<String, Player>();
        Hangman hm = new Hangman();

        while (true) {
            Arrays.fill(receivedData, (byte) 0);
            DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
            serverSocket.receive(receivePacket);

            String sentence = new String(receivePacket.getData()).trim();
            InetAddress ipAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            if (PlayerMessage.CONNECT.name().equals(sentence)) {
                System.out.println("Requisição de conexão recebida do IP: " + ipAddress + " porta: " + port);
                playersConnected.put(ipAddress.toString() + port, new Player(ipAddress, port));
                
                System.out.println(playersConnected.size());

                if (playersConnected.size() == 2) {
                    System.out.println("Dois jogadores conectados. Esperando que eles estejam prontos...");
                }
            } else if (PlayerMessage.READY.name().equals(sentence)) {
                playersConnected.get(ipAddress.toString() + port).ready();
                System.out.println("Jogador com IP " + ipAddress + " marcado como pronto");

                if (playersConnected.size() == 2 && playersConnected.values().stream().allMatch(Player::isReady)) {
                    String encodedWord = "Palavra: " + hm.getEncodedWord() + "\n" + "Chute uma letra: ";
                    System.out.println("Todos os jogadores prontos. Iniciando o jogo...");

                    for (Player player : playersConnected.values()) {
                        byte[] sendData = encodedWord.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                        serverSocket.send(sendPacket);
                    }
                } else {
                    System.out.println("Aguardando todos os jogadores estarem prontos...");
                }
            } else {
                String guessedLetter = sentence.toLowerCase();
                hm.guess(guessedLetter);

                String updatedEncodedWord = hm.getEncodedWord();

                for (Player player : playersConnected.values()) {
                    byte[] sendData = updatedEncodedWord.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                    serverSocket.send(sendPacket);
                }

                if (hm.won()) {
                    String winMessage = "Você venceu! A palavra era: " + hm.word;
                    for (Player player : playersConnected.values()) {
                        byte[] sendData = winMessage.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                        serverSocket.send(sendPacket);
                    }
                    break; 
                } else if (hm.lost()) {
                    String loseMessage = "Você perdeu! A palavra era: " + hm.word;
                    for (Player player : playersConnected.values()) {
                        byte[] sendData = loseMessage.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                        serverSocket.send(sendPacket);
                    }
                    break; 
                }
            }
        }
    }
}
