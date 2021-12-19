package UserData;

import Config.Config;
import Mongo.Mongo;
import Socket.Client;
import Socket.ClientHandler;
import org.json.JSONObject;

public class UserDataHandler implements ClientHandler {
    private Mongo mongo;

    public UserDataHandler() {
        mongo = new Mongo(Config.MONGO_URI,Config.MONGO_DATABASE_NAME);
    }

    @Override
    public void handle(Client client) {
        JSONObject clientRequest = new JSONObject(client.readFromClient());
        if(clientRequest.getString("action").equals("getUser")) {
            JSONObject UserData = mongo.findOne(Config.MONGO_USER_COLLECTION,new JSONObject("{_id:\""+clientRequest.getString("_id")+"\"}"));
            client.writeToClient(UserData.toString());
        }
    }
}
