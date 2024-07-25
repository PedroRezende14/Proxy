package Sock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void start() throws IOException {
        clientSocket = new Socket(SERVER_ADDRESS, Server.PORT);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println("A conexão com o servidor foi realizada " + SERVER_ADDRESS + ":" + Server.PORT);
    }

    private void requisicao() throws IOException {
        String req;
        Scanner ent = new Scanner(System.in);
        do {
            System.out.println("Aguardando requisicao:");
            req = ent.nextLine();
            out.println(req);

            // Receber e imprimir a resposta do servidor
            String response = in.readLine();
            if (response != null) {
                System.out.println("Resposta do servidor: " + response);
            }

        } while (!req.equalsIgnoreCase("sair"));
        ent.close();
        out.close();
        in.close();
        clientSocket.close();
    }

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.start();
            client.requisicao();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
