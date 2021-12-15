package BZL.Stocks;

import BZL.Services.MongoService;
import Config.Config;
import Kafka.Kafka;
import Kafka.KafkaMessageHandler;
import Mongo.Mongo;
import jdk.jfr.StackTrace;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.requests.SaslAuthenticateRequest;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StockKafkaRequestHandler implements KafkaMessageHandler {
    private Kafka kafka;
    private Mongo mongo;
    private String ticker;
    private static ConcurrentHashMap<String,JSONObject> BuyOrders = new ConcurrentHashMap<String, JSONObject>();
    private static ConcurrentHashMap<String,JSONObject> SellOrders = new ConcurrentHashMap<String, JSONObject>();


    public StockKafkaRequestHandler(String ticker) {
        this.ticker = ticker;
        this.kafka = new Kafka(Config.KAFKA_ADDRESS,Config.KAFKA_GROUP_ID);
       // this.mongo = new Mongo(Config.MONGO_URI,Config.MONGO_DATABASE_NAME);

    }

    public void handleMessage(ConsumerRecord<String, String> record) {
        System.out.println(Thread.currentThread().getName() + " Received Request : " + record.value());
        JSONObject request;

        try {
            request = new JSONObject(record.value());
            if(testRequest(request) == false) {
                throw  new JSONException("");
            }
        } catch(JSONException je) {
            System.out.println(record.value() + " is in wrong format, expected JSON");
            return;
        }

        if(request.getString("action").equals("Buy_Order")) {
            if(resolveBuyOrder(request)) {

                MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",
                        new JSONObject("{_id:\""+request.getString("user")+"\"}"),
                        new JSONObject("{transaction_history:"+request.toString()+"}"));
                //update user stocks
                updateUserOwnedStocks(request);
                kafka.sendMessage(ticker+"-sender",hashString(request.toString()),request.toString());
            }
            else {
                StockKafkaRequestHandler.BuyOrders.put(hashString(request.toString()),request);
                MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",new JSONObject("{_id:\""+request.getString("user")+"\"}"), new JSONObject("{pending_orders:"+ request.toString()+"}"));
                MongoService.db.insertOne(Config.MONGO_BUY_ORDERS_COLLECTION,request);
            }
            updateUserBalance(request,request);

        }
        else if (request.getString("action").equals("Sell_Order")) {
            //update user stocks

            if(resolveSellOrder(request)) {
                //update user
                MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",
                        new JSONObject("{_id:\""+request.getString("user")+"\"}"),
                        new JSONObject("{transaction_history:"+request.toString()+"}"));
                MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$pull",new JSONObject("{_id:\""+request.getString("user")+"\"}"), new JSONObject("{pending_orders:{timestamp:\""+request.getString("timestamp")+"\"}}"));
                kafka.sendMessage(ticker+"-sender",hashString(request.toString()),request.toString());
                updateUserBalance(request,request);
            }
            else {
                StockKafkaRequestHandler.SellOrders.put(hashString(request.toString()),request);
                //MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",new JSONObject("{_id:\""+request.getString("user")+"\"}"), new JSONObject("{pending_orders:"+ request.toString()+"}"));
                MongoService.db.insertOne(Config.MONGO_SELL_ORDERS_COLLECTION,request);
                MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",
                        new JSONObject("{_id:\""+request.getString("user")+"\"}"),
                        new JSONObject("{pending_orders:"+request.toString()+"}"));
                updateUserOwnedStocks(request);
            }
        }
        else if(request.getString("action").equals("Update_Order")) {
            resolveUpdateOrder(request);
        }
    }

    private boolean resolveSellOrder(JSONObject request) {
        ArrayList<JSONObject> sorted_sell_orders = new ArrayList<JSONObject>();
        //transform the hashmap values in jsonarray
        JSONArray jsonArr = new JSONArray(StockKafkaRequestHandler.BuyOrders.values().stream().toList());



        //transform the json array in a list to sort
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArr.length(); i++) {
            jsonValues.add(jsonArr.getJSONObject(i));
        }

        sortJSONCollection(jsonValues);

        //add them to sorted array list
        for (int i = 0; i < StockKafkaRequestHandler.BuyOrders.size(); i++) {
            sorted_sell_orders.add(jsonValues.get(i));
        }

        //get current price and ammunt
        int needed_amount = request.getInt("amount");
        double price = request.getDouble("price");
        Iterator<JSONObject> iterator = sorted_sell_orders.iterator();

        while(iterator.hasNext()) {
            if(needed_amount <= 0) {
                break;
            }

            JSONObject currentBuyOrder = iterator.next();
            //daca pretul cu care cineva vrea sa cumpere este mai mare sau egal cu cel din req
            if(currentBuyOrder.getDouble("price") >= price && currentBuyOrder.getString("user").equals(request.getString("user")) == false) {
                needed_amount -= currentBuyOrder.getInt("amount");
            }
        }

        //if it didnt found enough stocks to sell return cuz yea
        if(needed_amount > 0) {
            StockKafkaRequestHandler.SellOrders.put(hashString(request.toString()),request);
            return false;
        }

        //update seller

        iterator = sorted_sell_orders.iterator();
        needed_amount = request.getInt("amount");

        //iterate again over sorted jsons
        while(iterator.hasNext()) {
            if(needed_amount == 0 )
                break;

            JSONObject currentBuyOrder = iterator.next();
            System.out.println("Currently working with buying user " + currentBuyOrder.getString("user"));
            System.out.println(currentBuyOrder.toString());

            if(currentBuyOrder.getDouble("price") >= price && currentBuyOrder.getString("user").equals(request.getString("user")) == false) {
                //if we have the exact ammount
                if(currentBuyOrder.getInt("amount") == needed_amount) {
                    System.out.println("remove from memory");
                    StockKafkaRequestHandler.BuyOrders.remove(hashString(currentBuyOrder.toString()));
                    System.out.println("delete entry from db");
                    MongoService.db.deleteOne(Config.MONGO_BUY_ORDERS_COLLECTION,new JSONObject("{timestamp:\""+currentBuyOrder.getString("timestamp")+"\"}"));
                    System.out.println("pull entry from user pending list");
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$pull",new JSONObject("{_id:\""+currentBuyOrder.getString("user")+"\"}"), new JSONObject("{pending_orders:{timestamp:\""+currentBuyOrder.getString("timestamp")+"\"}}"));
                    System.out.println("update user transaction history");
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",new JSONObject("{_id:\""+currentBuyOrder.getString("user")+"\"}"),new JSONObject("{transaction_history:"+currentBuyOrder.toString()+"}"));
                    System.out.println("update buying user stocks");
                    updateUserOwnedStocks(currentBuyOrder);
                    needed_amount = 0;
                    //TO DO -> UPDATE DB AFTER EACH UPDATE
                }
                //if the ammount is less
                else if(currentBuyOrder.getInt("amount") < needed_amount) {
                    System.out.println("remove from memory");
                    StockKafkaRequestHandler.BuyOrders.remove(hashString(currentBuyOrder.toString()));
                    System.out.println("delete entry from db");
                    MongoService.db.deleteOne(Config.MONGO_BUY_ORDERS_COLLECTION,new JSONObject("{timestamp:\""+currentBuyOrder.getString("timestamp")+"\"}"));
                    System.out.println("pull entry from user pending list");
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$pull",new JSONObject("{_id:\""+currentBuyOrder.getString("user")+"\"}"), new JSONObject("{pending_orders:{timestamp:\""+currentBuyOrder.getString("timestamp")+"\"}}"));
                    System.out.println("update user transaction history");
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",new JSONObject("{_id:\""+currentBuyOrder.getString("user")+"\"}"),new JSONObject("{transaction_history:"+currentBuyOrder.toString()+"}"));
                    System.out.println("update buying user stocks");
                    updateUserOwnedStocks(currentBuyOrder);
                    needed_amount -= currentBuyOrder.getInt("amount");
                }
                //if the ammount is higher
                else if (currentBuyOrder.getInt("amount") > needed_amount) {
                    System.out.println("remove from memory");
                    StockKafkaRequestHandler.BuyOrders.remove(hashString(currentBuyOrder.toString()));
                    System.out.println("delete entry from db");
                    MongoService.db.updateOne(Config.MONGO_BUY_ORDERS_COLLECTION,"$set",new JSONObject("{timestamp:\""+currentBuyOrder.getString("timestamp")+"\"}"),new JSONObject("{amount:"+(currentBuyOrder.getInt("amount") - needed_amount)+"}"));
                    System.out.println("pull entry from user pending list");
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$set",new JSONObject("{_id:\""+currentBuyOrder.getString("user")+"\",pending_orders.amount:"+currentBuyOrder.getInt("amount")+"}"), new JSONObject("{pending_orders.$.amount:"+(currentBuyOrder.getInt("amount") - needed_amount)+"}"));
                    System.out.println("update user transaction history");
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",new JSONObject("{_id:\""+currentBuyOrder.getString("user")+"\"}"),new JSONObject("{transaction_history:"+currentBuyOrder.toString()+"}"));
                    System.out.println("update buying user stocks");
                    updateUserOwnedStocks(currentBuyOrder);
                    System.out.println("add back updated entry to BuyOrders");
                    currentBuyOrder.put("amount",currentBuyOrder.getInt("amount") - needed_amount);
                    StockKafkaRequestHandler.BuyOrders.put(hashString(currentBuyOrder.toString()),currentBuyOrder);
                    needed_amount = 0;

                }
                System.out.println("needing amount left : " + needed_amount);
            }
        }
        return true;
    }

    private boolean resolveBuyOrder(JSONObject request) {
        System.out.println("User " + request.getString("user") + " requested to solve a buy order");
        //create a sorted
        ArrayList<JSONObject> sorted_sell_orders = new ArrayList<JSONObject>();
        //transform the hashmap values in jsonarray
        JSONArray jsonArr = new JSONArray(StockKafkaRequestHandler.SellOrders.values().stream().toList());

        //transform the json array in a list to sort
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArr.length(); i++) {
            jsonValues.add(jsonArr.getJSONObject(i));
        }

        sortJSONCollection(jsonValues);

        //add them to sorted array list
        for (int i = 0; i < StockKafkaRequestHandler.SellOrders.size(); i++) {
            sorted_sell_orders.add(jsonValues.get(i));
        }

        //get current price and ammunt
        int needed_amount = request.getInt("amount");
        double price = request.getDouble("price");
        Iterator<JSONObject> iterator = sorted_sell_orders.iterator();

        while(iterator.hasNext()) {
            if(needed_amount <= 0) {
                break;
            }

            JSONObject currentSellOrder = iterator.next();
            //daca pretul cu care cumpara este mai mare decat pretul cu care vinde
            if(currentSellOrder.getDouble("price") <= price && currentSellOrder.getString("user").equals(request.getString("user")) == false) {
                needed_amount -= currentSellOrder.getInt("amount");
            }
        }

        //if it didnt found enough stocks to sell return cuz yea
        if(needed_amount > 0) {
            StockKafkaRequestHandler.BuyOrders.put(hashString(request.toString()),request);
            return false;
        }
        System.out.println("Found potential sellers");
        iterator = sorted_sell_orders.iterator();
        needed_amount = request.getInt("amount");

        //iterate again over sorted jsons
        while(iterator.hasNext()) {
            if(needed_amount == 0 )
                break;

            JSONObject currentSellOrder = iterator.next();
            System.out.println("Currently working with seller user " + currentSellOrder.getString("user"));
            System.out.println(currentSellOrder.toString());
            //if the price that the dude is selling is lower than the buyer
            if(currentSellOrder.getDouble("price") <= price && currentSellOrder.getString("user").equals(request.getString("user")) == false) {
                //if we have the exact ammount
                if(currentSellOrder.getInt("amount") == needed_amount) {
                    System.out.println("remove from memory");
                    StockKafkaRequestHandler.SellOrders.remove(hashString(currentSellOrder.toString()));
                    System.out.println("delete entry from db");
                    MongoService.db.deleteOne(Config.MONGO_SELL_ORDERS_COLLECTION,new JSONObject("{timestamp:\""+currentSellOrder.getString("timestamp")+"\"}"));
                    System.out.println("pull entry from user pending list");
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$pull",new JSONObject("{_id:\""+currentSellOrder.getString("user")+"\"}"), new JSONObject("{pending_orders:{timestamp:\""+currentSellOrder.getString("timestamp")+"\"}}"));
                    System.out.println("update user transaction history");
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",new JSONObject("{_id:\""+currentSellOrder.getString("user")+"\"}"),new JSONObject("{transaction_history:"+currentSellOrder.toString()+"}"));
                    System.out.println("update user balance");
                    updateSellingUserBalance(currentSellOrder,request.getDouble("price"));
                    needed_amount = 0;
                    //TO DO -> UPDATE DB AFTER EACH UPDATE
                }
                //if the ammount is less
                else if(currentSellOrder.getInt("amount") < needed_amount) {
                    System.out.println("remove from memory");
                    StockKafkaRequestHandler.SellOrders.remove(hashString(currentSellOrder.toString()));
                    System.out.println("delete entry from db");
                    MongoService.db.deleteOne(Config.MONGO_SELL_ORDERS_COLLECTION,new JSONObject("{timestamp:\""+currentSellOrder.getString("timestamp")+"\"}"));
                    System.out.println("pull entry from user pending list");
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$pull",new JSONObject("{_id:\""+currentSellOrder.getString("user")+"\"}"), new JSONObject("{pending_orders:{timestamp:\""+currentSellOrder.getString("timestamp")+"\"}}"));
                    System.out.println("update user transaction history");
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",new JSONObject("{_id:\""+currentSellOrder.getString("user")+"\"}"),new JSONObject("{transaction_history:"+currentSellOrder.toString()+"}"));
                    System.out.println("update user balance");
                    updateSellingUserBalance(currentSellOrder,request.getDouble("price"));
                    needed_amount -= currentSellOrder.getInt("amount");
                }
                //if the ammount is higher
                else if (currentSellOrder.getInt("amount") > needed_amount) {
                    System.out.println("remove from memory");
                    StockKafkaRequestHandler.SellOrders.remove(hashString(currentSellOrder.toString()));
                    System.out.println("update entry from db");
                    MongoService.db.updateOne(Config.MONGO_SELL_ORDERS_COLLECTION,"$set",new JSONObject("{timestamp:\""+currentSellOrder.getString("timestamp")+"\"}"),new JSONObject("{amount:"+(currentSellOrder.getInt("amount") - needed_amount)+"}"));
                    System.out.println("update entry from user pending list");
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$set",new JSONObject("{_id:\""+currentSellOrder.getString("user")+"\",pending_orders.timestamp:\""+currentSellOrder.getString("timestamp")+"\"}"), new JSONObject("{pending_orders.$.amount:"+(currentSellOrder.getInt("amount") - needed_amount)+"}"));
                    System.out.println("update user transaction history");
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",new JSONObject("{_id:\""+currentSellOrder.getString("user")+"\"}"),new JSONObject("{transaction_history:"+request.toString()+"}"));
                    System.out.println("update user balance");
                    updateSellingUserBalance(currentSellOrder,request.getDouble("price"));
                    System.out.println("update memory entry");
                    currentSellOrder.put("amount",currentSellOrder.getInt("amount") - needed_amount);
                    System.out.println("add back updated entry to SellOrders");
                    StockKafkaRequestHandler.SellOrders.put(hashString(currentSellOrder.toString()),currentSellOrder);
                    needed_amount = 0;

                }
                System.out.println("needing amount left : " + needed_amount);
            }
        }

        return true;
    }

    private boolean resolveUpdateOrder(JSONObject request) {
        JSONObject updating_order = request.getJSONObject("updating_order");

        if(updating_order.getString("action").equals("Buy_Order")) {
            try {
                System.out.println("remove from memory");
                StockKafkaRequestHandler.BuyOrders.remove(hashString(updating_order.toString()));
                System.out.println("update Buy_Orders collection");
                MongoService.db.updateOne(Config.MONGO_BUY_ORDERS_COLLECTION, "$set",
                        new JSONObject("_id:\"" + updating_order.getJSONObject("_id").getString("$oid") + "\""),
                        new JSONObject("{pending_orders.$.amount:" + updating_order.getInt("amount") + "}"));
                System.out.println("update user pending orders");
                MongoService.db.updateOne(Config.MONGO_USER_COLLECTION, "$set",
                        new JSONObject("{_id:\"" + updating_order.getString("user") + "\",pending_orders:{timestamp:\"" + updating_order.getString("timestamp") + "\"}}"),
                        new JSONObject("{pending_orders.$.amount:" + updating_order.getInt("amount") + "}"));
                System.out.println("placing back the updated buy order");
                StockKafkaRequestHandler.BuyOrders.put(hashString(updating_order.toString()), updating_order);
            } catch(JSONException e) {
                System.out.println(e);
                return false;
            } catch(NoSuchElementException n) {
                System.out.println(n);
                return false;
            }
        }
        else if(updating_order.getString("action").equals("Sell_Order")) {
            try {
                System.out.println("remove from memory");
                StockKafkaRequestHandler.SellOrders.remove(hashString(updating_order.toString()));
                System.out.println("update Buy_Orders collection");
                MongoService.db.updateOne(Config.MONGO_SELL_ORDERS_COLLECTION, "$set",
                        new JSONObject("_id:\"" + updating_order.getJSONObject("_id").getString("$oid") + "\""),
                        new JSONObject("{pending_orders.$.amount:" + updating_order.getInt("amount") + "}"));
                System.out.println("update user pending orders");
                MongoService.db.updateOne(Config.MONGO_USER_COLLECTION, "$set",
                        new JSONObject("{_id:\"" + updating_order.getString("user") + "\",pending_orders:{timestamp:\"" + updating_order.getString("timestamp") + "\"}}"),
                        new JSONObject("{pending_orders.$.amount:" + updating_order.getInt("amount") + "}"));
                System.out.println("placing back the updated sell order");
                StockKafkaRequestHandler.SellOrders.put(hashString(updating_order.toString()), updating_order);
            } catch(JSONException e) {
                System.out.println(e);
                return false;
            } catch(NoSuchElementException n) {
                System.out.println(n);
                return false;
            }
        }
        return true;
    }

    private void sortJSONCollection(List<JSONObject> jsonValues) {
        //sort the list
        Collections.sort( jsonValues, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID
            private static final String KEY_NAME = "price";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                double valA = 0;
                double valB = 0;

                try {
                    valA = a.getDouble(KEY_NAME);
                    valB = b.getDouble(KEY_NAME);
                }
                catch (JSONException e) {
                    //do something
                }

                if (valA == valB)
                    return 0;
                else if(valA < valB)
                    return -1;
                else
                    return 1;
                //if you want to change the sort order, simply use the following:
                //return -valA.compareTo(valB);
            }
        });
    }

    private boolean testRequest(JSONObject request) {
        try {
            request.getInt("amount");
            request.getDouble("price");
            request.getString("action");
            request.getString("ticker");
        } catch (JSONException je) {
            System.out.println(request.toString() + " is in wrong format, expected JSON");
            return false;
        }
        return true;
    }

    private String hashString(String stringToHash){
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(stringToHash.getBytes());
            return new String(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    private JSONObject createStockJSON(String company,String ticker,int amount, double bought_price) {
        return new JSONObject("{company:\""+company+"\",ticker:\""+ticker+"\",amount:"+amount+",price:"+bought_price+"}");
    }

    private void updateBuyingUserStocks(JSONObject sellingUser, JSONObject buyingUser) {
        System.out.println("updating buying user stocks " + sellingUser.getString("user"));
        JSONObject user = MongoService.db.findOne(Config.MONGO_USER_COLLECTION,new JSONObject("{_id:\""+sellingUser.getString("user")+"\"}"));
        try {
            JSONArray owned_stocks = user.getJSONArray("owned_stocks");
            JSONObject wanted_stock = null;
            for (int i = 0; i < owned_stocks.length(); i++) {
                if (owned_stocks.getJSONObject(i).getString("company").equals(sellingUser.getString("company"))) {
                    wanted_stock = owned_stocks.getJSONObject(i);
                    break;
                }
            }

            System.out.println("Found existing stock : " + wanted_stock != null);

            if(wanted_stock != null) {
                System.out.println("updating stock amount with " + (wanted_stock.getInt("amount") + sellingUser.getInt("amount")));
                MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$set",
                        new JSONObject("{_id:\""+sellingUser.getString("user")+"\",owned_stocks.company:\""+ sellingUser.getString("company")+"\"}"),
                        new JSONObject("{owned_stocks.$.amount:"+ (wanted_stock.getInt("amount") + sellingUser.getInt("amount")) +"}"));
            }
            else {
                System.out.println("pushing new stock with amount " + sellingUser.getString("amount"));
                MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",
                        new JSONObject("{_id:\""+sellingUser.getString("user")+"\"}"),
                        new JSONObject("{owned_stocks:"+createStockJSON(sellingUser.getString("company"),sellingUser.getString("ticker"),sellingUser.getInt("amount"),sellingUser.getDouble("price")).toString()+"}"));
            }

        } catch(JSONException e) {

        }
    }

    private void updateUserOwnedStocks(JSONObject request) {
        System.out.println("Updating user " + request.getString("user") + " stocks");
        System.out.println(request.toString());
        JSONObject user = MongoService.db.findOne(Config.MONGO_USER_COLLECTION,new JSONObject("{_id:\""+request.getString("user")+"\"}"));
        try {
            JSONArray owned_stocks = user.getJSONArray("owned_stocks");
            JSONObject wanted_stock = null;
            for(int i = 0; i < owned_stocks.length(); i++) {
                if(owned_stocks.getJSONObject(i).getString("company").equals(request.getString("company"))) {
                    System.out.println("Found stock already in array");
                    wanted_stock = owned_stocks.getJSONObject(i);
                    break;
                }
            }

            if (request.getString("action").equals("Buy_Order") && wanted_stock != null) {

                MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$set",
                        new JSONObject("{_id:\""+request.getString("user")+"\",owned_stocks.company:\""+ request.getString("company")+"\"}"),
                        new JSONObject("{owned_stocks.$.amount:"+ (wanted_stock.getInt("amount") + request.getInt("amount")) +"}"));
            }
            else if(request.getString("action").equals("Buy_Order") && wanted_stock == null) {
                MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$push",
                        new JSONObject("{_id:\""+request.getString("user")+"\"}"),
                        new JSONObject("{owned_stocks:"+createStockJSON(request.getString("company"),request.getString("ticker"),request.getInt("amount"),request.getDouble("price")).toString()+"}"));
            }
            else if (request.getString("action").equals("Sell_Order") && wanted_stock != null) {
                if((wanted_stock.getInt("amount") - request.getInt("amount")) != 0) {
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION, "$set",
                            new JSONObject("{_id:\"" + request.getString("user") + "\",owned_stocks.company:\"" + request.getString("company") + "\"}"),
                            new JSONObject("{owned_stocks.$.amount:" + (wanted_stock.getInt("amount") - request.getInt("amount")) +
                                    ",owned_stocks.$.price:" + request.getDouble("price") + "}"));
                }
                else {
                    MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$pull",
                            new JSONObject("{_id:\"" + request.getString("user") + "\",owned_stocks.company:\"" + request.getString("company") + "\"}"),
                            new JSONObject("{owned_stocks:{ticker:\""+request.getString("ticker")+"\"}}"));
                }
            }
        } catch(JSONException e) {
            System.out.println("HELLO ERROR FFS?");
            System.out.println(e);
        }
    }

    public void updateSellingUserBalance(JSONObject sellingUser,double buying_price) {
        System.out.println("Updating balance for selling user " + sellingUser.getString("user"));
        JSONObject user = MongoService.db.findOne(Config.MONGO_USER_COLLECTION,new JSONObject("{_id:\""+sellingUser.getString("user")+"\"}"));
        MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$set",
                new JSONObject("{_id:\""+sellingUser.getString("user")+"\"}"),
                new JSONObject("{balance:"+(user.getDouble("balance") + buying_price*sellingUser.getInt("amount")) +"}"));

    }

    public void updateUserBalance(JSONObject sitting_request,JSONObject ordering_request) {
        System.out.println("Updating balance for user " + sitting_request.getString("user"));
        System.out.println(sitting_request.toString());
        JSONObject user = MongoService.db.findOne(Config.MONGO_USER_COLLECTION,new JSONObject("{_id:\""+sitting_request.getString("user")+"\"}"));


        if(sitting_request.getString("action").equals("Buy_Order") && user.getDouble("balance") >= ordering_request.getDouble("price")*ordering_request.getInt("amount")) {
            MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$set",
                    new JSONObject("{_id:\""+sitting_request.getString("user")+"\"}"),
                    new JSONObject("{balance:"+(user.getDouble("balance") - ordering_request.getDouble("price")*ordering_request.getInt("amount")) +"}"));
        }
        else if(sitting_request.getString("action").equals("Sell_Order")) {
            MongoService.db.updateOne(Config.MONGO_USER_COLLECTION,"$set",
                    new JSONObject("{_id:\""+sitting_request.getString("user")+"\"}"),
                    new JSONObject("{balance:"+(user.getDouble("balance") + ordering_request.getDouble("price")*ordering_request.getInt("amount")) +"}"));

        }

    }
/*
    public boolean checkIfUserCanSell(JSONObject request) {
        JSONObject user = MongoService.db.findOne(Config.MONGO_USER_COLLECTION,new JSONObject("{_id:\""+request.getString("user")+"\"}"));
        System.out.println(user.toString());
    }
*/
}



