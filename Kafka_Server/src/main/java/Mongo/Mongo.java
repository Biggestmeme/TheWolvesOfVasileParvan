package Mongo;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class Mongo {
    private String address;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private String databaseName;

    public Mongo(String address, String databaseName) {
        System.out.println("Mongo connecting...");
        this.address = address;
        this.databaseName = databaseName;
        //connect to mongo
        mongoClient = new MongoClient(new MongoClientURI(this.address));
        //get the specified database
        database = mongoClient.getDatabase(this.databaseName);
        System.out.println("Mongo connection established successfully");
    }

    //the same function as the one used by mongo scripting language
    public ArrayList<JSONObject> find(String collection_name,JSONObject query) {
        //get db collection
        MongoCollection<Document> collection = database.getCollection(collection_name);
        //get stats of the collection
        Document stats = database.runCommand(new Document("collStats", collection_name));
        //convert to json string
        String stringStats =  stats.toJson();
        //convert to jsonObject
        JSONObject jsonStats = new JSONObject(stringStats);
        //get how many entries are in there
        int collection_count = jsonStats.getInt("count");

        //init results array
        ArrayList<JSONObject> results = new ArrayList<JSONObject>(collection_count);
        //actually find the collection
        FindIterable<Document> collection_iterable = null;
        if(query == null) {
            collection_iterable = collection.find();
        }
        else {
            Iterator<String> queryKeys = query.keys();
            BasicDBObject bdoQuery = new BasicDBObject();
            String bdoQueryKey = queryKeys.next();
            bdoQuery.append(bdoQueryKey,query.get(bdoQueryKey));
            collection_iterable = collection.find(bdoQuery);
        }

        //get a cursor of the collection
        MongoCursor collection_cursor = collection_iterable.cursor();
        //iterate over it and add it into an arrayList
        while (collection_cursor.hasNext()) {
            String jsonEntry = com.mongodb.util.JSON.serialize(collection_cursor.next());
            results.add(new JSONObject(jsonEntry));
        }

        return results;
    }

    public ArrayList<JSONObject> findWithCondition(String collection_name,JSONObject conditions,String field) {
        //get db collection
        Document docConditions = Document.parse(conditions.toString());
        ArrayList<JSONObject> results = new ArrayList<JSONObject>();
        FindIterable<Document> foundMany = database.getCollection(collection_name).find(new Document(field,docConditions));
        MongoCursor foundManyCursor = foundMany.cursor();
        //iterate over it and add it into an arrayList
        while (foundManyCursor.hasNext()) {
            String jsonEntry = com.mongodb.util.JSON.serialize(foundManyCursor.next());
            results.add(new JSONObject(jsonEntry));
        }

        return results;
    }

    public void insertOne(String collection_name,JSONObject jsonObject) {
        //if json object is null
        if(jsonObject.toString().length() == 2 || collection_name.length() == 0) {
            return;
        }
        Document document = Document.parse(jsonObject.toString());
        //get collection and insert
        database.getCollection(collection_name).insertOne(document);

    }

    public void updateOne(String collection_name,String updateParam,JSONObject query, JSONObject update) {
        //if json object is null
        if(query.toString().length() == 2 || update.toString().length() == 2 || collection_name.length() == 0) {
            return;
        }
        Iterator<String> queryKeys = query.keys();
        BasicDBObject bdoUpdate = BasicDBObject.parse(update.toString());
        BasicDBObject filter = new BasicDBObject();
        while(queryKeys.hasNext()) {
            String queryKey = queryKeys.next();
            if(queryKey.equals("_id")) {
                filter.put(queryKey,new ObjectId(query.getString(queryKey)));
                continue;
            }
            filter.put(queryKey,query.get(queryKey));
        }
        database.getCollection(collection_name).updateOne(filter,new BasicDBObject(updateParam, bdoUpdate));
    }

    public JSONObject findOne(String collection_name, JSONObject query) {
        //if json object is null
        if(query.toString().length() == 2 || collection_name.length() == 0) {
            return null;
        }
        Iterator<String> queryKeys = query.keys();
        BasicDBObject filter = new BasicDBObject();
        while(queryKeys.hasNext()) {
            String queryKey = queryKeys.next();
            if(queryKey.equals("_id")) {
                filter.put(queryKey,new ObjectId(query.getString(queryKey)));
                continue;
            }
            filter.put(queryKey,query.get(queryKey));
        }
        FindIterable<Document> foundOne = database.getCollection(collection_name).find(filter);
        Iterator<Document> keys = foundOne.iterator();
        return new JSONObject(com.mongodb.util.JSON.serialize(keys.next()).toString());
    }

    public void deleteOne(String collection_name, JSONObject query) {
        if(query.toString().length() == 2 || collection_name.length() == 0) {
            return;
        }
        BasicDBObject bdoQuery = new BasicDBObject();
        ObjectId objectId = this.constructObjectID(query);
        Iterator<String> queryKeys = query.keys();
        String key = queryKeys.next();
        if (objectId == null) {
            bdoQuery.append(key,query.get(key));
        }
        else {
            bdoQuery.append(key,objectId);
        }
        database.getCollection(collection_name).deleteOne(bdoQuery);
    }

    public void unsetField(String collection_name, JSONObject query, String field) {
        //if json object is null
        if(query.toString().length() == 2 || field.length() < 1 || collection_name.length() == 0) {
            return;
        }

        Iterator<String> queryKeys = query.keys();
        BasicDBObject filter = new BasicDBObject();
        while(queryKeys.hasNext()) {
            String queryKey = queryKeys.next();
            if(queryKey.equals("_id")) {
                filter.put(queryKey,new ObjectId(query.getString(queryKey)));
                continue;
            }
            filter.put(queryKey,query.get(queryKey));
        }
        Bson unset = Updates.unset(field);

        database.getCollection(collection_name).updateOne(filter,unset);
    }

    private ObjectId constructObjectID(JSONObject query) {
        Iterator<String> queryKeys = query.keys();
        String key = queryKeys.next();
        if(key.equals("_id")) {
            return new ObjectId(query.getString(key));
        }
        return null;
    }



}
