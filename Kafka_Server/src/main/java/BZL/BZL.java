package BZL;

import BZL.Stocks.StockKafkaRequestHandler;
import Kafka.Kafka;
import Config.Config;
import org.json.JSONObject;

public class BZL {
    private Kafka kafka;

    public BZL(String ticker) {
        this.kafka = new Kafka(Config.KAFKA_ADDRESS,Config.KAFKA_GROUP_ID);

      //  this.stocks = new Stocks();
       // this.createStockTopics();

        //create as many threads as partitions
        this.initKafkaConsumers(ticker);
    }

    private void initKafkaConsumers(String ticker) {
        String[] asd = new String[1];
        asd[0] = ticker;
//        this.kafka.createConsumer(new StockRequestHandler(),this.stock_receiver_topics.stream().toArray(String[]::new));
        this.kafka.createConsumer(new StockKafkaRequestHandler(ticker),asd);

    }

    private JSONObject getStockInfo() {
        //get from mongo the stock
        //update sell order array with everything thats available


        return null;
    }
}
