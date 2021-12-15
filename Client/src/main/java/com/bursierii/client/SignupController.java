package com.bursierii.client;

import com.bursierii.client.Config.Config;
import com.bursierii.client.Encryption.SHA256;
import com.bursierii.client.Sockets.Sockets;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.Base64;

public class SignupController {
    @FXML
    private TextField fnameInput;

    @FXML
    private TextField lnameInput;

    @FXML
    private TextField usernameInput;

    @FXML
    private PasswordField passwordInput;

    @FXML
    private PasswordField confirmPassInput;



    @FXML
    public void onSignup(ActionEvent event){
        Sockets client = new Sockets(Config.AUTH_SERVER_IP,Config.AUTH_SERVER_PORT);
        //add check here
        String registerRequest = createRegisterRequest(fnameInput.getText(),lnameInput.getText(),usernameInput.getText(),"asd@asd.asd",passwordInput.getText());
        client.writeToClient(registerRequest);
        JSONObject response;
        try {
            response = new JSONObject(registerRequest);
            if(checkIfRegisterResponseIsError(response)) {
                fnameInput.setText(response.getString("message"));
                return;
            }

            fnameInput.setText("Registered");
        }catch(JSONException e) {


        }
    }

    @FXML
    public void goBack(ActionEvent event){
        System.out.println("goBack");
    }


    private String createRegisterRequest(String firstName, String lastName,String username,String email, String password) {
        if(username.length() == 0 || password.length() == 0) {
            return "{}";
        }

        return "{action:\"register\",firstName:"+firstName+",lastName:"+lastName+",email:"+email+",username:"+username+",password:\""+ Base64.getEncoder().encodeToString(SHA256.encryptString(password).getBytes()) +"\"}";

    }

    private boolean checkIfRegisterResponseIsError(JSONObject response) {
        try {
            response.getString("message");
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
}
