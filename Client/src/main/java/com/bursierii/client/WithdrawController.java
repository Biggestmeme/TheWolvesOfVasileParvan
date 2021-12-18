package com.bursierii.client;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class WithdrawController {

    @FXML
    private Label expireDate;

    @FXML
    private Label CVVcode;

    @FXML
    private TextField withdrawAmount;

    @FXML
    public void goBack() throws IOException {
        Stage stage = (Stage)expireDate.getScene().getWindow();
        Parent viewClientPage = FXMLLoader.load(getClass().getResource("account.fxml"));
        Scene scene = new Scene(viewClientPage);
        stage.setScene(scene);
        stage.show();
    }
}
