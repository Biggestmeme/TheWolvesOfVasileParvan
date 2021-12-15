package BZL;

import Mongo.Mongo;
import Config.Config;
import Kafka.Kafka;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class StockUtility {
    private ArrayList<JSONObject> stock_list;
    //this method creates two topics for every stock in the db
    //we need two topics, one for receiving requests, another for showing real time transactions

    public StockUtility() {
        this.createTopics();
    }

    public ArrayList<String> getTickers() {
        Mongo mongo = new Mongo(Config.MONGO_URI,Config.MONGO_DATABASE_NAME);
        this.stock_list = mongo.find(Config.MONGO_STOCK_COLLECTION,null);
        ArrayList<String> tickers = new ArrayList<String>();
        for(JSONObject stock : this.stock_list) {
            tickers.add(stock.getString("ticker"));
        }
        return tickers;
    }

    private void createTopics() {
        System.out.println("Stock topics init...");
        Kafka kafka = new Kafka(Config.KAFKA_ADDRESS,Config.KAFKA_GROUP_ID);
        ArrayList<String> tickers = this.getTickers();
        //for every stock entry in the database create 2 kafka topics
        for(String ticker : tickers) {

            try {
                if(kafka.checkTopicExistence(ticker + "-receiver") == false) {
                    kafka.createTopic(Config.KAFKA_PROPERTIES, ticker + "-receiver", Config.PARTITIONS_PER_STOCK, Config.REPLICATION_FACTOR_PER_STOCK);
                    kafka.createTopic(Config.KAFKA_PROPERTIES, ticker + "-sender", Config.PARTITIONS_PER_STOCK, Config.REPLICATION_FACTOR_PER_STOCK);
                    System.out.println("Topics created for " + ticker);
                }
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }
        System.out.println("Stock topics created successfully");
    }
}
