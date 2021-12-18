package com.bursierii.client.Services;

import org.json.JSONArray;
import org.json.JSONObject;

public class UpdateStockService {
    public static String ticker;
    public static String action;
    public static String company;
    public static String user;
    public static String timestamp;
    public static int amount;
    public static double price;

    public static void setUpdateStock(String ticker,String action,String company,String user,String timestamp,int amount,double price) {
        UpdateStockService.ticker = ticker;
        UpdateStockService.action = action;
        UpdateStockService.company = company;
        UpdateStockService.user = user;
        UpdateStockService.timestamp = timestamp;
        UpdateStockService.amount = amount;
        UpdateStockService.price = price;
    }

    public static JSONObject getJSON() {
        return new JSONObject("{action:\""+UpdateStockService.action+"\"," +
                "ticker:\""+UpdateStockService.ticker+"\"," +
                "company:\""+UpdateStockService.company+"\"," +
                "user:\""+UpdateStockService.user+"\"," +
                "timestamp:\""+UpdateStockService.timestamp+"\"," +
                "amount:\""+UpdateStockService.amount+"\"," +
                "price:\""+UpdateStockService.price+"\"" + "}");
    }
}
