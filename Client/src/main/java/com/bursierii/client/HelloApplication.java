package com.bursierii.client;

import com.bursierii.client.Sockets.Sockets;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.*;

import com.bursierii.client.*;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

    }

    public static void main(String[] args) {
        Socket sock = null;
        try {
            sock = new Socket("localhost",64502);
            PrintWriter out =                                            // 2nd statement
                    new PrintWriter(sock.getOutputStream(), true);

            JSONObject json = new JSONObject("{\"hello\":[\"Hey\"]}");
            System.out.println(json.toString());

            out.println(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}