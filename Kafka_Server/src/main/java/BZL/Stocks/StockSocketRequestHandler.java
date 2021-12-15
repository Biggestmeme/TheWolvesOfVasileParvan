package BZL.Stocks;

import BZL.Services.MongoService;
import Config.Config;
import Mongo.Mongo;
import Socket.Client;
import Socket.ClientHandler;
import org.json.JSONObject;

public class StockSocketRequestHandler implements ClientHandler {
    private Mongo mongo;

    public StockSocketRequestHandler() {
    }

    @Override
    public void handle(Client client) {
        JSONObject request = new JSONObject(client.readFromClient());

        if (request.getString("action").equals("Get_Stock_Info")) {
            JSONObject result = MongoService.db.findOne(Config.MONGO_STOCK_COLLECTION,new JSONObject("{company:\""+request.getString("company")+"\"}"));
            System.out.println(result.toString());
            client.writeToClient(result.toString());
        }
    }
}
