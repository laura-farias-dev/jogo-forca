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
        HashMap<InetAddress, Player> playersConnected = new HashMap<InetAddress, Player>();
        Hangman hm = new Hangman();

        while (true) {
            Arrays.fill(receivedData, (byte) 0);
            DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
            serverSocket.receive(receivePacket);

            String sentence = new String(receivePacket.getData()).trim();
            InetAddress ipAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            if (PlayerMessage.valueOf(sentence) == PlayerMessage.CONNECT) {
                System.out.println("Connection request received from ip: " + ipAddress + " port: " + port);
                playersConnected.put(ipAddress, new Player(ipAddress, port));

                if (playersConnected.size() == 2) {
                    System.out.println("Dois jogadores conectados. Esperando que eles estejam prontos...");
                }
            } else if (PlayerMessage.valueOf(sentence) == PlayerMessage.READY) {
                playersConnected.get(ipAddress).ready();
                System.out.println("Player with IP " + ipAddress + " marked as ready");

                if (playersConnected.size() == 2 && playersConnected.values().stream().allMatch(Player::isReady)) {
                    String encodedWord = hm.getEncodedWord();
                    System.out.println("Todos os jogadores prontos. Iniciando o jogo...");
                    System.out.println("Palavra para adivinhar: " + encodedWord);

                    for (Player player : playersConnected.values()) {
                        byte[] sendData = encodedWord.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                        serverSocket.send(sendPacket);
                    }
                } else {
                    System.out.println("Aguardando o segundo jogador estar pronto...");
                }
            } else {
                // Presume que a mensagem recebida é uma letra de adivinhação
                String guessedLetter = sentence.toLowerCase();
                hm.guess(guessedLetter);

                // Atualiza a palavra codificada após a adivinhação
                String updatedEncodedWord = hm.getEncodedWord();

                // Envia a palavra atualizada para todos os jogadores
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

            String packetAck = sentence + " " + "ACK";
            byte[] sendData = packetAck.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
            serverSocket.send(sendPacket);
        }
    }
}
