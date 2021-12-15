package Auth;

import Config.Config;
import Mongo.Mongo;
import Socket.Client;
import Socket.ClientHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.NoSuchElementException;

// WARNING : TO DO -> CREATE INPUT VALIDATORS
public class ClientAuthHandler implements ClientHandler {
    private Mongo mongo;

    public ClientAuthHandler() {
        mongo = new Mongo(Config.MONGO_URI,Config.MONGO_DATABASE_NAME);
    }

    @Override
    public void handle(Client client) {
        //TO DO -> add token instead of credentials
        //TO DO -> add more validators + email|username login
        JSONObject clientRequest = new JSONObject(client.readFromClient());
        System.out.println(clientRequest.toString());


        if(clientRequest.getString("action").equals("login")) {
            if(this.validateLoginInput(clientRequest) && this.validateLoginInput_2(clientRequest)) {
                client.writeToClient(Config.UNEXPECTED_ERROR);
                return;
            }

            // activate remember me

            //check if it has auth token
            try {
                String token = clientRequest.getString("token");
                JSONObject user = mongo.findOne(Config.MONGO_USER_COLLECTION,new JSONObject("{token:\""+token+"\"}"));
                client.writeToClient(user.toString());
                return;
            } catch(JSONException jExe) {
                System.out.println(jExe);

            } catch(NoSuchElementException noEle) {
                client.writeToClient(new JSONObject("{message:\""+Config.UNEXPECTED_ERROR+"\"}").toString());
                return;
            }

            try {
                JSONObject user = this.checkUserExistence(clientRequest.getString("username"));
                if (user == null) {
                    throw new NoSuchElementException();
                }

                if(clientRequest.getString("password").equals(user.getString("password"))) {
                    try {
                        clientRequest.getString("rememberMe");
                        mongo.updateOne(Config.MONGO_USER_COLLECTION,"$set",
                                new JSONObject("{username:\""+clientRequest.getString("username")+"\"}"),
                                new JSONObject("{token:\""+client.clientID+"\"}"));
                    } catch(JSONException jExe) {

                    }
                    user.put("token",client.clientID);
                    client.writeToClient(user.toString());
                }
                else {
                    client.writeToClient(Config.NO_ACCOUNT_FOUND);
                }
                //TO DO -> add token instead of credentials

            } catch (NoSuchElementException noEle) {
                System.out.println("Client " + client.clientID + " doesn't exist");
                client.writeToClient(new JSONObject("{message:\""+Config.NO_ACCOUNT_FOUND+"\"}").toString());
            }
        }
        else if(clientRequest.getString("action").equals("register")) {
            if(this.validateRegisterInput(clientRequest) == false) {
                client.writeToClient(Config.UNEXPECTED_ERROR);
                return;
            }
            try {
                JSONObject user = this.checkUserExistence(clientRequest.getString("username"));
                if( user != null) {
                    throw new NoSuchElementException();
                }
                clientRequest.remove("action");
                //TO DO -> add minimum user data security
                clientRequest.put("balance",0.0);
                clientRequest.put("pending_orders",new JSONObject[0]);
                clientRequest.put("owned_stocks",new JSONObject[0]);
                clientRequest.put("transaction_history",new JSONObject[0]);

                mongo.insertOne(Config.MONGO_USER_COLLECTION,clientRequest);
                client.writeToClient(new JSONObject("{message:\""+Config.ACCOUNT_CREATED+"\"}").toString());
            } catch(NoSuchElementException noEle) {
                System.out.println("Client " + client.clientID + " doesn't exist");
                client.writeToClient(new JSONObject("{message:\""+Config.ACCOUNT_ALREADY_EXISTS+"\"}").toString());
            }
        }
        else if(clientRequest.getString("action").equals("logout")) {
            mongo.unsetField(Config.MONGO_USER_COLLECTION,
                    new JSONObject("{username:\""+clientRequest.getString("username")+"\"}"),
                    "token");
        }
    }

    private JSONObject checkUserExistence(String username) {
        try {
            JSONObject user = mongo.findOne(Config.MONGO_USER_COLLECTION,new JSONObject("{username:\""+username+"\"}"));
            return user;
        } catch (NoSuchElementException noEle) {
            return null;
        }
    }

    private boolean validateLoginInput_2(JSONObject clientRequest) {
        try {
            clientRequest.getString("username");
            clientRequest.getString("password");
            return true;
        } catch (JSONException jExe) {
            return false;
        }
    }

    private boolean validateLoginInput(JSONObject clientRequest) {
        try {
            clientRequest.getString("token");
            return true;
        } catch(JSONException jExe) {
            return false;
        }
    }

    private boolean validateRegisterInput(JSONObject clientRequest) {
        try {
            clientRequest.getString("username");
            clientRequest.getString("password");
            clientRequest.getString("email");
            return true;
        } catch (JSONException jExe) {
            return false;
        }
    }

    private boolean validateLoginToken(JSONObject clientRequest) {
        try {
            String remember = clientRequest.getString("rememberMe");

            return true;
        } catch(JSONException jExe) {
            return false;
        }
    }


}
