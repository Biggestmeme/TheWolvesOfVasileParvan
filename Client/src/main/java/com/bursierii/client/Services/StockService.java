package com.bursierii.client.Services;

import com.bursierii.client.Config.Config;
import com.bursierii.client.Kafka.Kafka;
import com.bursierii.client.Sockets.Sockets;
import org.json.JSONObject;

public class StockService {
    public static String name;
    public static double price;
    public static String ticker;
    public static int quantity;
    public static Kafka kafka = new Kafka(Config.KAFKA_ADDRESS,Config.KAFKA_GROUP_ID);

    private static void setStock(JSONObject stock) {
        StockService.name = stock.getString("company");
        StockService.price = stock.getDouble("current_price");
        StockService.ticker = stock.getString("ticker");

    }

    public static JSONObject getStockInfo(String companyName) {
        Sockets client = new Sockets(Config.STOCK_SERVER_IP,Config.STOCK_SERVER_PORT);
        client.writeToClient("{action:\"Get_Stock_Info\",company:\""+companyName+"\"}");
        JSONObject response = new JSONObject(client.readFromClient());
        client.closeClient();
        System.out.println(response.toString());
        StockService.setStock(response);
        return response;
    }
}
