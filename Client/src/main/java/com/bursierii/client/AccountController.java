package com.bursierii.client;

import com.bursierii.client.Services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.util.ArrayList;

public class AccountController {
//    ArrayList<JSONObject> myStocks = new ArrayList<>();
    private Button updateProfileButton, backButton;

    @FXML
    private Label firstName;

    @FXML
    private Label lastName;

    @FXML
    private Label moneyInDaBank;

    @FXML
    private TableView<Stock> stocksTable;

    @FXML
    private TableColumn<Stock, String> Ticker;

    @FXML
    private TableColumn<Stock, String> Company;

    @FXML
    private TableColumn<Stock, Integer> Amount;

    @FXML
    private TableColumn<Stock, Integer> Price;

    @FXML
    private TableColumn<Stock, String> Date;

    ObservableList<Stock> list = FXCollections.observableArrayList(
            new Stock("appl","Apple",5,10,"17-12-2021"),
            new Stock("goog","Google",3,7,"17-12-2021"),
            new Stock("fb","Facebook",2,23,"17-12-2021")
    );

    @FXML
    public void initialize() {
        firstName.setText(UserService.firstName);
        lastName.setText(UserService.lastName);
        moneyInDaBank.setText(UserService.balance + "$");

        Ticker.setCellValueFactory(new PropertyValueFactory<Stock,String>("Ticker"));
        Company.setCellValueFactory(new PropertyValueFactory<Stock,String>("Company"));
        Amount.setCellValueFactory(new PropertyValueFactory<Stock,Integer>("Amount"));
        Price.setCellValueFactory(new PropertyValueFactory<Stock,Integer>("Price"));
        Date.setCellValueFactory(new PropertyValueFactory<Stock,String>("Date"));

        stocksTable.setItems(list);

    }

    @FXML
    public void updateProfile(ActionEvent event) {
        String id = ((Control)event.getSource()).getId();
        System.out.println(id);
        // check the filled text fields
    }

    @FXML
    public void updateStock(){
        System.out.println(stocksTable.getSelectionModel().getSelectedCells());
    }

    @FXML
    public void goBack() throws IOException {
        Stage stage = (Stage)firstName.getScene().getWindow();
        Parent viewClientPage = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
        Scene scene = new Scene(viewClientPage);
        stage.setScene(scene);
        stage.show();
    }


    @FXML
    public void goToDeposit(ActionEvent event) throws IOException {
        Stage stage = (Stage)firstName.getScene().getWindow();
        Parent viewClientPage = FXMLLoader.load(getClass().getResource("deposit.fxml"));
        Scene scene = new Scene(viewClientPage);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void goToWithdraw(ActionEvent event) throws IOException {
        Stage stage = (Stage)firstName.getScene().getWindow();
        Parent viewClientPage = FXMLLoader.load(getClass().getResource("withdraw.fxml"));
        Scene scene = new Scene(viewClientPage);
        stage.setScene(scene);
        stage.show();
    }
}