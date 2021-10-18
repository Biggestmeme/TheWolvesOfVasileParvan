package Sockets;

import jdk.jshell.spi.ExecutionControl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements ClientHandler {
    public Socket socket;
    public Thread thread;
    public PrintWriter output;
    public BufferedReader input;


    public void setSocket(Socket socket) {
        this.socket = socket;
        try {
            this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.output = new PrintWriter(this.socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //here happens the logic
    @Override
    public void call(ArrayList<Client> clients, Client client) {
        System.out.println(client.readFromClient());
        clients.remove(client);
        client.closeClient();
    }

    @Override
    public void closeClient() {
        try {

            this.socket.close();
            this.input.close();
            this.output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String readFromClient() {
        try {
            return this.input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void writeToClient(String message) {
        this.output.println(message);
    }
}
