package com.bursierii.client.Sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

public class Sockets implements Runnable {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private int PORT;
    private String IPADDRESS;


    public void run() {

    }



    public void connectToServer(String IPADDRESS,int PORT) {
        try {
            this.clientSocket = new Socket(IPADDRESS,PORT);
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch(IOException sockE) {
            System.out.println(sockE);
        }
    }

    public void createServer(int PORT, Object clientHandler) {
        try {
            Socket connectedClients;
            this.serverSocket = new ServerSocket(PORT);
            while(true) {
                connectedClients = this.serverSocket.accept();
                PrintWriter out = new PrintWriter(connectedClients.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(connectedClients.getInputStream()));
                Thread clientThread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            String a = in.readLine();
                            System.out.println("Client says : " + a );
                        } catch(IOException e) {
                            System.out.println(e);
                        }
                    }
                });
                clientThread.start();
            }

        } catch(IOException sockE) {
            System.out.println(sockE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
            if(this.clientSocket != null) {
                this.clientSocket.close();
            }
        } catch(IOException sockE) {
            System.out.println(sockE);
        } finally {
            this.closeIO();
        }
    }

    private void createServerSocket(int PORT)   {
        try {
            this.serverSocket = new ServerSocket(PORT);
        } catch(IOException sockE) {
            System.out.println(sockE);
        }
    }

    private void closeIO() {
        try {
            this.out.close();
            this.in.close();
        } catch(IOException ioe) {
            System.out.println(ioe);
        }
    }
}