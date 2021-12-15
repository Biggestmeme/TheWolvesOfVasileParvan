package com.bursierii.client.Services;

import org.json.JSONArray;
import org.json.JSONObject;

public class UserService {

    // Declaring a variable of type String
    public static String firstName;
    public static String lastName;
    public static String username;
    public static String email;
    public static String userID;
    public static double balance;
    public static JSONArray owned_stocks;
    public static JSONArray pending_buying_stocks;

    public static void setUser(JSONObject user) {
        UserService.username = user.getString("username");
        UserService.email = user.getString("email");
        UserService.firstName = user.getString("firstName");
        UserService.lastName = user.getString("lastName");
        UserService.userID = user.getJSONObject("_id").getString("$oid");
        UserService.balance = user.getDouble("balance");
    }

}
