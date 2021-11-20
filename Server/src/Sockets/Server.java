package Sockets;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private int PORT;
    private ServerSocket server;
    private ArrayList<Client> clients = new ArrayList<Client>();

    public Server(int PORT, ClientHandler handler) {
        this.PORT = PORT;
        this.startServer(handler);
    }

    private void startServer(ClientHandler handler) {
        try {
            server = new ServerSocket(this.PORT);

            while (true) {
                Client client = new Client();
                client.setSocket(this.server.accept());
                client.thread = new Thread(new Runnable() {
                    public void run() {
                        handler.call(clients,client);
                    }
                });
                client.thread.start();
                clients.add(client);
            }
        } catch(IOException sockE) {
            System.out.println(sockE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeMultipleClients(this.clients);
        }
    }

    private void closeMultipleClients(ArrayList<Client> clients) {
        for (Client client : clients) {
            this.closeClient(client);
            clients.remove(client);
        }
    }

    private void closeClient(Client client) {
        try {
            client.socket.close();
            client.input.close();
            client.output.close();
            client.thread.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
