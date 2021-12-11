package com.bursierii.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    public void Login(ActionEvent event) {
        try{
            Stage stage = (Stage)username.getScene().getWindow();
            Parent viewClientPage = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
            Scene scene = new Scene(viewClientPage);
            stage.setScene(scene);
            stage.show();

        }catch (IOException ex){
            ex.printStackTrace();
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

}
