package com.bursierii.client;

import com.bursierii.client.Config.Config;
import com.bursierii.client.Encryption.SHA256;
import com.bursierii.client.Services.StockService;
import com.bursierii.client.Services.UpdateStockService;
import com.bursierii.client.Services.UserService;
import com.bursierii.client.Sockets.Sockets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
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

    @FXML
    private TableView<Stock> pendingTable;

    @FXML
    private TableColumn<Stock, String> pendingTicker;

    @FXML
    private TableColumn<Stock, String> pendingCompany;

    @FXML
    private TableColumn<Stock, Integer> pendingAmount;

    @FXML
    private TableColumn<Stock, Integer> pendingPrice;

    @FXML
    private TableColumn<Stock, String> pendingDate;

    @FXML
    private TableColumn<Stock, String> pendingAction;

    ObservableList<Stock> list;
    ObservableList<Stock> pendinglist;

    @FXML
    public void initialize() {
        ArrayList<Stock> listdata = new ArrayList<Stock>();
        if (UserService.owned_stocks != null) {
            for (int i=0;i<UserService.owned_stocks.length();i++){
                JSONObject temp = UserService.owned_stocks.getJSONObject(i);
                listdata.add(new Stock(temp.getString("ticker"),temp.getString("company"),temp.getInt("amount"),temp.getDouble("price"),temp.getString("timestamp"),""));
            }
        }
        list = FXCollections.observableArrayList(listdata);

        ArrayList<Stock> pendinglistdata = new ArrayList<Stock>();
        if (UserService.pending_orders != null) {
            for (int i=0;i<UserService.pending_orders.length();i++){
                JSONObject temp = UserService.pending_orders.getJSONObject(i);
                System.out.println("PENDING : "+temp.toString());
                pendinglistdata.add(new Stock(temp.getString("ticker"),temp.getString("company"),temp.getInt("amount"),temp.getDouble("price"),temp.getString("timestamp"),temp.getString("action")));
            }
        }
        pendinglist = FXCollections.observableArrayList(pendinglistdata);

        firstName.setText(UserService.firstName);
        lastName.setText(UserService.lastName);
        moneyInDaBank.setText(UserService.balance + "$");

        Ticker.setCellValueFactory(new PropertyValueFactory<Stock,String>("Ticker"));
        Company.setCellValueFactory(new PropertyValueFactory<Stock,String>("Company"));
        Amount.setCellValueFactory(new PropertyValueFactory<Stock,Integer>("Amount"));
        Price.setCellValueFactory(new PropertyValueFactory<Stock,Integer>("Price"));
        Date.setCellValueFactory(new PropertyValueFactory<Stock,String>("Date"));

        pendingTicker.setCellValueFactory(new PropertyValueFactory<Stock,String>("Ticker"));
        pendingCompany.setCellValueFactory(new PropertyValueFactory<Stock,String>("Company"));
        pendingAmount.setCellValueFactory(new PropertyValueFactory<Stock,Integer>("Amount"));
        pendingPrice.setCellValueFactory(new PropertyValueFactory<Stock,Integer>("Price"));
        pendingDate.setCellValueFactory(new PropertyValueFactory<Stock,String>("Date"));

        stocksTable.setItems(list);
        pendingTable.setItems(pendinglist);

        //pendingTable.getSelectionModel().getSelectedItem().getPrice()
        setDoubleClickActionOnTable(pendingTable);

    }


    private void setDoubleClickActionOnTable(TableView<Stock> table) {
        table.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    UpdateStockService.setUpdateStock(table.getSelectionModel().getSelectedItem().getTicker(),
                            table.getSelectionModel().getSelectedItem().getAction(),
                            table.getSelectionModel().getSelectedItem().getCompany(),
                            UserService.userID,
                            table.getSelectionModel().getSelectedItem().getDate(),
                            table.getSelectionModel().getSelectedItem().getAmount(),
                            table.getSelectionModel().getSelectedItem().getPrice());


                    System.out.println("ASDFASDFAS : " + UpdateStockService.getJSON());
                    JSONObject kafkaRequest = new JSONObject("{action:\"Pending_Update\",updating_order:"+UpdateStockService.getJSON().toString()+"}");

                    StockService.kafka.sendMessage(UpdateStockService.ticker+"-receiver", SHA256.encryptString(kafkaRequest.toString()),kafkaRequest.toString());
                    System.out.println("Send pending update order");
                    Stage stage = (Stage)firstName.getScene().getWindow();
                    Parent viewClientPage = null;
                    try {
                        viewClientPage = FXMLLoader.load(getClass().getResource("updateStock.fxml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                    Scene scene = new Scene(viewClientPage);
                    stage.setScene(scene);
                    stage.show();
                }
            }
        });
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