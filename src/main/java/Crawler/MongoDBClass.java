package Crawler;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoDBClass {
    public static void main(String args[]) {
        String uri="mongodb+srv://karimyasser34:3uRq0QAZoqUbUPzN@cluster0.8ikdynj.mongodb.net/";
        MongoClientURI clientURI=new MongoClientURI(uri);
        MongoClient mongoClient=new MongoClient(clientURI);
    }
}
