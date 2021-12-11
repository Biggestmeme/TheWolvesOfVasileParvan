package com.bursierii.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Label title;

    @FXML
    public void goToStockDetail(ActionEvent event){
        String id = ((Control)event.getSource()).getId();
        String company = id.substring(0, id.length()-7);
        System.out.println(company);

        try{
            Stage stage = (Stage)title.getScene().getWindow();
            Parent viewClientPage = FXMLLoader.load(getClass().getResource("stockDetail.fxml"));
            Scene scene = new Scene(viewClientPage);
            stage.setScene(scene);
            stage.show();

        }catch (IOException ex){
            ex.printStackTrace();
        }

    }

}
