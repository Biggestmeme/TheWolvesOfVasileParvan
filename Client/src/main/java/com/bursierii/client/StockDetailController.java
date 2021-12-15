package com.bursierii.client;

import com.bursierii.client.Encryption.SHA256;
import com.bursierii.client.Services.StockService;
import com.bursierii.client.Services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

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
        int quantity = 4;
        String buyRequest = createBuyRequest(quantity,StockService.price);
        StockService.kafka.sendMessage(StockService.ticker+"-receiver", SHA256.encryptString(buyRequest),buyRequest);
    }

    @FXML
    public void Sell(ActionEvent actionEvent) {
        //adauga checkuri daca poate sa vanda sau sa cumpere

        //ne prefacem ca avem field pt cantitate
        int quantity =8;
        String buyRequest = createSellRequest(quantity,StockService.price);
        StockService.kafka.sendMessage(StockService.ticker+"-receiver", SHA256.encryptString(buyRequest),buyRequest);
    }

    public String createBuyRequest(int quantity, double price) {
        return "{action:\"Buy_Order\",company:\""+StockService.name+"\",ticker:\""+StockService.ticker+"\",amount:"+quantity+",price:"+price+",timestamp:\""+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())+"\",user:\""+UserService.userID+"\"}";
    }

    public String createSellRequest(int quantity, double price) {
        return "{action:\"Sell_Order\",company:\""+StockService.name+"\",ticker:\""+StockService.ticker+"\",amount:"+quantity+",price:"+price+",timestamp:\""+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())+"\",user:\""+UserService.userID+"\"}";
    }


}
