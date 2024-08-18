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
            
            String playerKey = ipAddress.toString() + port;

            if (PlayerMessage.CONNECT.name().equals(sentence)) {
                System.out.println("Requisição de conexão recebida do IP: " + ipAddress + " porta: " + port);
                Player newPlayer = new Player(ipAddress, port);
                playersConnected.put(playerKey, newPlayer);
                
                String readyMessage = "Digite READY para ficar pronto para a partida";
                DatagramPacket readyMessagePacket = new DatagramPacket(readyMessage.getBytes(), readyMessage.length(), newPlayer.ipAddress, newPlayer.port);
                serverSocket.send(readyMessagePacket);

                if (playersConnected.size() == 2) {
                    System.out.println("Dois jogadores conectados. Esperando que eles estejam prontos...");
                }
            } else if (PlayerMessage.READY.name().equals(sentence)) {
                playersConnected.get(playerKey).ready();
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
                	String waitingPlayers = "Aguardando todos os jogadores estarem prontos...";
                    System.out.println(waitingPlayers);
                }
            } else if(playersConnected.size() == 2 && playersConnected.values().stream().allMatch(Player::isReady)) {
                String guessedLetter = sentence.toLowerCase();
                hm.guess(guessedLetter);

                String updatedEncodedWord = hm.getEncodedWord();

                for (Player player : playersConnected.values()) {
                    byte[] sendData = updatedEncodedWord.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                    serverSocket.send(sendPacket);
                }

                if (hm.won()) {
                    String winMessage = "Você venceu! A palavra era: " + hm.getWord();
                    for (Player player : playersConnected.values()) {
                        byte[] sendData = winMessage.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                        serverSocket.send(sendPacket);
                    }
                    break; 
                } else if (hm.lost()) {
                    String loseMessage = "Você perdeu! A palavra era: " + hm.getWord();
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
