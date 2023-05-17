package Crawler;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import static com.mongodb.client.model.Filters.eq;
//import javax.swing.text.Document;

public class MongoDBClass {
    String connectionString;
    MongoClientURI clientURI;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection htmlDocsCollection;
    MongoCollection urlsQueueCollection;
    public MongoDBClass() {
        this.connectionString="mongodb+srv://karimyasser34:3uRq0QAZoqUbUPzN@cluster0.8ikdynj.mongodb.net/";
        this.clientURI=new MongoClientURI(connectionString);
        this.mongoClient=new MongoClient(clientURI);
        //connect to db
        this.mongoDatabase=mongoClient.getDatabase("Crawler");
        //get access to collections
        this.htmlDocsCollection=mongoDatabase.getCollection(("htmlDocs"));
        this.urlsQueueCollection=mongoDatabase.getCollection(("unvisitedUrlsQueue"));
    }
    //function that insert a url, the file path into the database
    public void insertVisitedUrlRecord(String link, String filePath){
        Document doc=new Document("url",link);
        doc.append("filePath",filePath);
        htmlDocsCollection.insertOne(doc);

    }
    //insert at end of the collection ( queue )
    public void insertUnvisitedUrlInQueue(String link){
        Document doc=new Document("url",link);
        urlsQueueCollection.insertOne(doc);
    }

    //function get the arrayList of visitedUrls from the collection "htmlDocsCollection"
    public ArrayList<String> getVisitedUrlsFromCollection() {
        MongoCollection<Document> visitedUrlsCollection = this.mongoDatabase.getCollection("htmlDocs");
        // Find all documents in the collection and add the string value to an ArrayList
        ArrayList<String> visitedUrlsList = new ArrayList<>();
        String url;
        for (Document doc : visitedUrlsCollection.find()) {
             url= doc.getString("url");
            visitedUrlsList.add(url);
        }
        return visitedUrlsList;
    }

    //function get the arrayList of visitedUrls from the collection "urlsQueueCollection"
    public Queue<String> getUrlsQueueFromCollection() {
        MongoCollection<Document> visitedUrlsCollection = this.mongoDatabase.getCollection("unvisitedUrlsQueue");
        // Find all documents in the collection and add the string value to an ArrayList
        Queue<String> unvisitedUrlsQueue = new LinkedList<>();
        String url;
        for (Document doc : visitedUrlsCollection.find()) {
            url= doc.getString("url");
            unvisitedUrlsQueue.add(url);
        }
        return unvisitedUrlsQueue;
    }

    //function delete to delete url from collection "urlsQueueCollection"
    public void deleteUrlFromUrlsQueueCollection(String link){
        // Get the "urlsQueueCollection" collection
        MongoCollection<Document> collection = this.mongoDatabase.getCollection("urlsQueueCollection");

        try {
            // Delete a document where the "url" attribute has the value "link"
            System.out.println("to be deleted from db    "+link);
            collection.deleteMany(eq("url", link));
        } catch (MongoException e) {
            // Handle the exception appropriately, such as logging the error message
            System.err.println("Error deleting document from collection: " + e.getMessage());
        }
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
