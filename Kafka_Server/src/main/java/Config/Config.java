package Config;

public class Config {
    public static String KAFKA_ADDRESS = "localhost:9092";
    public static String KAFKA_GROUP_ID = "basic-group-id";
    public static String KAFKA_PROPERTIES = "src/main/java/Kafka/kafka.properties";

    public static String MONGO_DATABASE_NAME = "SiteLicitatii";
    public static String MONGO_PASSWORD = "UMdr98Y6d62D16Sa";
    public static String MONGO_USER = "PCBE";
    public static String MONGO_URI = "mongodb+srv://"+MONGO_USER+":"+MONGO_PASSWORD+"@cluster0.o9huu.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
    public static String MONGO_STOCK_COLLECTION = "Stocks";
    public static String MONGO_BUY_ORDERS_COLLECTION = "Buy_Orders";
    public static String MONGO_SELL_ORDERS_COLLECTION = "Sell_Orders";

    public static int STOCK_SOCKET_SERVER_PORT = 64502;
    public static int USER_DATA_PORT = 64515;
    public static int ClientAuthPort = 64501;
    public static String MONGO_USER_COLLECTION = "Users";


    public static String NO_ACCOUNT_FOUND = "No client found";
    public static String ACCOUNT_ALREADY_EXISTS = "Account already exists";
    public static String ACCOUNT_CREATED = "Account created";
    public  static String UNEXPECTED_ERROR = "Unexpected Error";

    public static int PARTITIONS_PER_STOCK = 10;
    public static short REPLICATION_FACTOR_PER_STOCK = 1;

}
