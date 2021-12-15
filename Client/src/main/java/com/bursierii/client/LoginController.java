package com.bursierii.client;

import com.bursierii.client.Config.Config;
import com.bursierii.client.Encryption.SHA256;
import com.bursierii.client.Services.UserService;
import com.bursierii.client.Sockets.Sockets;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Base64;

public class LoginController {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    public void Login(ActionEvent event) {
        Sockets client = new Sockets(Config.AUTH_SERVER_IP,Config.AUTH_SERVER_PORT);

        //aici se creeaza requestul
        String builtLoginReq = createLoginRequest(username.getText(),password.getText());
        //aici trimite requestul de login
        client.writeToClient(builtLoginReq);

        JSONObject loginResponse = null;
        try {
            //aici primeste raspunsul servarului
            loginResponse = new JSONObject(client.readFromClient());
            //verifica daca e eroare
            if(checkIfLoginResponseIsError(loginResponse)) {
                //daca da, o afiseaza si iese din metoda
                username.setText(loginResponse.getString("message"));
                client.closeClient();
                return;
            }

            UserService.setUser(loginResponse);

            //schimba scena
            Stage stage = (Stage)username.getScene().getWindow();
            Parent viewClientPage = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
            Scene scene = new Scene(viewClientPage);
            stage.setScene(scene);
            stage.show();




            System.out.println(loginResponse.toString());
        } catch(JSONException | IOException e) {
            client.closeClient();
            username.setText("Unexpected Error");
        }

    }

    @FXML
    public void Signup(ActionEvent event){


        try{
            Stage stage = (Stage)username.getScene().getWindow();
            Parent viewClientPage = FXMLLoader.load(getClass().getResource("signup.fxml"));
            Scene scene = new Scene(viewClientPage);
            stage.setScene(scene);
            stage.show();

        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private String createLoginRequest(String username, String password) {
        if(username.length() == 0 || password.length() == 0) {
            return "{}";
        }
        return "{action:\"login\",username:"+username+",password:\""+ Base64.getEncoder().encodeToString(SHA256.encryptString(password).getBytes()) +"\"}";
    }



    private boolean checkIfLoginResponseIsError(JSONObject response) {
        try {
            response.getString("message");
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

}
