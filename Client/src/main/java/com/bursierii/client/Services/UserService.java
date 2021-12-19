package com.bursierii.client.Services;

import com.bursierii.client.Config.Config;
import com.bursierii.client.Sockets.Sockets;
import org.json.JSONArray;
import org.json.JSONException;
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
    public static JSONArray pending_orders;
    public static JSONArray transaction_history;

    public static void setUser(JSONObject user) {
        UserService.username = user.getString("username");
        UserService.email = user.getString("email");
        UserService.firstName = user.getString("firstName");
        UserService.lastName = user.getString("lastName");
        UserService.userID = user.getJSONObject("_id").getString("$oid");
        UserService.balance = user.getDouble("balance");
        UserService.owned_stocks = user.getJSONArray("owned_stocks");
        UserService.pending_orders = user.getJSONArray("pending_orders");
        UserService.transaction_history = user.getJSONArray("transaction_history");
    }

    public static void getProfile() {
        Sockets client = new Sockets(Config.USER_DATA_IP, Config.USER_DATA_PORT);
        System.out.println(client.toString());
        System.out.println("{action:\"getUser\",_id:\"" + UserService.userID + "\"}");
        client.writeToClient("{action:\"getUser\",_id:\"" + UserService.userID + "\"}");

        JSONObject loginResponse = null;
        try {
            //aici primeste raspunsul servarului
            String user = client.readFromClient();
            loginResponse = new JSONObject(user);
            //verifica daca e eroare
            UserService.setUser(loginResponse);
        } catch(JSONException e) {
            System.out.println(e);
        }
    }

}
