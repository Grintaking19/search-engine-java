package Crawler;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
//import javax.swing.text.Document;

public class MongoDBClass {
    String uri;
    MongoClientURI clientURI;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection collection;
    public MongoDBClass() {
        String uri="mongodb+srv://karimyasser34:3uRq0QAZoqUbUPzN@cluster0.8ikdynj.mongodb.net/";
        this.clientURI=new MongoClientURI(uri);
        this.mongoClient=new MongoClient(clientURI);
        //connect to db
        this.mongoDatabase=mongoClient.getDatabase("Crawler");
        //get access to collection
        this.collection=mongoDatabase.getCollection(("htmlDocs"));
    }
    //function that insert a url, the file path into the database
    public void insertRecord(String link,String filePath){
        Document doc=new Document("url",link);
        doc.append("filePath",filePath);
        collection.insertOne(doc);

    }


//    public static void main(String args[]) {
//
//
//        Document search=new Document("url","fge");
//        if (search!=null) {
//            Bson updateUrl=new Document("visited",true);
//            Bson updateOperation=new Document("$set",updateUrl);
//            collection.updateOne(search,updateOperation);
//        }
//    }
}
