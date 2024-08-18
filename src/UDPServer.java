import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class UDPServer {
    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9881);
        System.out.println("UDP server rodando!");

        byte[] receivedData = new byte[1024];
        HashMap<String, Player> playersConnected = new HashMap<>();
        Hangman hm = new Hangman();
        boolean gameRunning = true; // Flag para controlar o loop principal
        List<Player> turnOrder = new ArrayList<>();
        int currentPlayerIndex = 0;
        boolean canGuessWord = false; // Flag para controlar se o jogador pode tentar adivinhar a palavra inteira

        while (gameRunning) {
            Arrays.fill(receivedData, (byte) 0);
            DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
            serverSocket.receive(receivePacket);

            String sentence = new String(receivePacket.getData()).trim();
            InetAddress ipAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            
            String playerKey = ipAddress.toString() + port;

            if (PlayerMessage.CONNECT.name().equalsIgnoreCase(sentence)) {
                System.out.println("Requisição de conexão recebida do IP: " + ipAddress + " porta: " + port);
                Player newPlayer = new Player(ipAddress, port);
                playersConnected.put(playerKey, newPlayer);
                turnOrder.add(newPlayer);

                String readyMessage = "Digite READY para ficar pronto para a partida";
                DatagramPacket readyMessagePacket = new DatagramPacket(readyMessage.getBytes(), readyMessage.length(), newPlayer.ipAddress, newPlayer.port);
                serverSocket.send(readyMessagePacket);

                if (playersConnected.size() == 2) {
                    System.out.println("Dois jogadores conectados. Esperando que eles estejam prontos...");
                }
            } else if (PlayerMessage.READY.name().equalsIgnoreCase(sentence)) {
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

                    notifyTurn(serverSocket, turnOrder.get(currentPlayerIndex));
                } else {
                    String waitingPlayers = "Aguardando todos os jogadores estarem prontos...";
                    System.out.println(waitingPlayers);
                }
            } else if (playersConnected.size() == 2 && playersConnected.values().stream().allMatch(Player::isReady)) {
                Player currentPlayer = turnOrder.get(currentPlayerIndex);
                if (!playerKey.equals(currentPlayer.ipAddress.toString() + currentPlayer.port)) {
                    String notYourTurnMessage = "Não é a sua vez! Aguarde o outro jogador.";
                    DatagramPacket notYourTurnPacket = new DatagramPacket(notYourTurnMessage.getBytes(), notYourTurnMessage.length(), ipAddress, port);
                    serverSocket.send(notYourTurnPacket);
                    continue;
                }

                // Se já pode adivinhar a palavra inteira, perguntar ao jogador o que ele quer fazer
                if (canGuessWord) {
                    if ("1".equals(sentence)) {
                        String guessWordMessage = "Digite a palavra inteira:";
                        DatagramPacket guessWordPacket = new DatagramPacket(guessWordMessage.getBytes(), guessWordMessage.length(), ipAddress, port);
                        serverSocket.send(guessWordPacket);
                    } else if ("2".equals(sentence)) {
                        String guessLetterMessage = "Digite uma letra:";
                        DatagramPacket guessLetterPacket = new DatagramPacket(guessLetterMessage.getBytes(), guessLetterMessage.length(), ipAddress, port);
                        serverSocket.send(guessLetterPacket);
                    } else if (sentence.length() > 1) {
                        // Tentativa de adivinhar a palavra inteira
                        if (hm.getWord().equalsIgnoreCase(sentence)) {
                            String winMessage = "Você venceu! A palavra era: " + hm.getWord();
                            for (Player player : playersConnected.values()) {
                                byte[] sendData = winMessage.getBytes();
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                                serverSocket.send(sendPacket);
                            }
                            gameRunning = false; // Termina o loop do jogo
                        } else {
                            String wrongWordMessage = "Palavra incorreta! Você perdeu!";
                            for (Player player : playersConnected.values()) {
                                byte[] sendData = wrongWordMessage.getBytes();
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                                serverSocket.send(sendPacket);
                            }
                            gameRunning = false; // Termina o loop do jogo
                        }
                    } else if (sentence.length() == 1) {
                        // Tentativa de adivinhar uma letra
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
                            gameRunning = false; // Termina o loop do jogo
                        } else if (hm.lost()) {
                            String loseMessage = "Você perdeu! A palavra era: " + hm.getWord();
                            for (Player player : playersConnected.values()) {
                                byte[] sendData = loseMessage.getBytes();
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                                serverSocket.send(sendPacket);
                            }
                            gameRunning = false; // Termina o loop do jogo
                        } else {
                            currentPlayerIndex = (currentPlayerIndex + 1) % turnOrder.size();
                            notifyTurn(serverSocket, turnOrder.get(currentPlayerIndex));
                        }
                    } else {
                        String invalidInputMessage = "Entrada inválida! Por favor, tente novamente.";
                        DatagramPacket invalidInputPacket = new DatagramPacket(invalidInputMessage.getBytes(), invalidInputMessage.length(), ipAddress, port);
                        serverSocket.send(invalidInputPacket);
                    }
                } else if (sentence.length() == 1 && Character.isLetter(sentence.charAt(0))) {
                    String guessedLetter = sentence.toLowerCase();
                    hm.guess(guessedLetter);

                    String updatedEncodedWord = hm.getEncodedWord();

                    for (Player player : playersConnected.values()) {
                        byte[] sendData = updatedEncodedWord.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                        serverSocket.send(sendPacket);
                    }

                    canGuessWord = true; // Permitir a tentativa de adivinhar a palavra inteira a partir da próxima rodada

                    if (hm.won()) {
                        String winMessage = "Você venceu! A palavra era: " + hm.getWord();
                        for (Player player : playersConnected.values()) {
                            byte[] sendData = winMessage.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                            serverSocket.send(sendPacket);
                        }
                        gameRunning = false; // Termina o loop do jogo
                    } else if (hm.lost()) {
                        String loseMessage = "Você perdeu! A palavra era: " + hm.getWord();
                        for (Player player : playersConnected.values()) {
                            byte[] sendData = loseMessage.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, player.ipAddress, player.port);
                            serverSocket.send(sendPacket);
                        }
                        gameRunning = false; // Termina o loop do jogo
                    } else {
                        currentPlayerIndex = (currentPlayerIndex + 1) % turnOrder.size();
                        notifyTurn(serverSocket, turnOrder.get(currentPlayerIndex));
                    }
                } else {
                    String invalidInputMessage = "Entrada inválida! Por favor, envie apenas uma letra.";
                    DatagramPacket invalidInputPacket = new DatagramPacket(invalidInputMessage.getBytes(), invalidInputMessage.length(), ipAddress, port);
                    serverSocket.send(invalidInputPacket);
                }
            }
        }

        // Fechar a conexão após o término do jogo
        serverSocket.close();
        System.out.println("Jogo encerrado, conexão fechada.");
    }

    private static void notifyTurn(DatagramSocket serverSocket, Player player) throws Exception {
        String yourTurnMessage = "Sua vez de jogar! Digite 1 para chutar a palavra ou 2 para chutar uma letra.";
        DatagramPacket yourTurnPacket = new DatagramPacket(yourTurnMessage.getBytes(), yourTurnMessage.length(), player.ipAddress, player.port);
        serverSocket.send(yourTurnPacket);
    }
}
