package com.bursierii.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Control;

public class DashboardController {
    @FXML
    public void goToStockDetail(ActionEvent event){
        String id = ((Control)event.getSource()).getId();
        String company = id.substring(0, id.length()-7);
        System.out.println(company);
    }

}
