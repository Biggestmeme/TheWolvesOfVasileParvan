package Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Client {
    public Socket socket;
    public Thread thread;
    public PrintWriter output;
    public BufferedReader input;
    public String clientID;


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
    public String call(Client client) {
        System.out.println(client.readFromClient());
        client.closeClient();

        return "ID";
    }

    public void closeClient() {
        try {

            this.socket.close();
            this.input.close();
            this.output.close();

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
}
