package com.bursierii.client.Sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Sockets {
    private Socket serverSocket;
    private PrintWriter output;
    private BufferedReader input;

    public Sockets(String IP, int PORT) {
        try {
            this.serverSocket = new Socket(IP,PORT);
            this.setSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setSocket() {
        try {
            this.input = new BufferedReader(new InputStreamReader(this.serverSocket.getInputStream()));
            this.output = new PrintWriter(this.serverSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readFromClient() {
        try {
            return this.input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeToClient(String message) {
        this.output.println(message);
    }

    public void closeClient() {
        try {
            this.input.close();
            this.output.close();
            this.serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
