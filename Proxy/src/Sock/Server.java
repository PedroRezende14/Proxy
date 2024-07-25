package Sock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

    public static final int PORT = 6000;
    private ServerSocket serverSocket;
    private ConcurrentHashMap<Integer, String> cache = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int CACHE_EXPIRATION_TIME = 60; // seconds

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciado na porta " + PORT);
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buscarDados(int id) {
        if (cache.containsKey(id)) {
            System.out.println("Dados obtidos do cache");
            return cache.get(id);
        } else {
            System.out.println("Dados obtidos do banco de dados");
            String dados = Sock.BancodeDados.consultarBD(id);
            if (dados != null) {
                cache.put(id, dados);
                scheduler.schedule(() -> cache.remove(id), CACHE_EXPIRATION_TIME, TimeUnit.SECONDS);
            }
            return dados;
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                System.out.println("Cliente " + clientSocket.getRemoteSocketAddress() + " realizou uma conexão");
                String msg;
                while ((msg = in.readLine()) != null) {
                    int id = Integer.parseInt(msg); // Supondo que a mensagem é o ID
                    String dados = buscarDados(id);
                    out.println("Dados para o ID " + id + ": " + dados);
                    System.out.println("Dados para o ID " + id + ": " + dados);
                }
            } catch (IOException e) {
                System.err.println("Erro ao receber a mensagem: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar a conexão: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        Sock.BancodeDados.conectar();
        Server serv = new Server();
        serv.start();
    }
}
