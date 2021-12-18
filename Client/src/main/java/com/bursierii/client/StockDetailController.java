package com.bursierii.client;

import com.bursierii.client.Encryption.SHA256;
import com.bursierii.client.Services.StockService;
import com.bursierii.client.Services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StockDetailController {
    @FXML
    private Text stockName;

    @FXML
    private Text price;


    @FXML
    public void initialize() {
        stockName.setText(StockService.name);
        price.setText(String.valueOf(StockService.price));
    }

    @FXML
    public void Buy(ActionEvent actionEvent) {
        //adauga checkuri daca poate sa vanda sau sa cumpere

        //ne prefacem ca avem field pt cantitate
        int quantity = 10;
        String buyRequest = createBuyRequest(quantity,StockService.price);
        StockService.kafka.sendMessage(StockService.ticker+"-receiver", SHA256.encryptString(buyRequest),buyRequest);
        System.out.println("Send Buy Request");
    }

    @FXML
    public void Sell(ActionEvent actionEvent) {
        //adauga checkuri daca poate sa vanda sau sa cumpere

        //ne prefacem ca avem field pt cantitate
        int quantity =10;
        String buyRequest = createSellRequest(quantity,StockService.price);
        StockService.kafka.sendMessage(StockService.ticker+"-receiver", SHA256.encryptString(buyRequest),buyRequest);
    }

    public String createBuyRequest(int quantity, double price) {
        return "{action:\"Buy_Order\",company:\""+StockService.name+"\",ticker:\""+StockService.ticker+"\",amount:"+quantity+",price:"+price+",timestamp:\""+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())+"\",user:\""+UserService.userID+"\"}";
    }

    public String createSellRequest(int quantity, double price) {
        return "{action:\"Sell_Order\",company:\""+StockService.name+"\",ticker:\""+StockService.ticker+"\",amount:"+quantity+",price:"+price+",timestamp:\""+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())+"\",user:\""+UserService.userID+"\"}";
    }

    public void goToProfile(ActionEvent actionEvent) {
        try{
            UserService.getProfile();
            Stage stage = (Stage)stockName.getScene().getWindow();
            Parent viewClientPage = FXMLLoader.load(getClass().getResource("account.fxml"));
            Scene scene = new Scene(viewClientPage);
            stage.setScene(scene);
            stage.show();

        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @FXML
    public void goBack() throws IOException {
        StockService.resetStock();
        Stage stage = (Stage)stockName.getScene().getWindow();
        Parent viewClientPage = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
        Scene scene = new Scene(viewClientPage);
        stage.setScene(scene);
        stage.show();
    }
}
