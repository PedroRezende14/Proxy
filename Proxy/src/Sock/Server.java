package Sock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

    public static final int PORT = 6000;
    private static final String DB_ADDRESS = "127.0.0.1";
    private static final int DB_PORT = 7000;
    private ServerSocket serverSocket;
    private ConcurrentHashMap<Integer, String> cache = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int CACHE_EXPIRATION_TIME = 30; 

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
            System.out.print("Dados do cache: ");
            return cache.get(id);
        } else {
            System.out.print("Dados do banco de dados: ");
            String dados = consultarBancoRemoto(id);
            if (dados != null) {
                cache.put(id, dados);
                scheduler.schedule(() -> cache.remove(id), CACHE_EXPIRATION_TIME, TimeUnit.SECONDS);
            }
            return dados;
        }
    }

    private String consultarBancoRemoto(int id) {
        String resposta = null;
        try (Socket socket = new Socket(DB_ADDRESS, DB_PORT);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(id);
            resposta = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resposta;
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
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
                System.out.println("Cliente " + clientSocket.getRemoteSocketAddress() + " realizou uma conexão");
                String msg;
                while ((msg = in.readLine()) != null) {
                    int id = Integer.parseInt(msg); 
                    String dados = buscarDados(id);
                    out.println("ID " + id + ": " + dados);
                    System.out.println("ID " + id + ": " + dados);
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
        Server serv = new Server();
        serv.start();
    }
}
