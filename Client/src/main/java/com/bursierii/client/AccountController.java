package com.bursierii.client;

import com.bursierii.client.Services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;

public class AccountController {
    private Button updateProfileButton, backButton;

    @FXML
    private Label firstName;

    @FXML
    private Label lastName;

    @FXML
    private Label moneyInDaBank;

    @FXML
    public void initialize() {
        firstName.setText(UserService.firstName);
        lastName.setText(UserService.lastName);
        moneyInDaBank.setText(UserService.balance + "$");
    }

    @FXML
    public void updateProfile(ActionEvent event) {
        String id = ((Control)event.getSource()).getId();
        System.out.println(id);
        // check the filled text fields
    }
}
