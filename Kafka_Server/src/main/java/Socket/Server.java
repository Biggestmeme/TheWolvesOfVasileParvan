package Socket;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Server<T extends Client> {

    private int PORT;
    private ServerSocket server;
    private ConcurrentHashMap<String,Client> clients = new ConcurrentHashMap<String,Client>();

    public Server(int PORT, ClientHandler handler) {
        this.PORT = PORT;
        this.startServer(handler);
    }

    private void startServer(ClientHandler handler) {
        try {
            server = new ServerSocket(this.PORT);

            while(true) {

                Client client = (T) new Client(); // create client
                client.clientID = UUID.randomUUID().toString(); // create random uuid
                client.setSocket(this.server.accept()); // set accepted socket
                clients.put(client.clientID,client); // place client in concurent hash map
                client.thread = new Thread(new Runnable() { // start thread for client
                    public void run() {
                        handler.handle(client); // handle whatever
                        client.closeClient(); // close client
                        clients.remove(client.clientID); // remove him from the lcient list
                        System.out.println("Client " + client.clientID +" removed");
                    }
                });
                System.out.println("Client " + client.clientID + " connected");
                client.thread.start(); // start thread;


            }
        } catch(IOException sockE) {
            System.out.println(sockE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeMultipleClients(this.clients);
        }
    }

    private void closeMultipleClients(ConcurrentHashMap<String,Client> clients) {
        for(Client client : clients.values()) {
            this.closeClient(client);
            clients.remove(client.clientID);
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
