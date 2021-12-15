package BZL.Services;

import Config.Config;
import Mongo.Mongo;

public class MongoService {
    public static Mongo db = new Mongo(Config.MONGO_URI,Config.MONGO_DATABASE_NAME);

    public MongoService() {
    }
}
