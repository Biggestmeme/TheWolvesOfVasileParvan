package Sockets;

import java.util.ArrayList;

public interface ClientHandler {
    //You are responsible to remove the client from the socket
    void call(ArrayList<Client> clients, Client client);
    void closeClient();
    void writeToClient(String message);
    String readFromClient();

}
