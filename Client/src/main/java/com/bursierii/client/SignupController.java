package com.bursierii.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class SignupController {



    @FXML
    public void onSignup(ActionEvent event){
        System.out.println("onSignup");
    }

    @FXML
    public void goBack(ActionEvent event){
        System.out.println("goBack");
    }

}
