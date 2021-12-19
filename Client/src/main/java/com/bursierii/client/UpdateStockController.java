package com.bursierii.client;

import com.bursierii.client.Encryption.SHA256;
import com.bursierii.client.Services.StockService;
import com.bursierii.client.Services.UpdateStockService;
import com.bursierii.client.Services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UpdateStockController {

    @FXML
    private TextField updatePrice;

    @FXML
    private TextField updateAmount;

    @FXML
    public void initialize() {
        updatePrice.setText(String.valueOf(UpdateStockService.price));
        updateAmount.setText(String.valueOf(UpdateStockService.amount));

    }

    @FXML
    public void goBack() throws IOException {
        StockService.resetStock();
        Stage stage = (Stage)updatePrice.getScene().getWindow();
        Parent viewClientPage = FXMLLoader.load(getClass().getResource("account.fxml"));
        Scene scene = new Scene(viewClientPage);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void Update() {
        String updateRequest = createBuyRequest(Integer.parseInt(updateAmount.getText()),Double.parseDouble(updatePrice.getText()),UpdateStockService.timestamp);
        JSONObject updateRequestjson = new JSONObject("{action:\"Update_Order\",updating_order:"+updateRequest+"}");
        System.out.println("SENDING UPDATE REQUEST : "+updateRequestjson.toString());
        StockService.kafka.sendMessage(StockService.ticker+"-receiver", SHA256.encryptString(updateRequestjson.toString()),updateRequestjson.toString());
        goToProfile();
    }
    private void goToProfile() {
        try{
            UserService.getProfile();
            Stage stage = (Stage)updatePrice.getScene().getWindow();
            Parent viewClientPage = FXMLLoader.load(getClass().getResource("account.fxml"));
            Scene scene = new Scene(viewClientPage);
            stage.setScene(scene);
            stage.show();

        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
    public String createBuyRequest(int quantity, double price,String timestamp) {
        return "{action:\""+UpdateStockService.action+"\",company:\""+StockService.name+"\",ticker:\""+StockService.ticker+"\",amount:"+quantity+",price:"+price+",timestamp:\""+timestamp+"\",user:\""+ UserService.userID+"\"}";
    }
}
