package Crawler;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

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
    public class VisitedUrlData {
        private String url;
        private String filePath;
        private int countChildren;
        private Set<String> Parents;

        public VisitedUrlData(String url, String filePath, Set<String> parents, int countChildren) {
            this.url = url;
            this.filePath = filePath;
            this.countChildren = countChildren;
            Parents = parents;
        }

        public String getUrl() {
            return url;
        }

        public String getFilePath() {
            return filePath;
        }

        public int getCountChildren() {
            return countChildren;
        }

        public Set<String> getParents() {
            return Parents;
        }
    }
    //function that insert a url, the file path into the database
    public void insertVisitedUrlRecord(String link, String filePath, Set<String> Parents){
        //convert set to list
        List<String> parentsList = new ArrayList<>(Parents);
        Document doc=new Document("url",link);
        doc.append("filePath",filePath);
        doc.append("parents",parentsList);
        doc.append("childrenCount",0);
        htmlDocsCollection.insertOne(doc);

    }
    public void updateChildCountInVisitedUrl(String popedUrl,int countChildren){
        Document search=new Document("url",popedUrl);
        if (search!=null) {
            Bson updateCountChildren=new Document("childrenCount",countChildren);
            Bson updateOperation=new Document("$set",updateCountChildren);
            htmlDocsCollection.updateOne(search,updateOperation);
        }
    }
    public void updateParentsListInVisitedUrlRecord(String link,Set<String> Parents){
        //convert set to list
        List<String> parentsList = new ArrayList<>(Parents);
        Document search=new Document("url",link);
        if (search!=null) {
            Bson updateParentsList=new Document("parents",parentsList);
            Bson updateOperation=new Document("$set",updateParentsList);
            htmlDocsCollection.updateOne(search,updateOperation);
        }

    }

    //insert at end of the collection ( queue )
    public void insertUnvisitedUrlInQueue(String link){
        Document doc=new Document("url",link);
        urlsQueueCollection.insertOne(doc);
    }

    //function get the arrayList of visitedUrls from the collection "htmlDocsCollection"
    public ArrayList<VisitedUrlData> getVisitedUrlsFromCollection() {
        MongoCollection<Document> visitedUrlsCollection = this.mongoDatabase.getCollection("htmlDocs");
        // Find all documents in the collection and add the string value to an ArrayList
        //ArrayList<String> ParentsUrlsList = new ArrayList<>();
        String urll;
        String filePathh;
        List<String> parentsUrlsList = new ArrayList<>();
        int count;
        ArrayList<VisitedUrlData> AllData=new ArrayList<>();
        for (Document doc : visitedUrlsCollection.find()) {

            urll= doc.getString("url");
            filePathh=doc.getString("filePath");
            parentsUrlsList=doc.get("parents",List.class);
            //visitedUrlsList.add(url);
            count=doc.getInteger("childrenCount");
            // Convert the ArrayList to a HashSet
            Set<String> parentsSet = new HashSet<String>(parentsUrlsList);
            VisitedUrlData data=new VisitedUrlData(urll,filePathh,parentsSet,count);
            AllData.add(data) ;
        }

        return AllData;
    }

    //function get the arrayList of visitedUrls from the collection "urlsQueueCollection"
    public ArrayList<String> getUrlsQueueFromCollection() {
        MongoCollection<Document> visitedUrlsCollection = this.mongoDatabase.getCollection("unvisitedUrlsQueue");
        // Find all documents in the collection and add the string value to an ArrayList
        ArrayList<String> unvisitedUrlsQueue = new ArrayList<>();
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
