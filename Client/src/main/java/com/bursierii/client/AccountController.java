package com.bursierii.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;

public class AccountController {
    private Button updateProfileButton, backButton;

    @FXML
    public void updateProfile(ActionEvent event) {
        String id = ((Control)event.getSource()).getId();
        System.out.println(id);
        // check the filled text fields
    }
}
