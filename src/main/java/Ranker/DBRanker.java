package Ranker;
import com.mongodb.*;
//import com.mongodb.client.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import indexer.UrlData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class DBRanker
{
    // Member Variables used
    MongoClient mongoClient;
    MongoDatabase RankerDB;
    MongoCollection<Document> urlsCollection;
    // Constructor
    public DBRanker()
    {
        // Make an object of type Mongoclient and make it local on port 27017 to be faster
        com.mongodb.MongoClient mongoClient = new com.mongodb.MongoClient("localhost", 27017);
        // Get the database called Indexer
        MongoDatabase database = mongoClient.getDatabase("Indexer");
        // Setting the member variables with the given data from connecting the database
        this.mongoClient  = mongoClient; // Client of mongo used to access the database
        this.RankerDB = database; // Database itself
        this.urlsCollection = RankerDB.getCollection("urls"); // Retrieve all the urls and save them
    }
    // This is a function that insert in the database
    public void insertUrlMap(Map<String, UrlData> urls){
        //First we need delete all data related to this index then add the need data(Delete many is much faster than delete one)
        this.urlsCollection.deleteMany(new Document());
        List <Document> documents_inserted = new ArrayList<>();
        // Loop on all the documents to be added (Not one by one such to be faster)
        for(Map.Entry<String, UrlData> entry: urls.entrySet())
        {
            documents_inserted.add(new Document("url", entry.getKey())
                    .append("filepath", entry.getValue().FilePath)
                    .append("popularity", entry.getValue().popularity)
                    .append("indexed", 0));
        }
        // Insert all
        this.urlsCollection.insertMany(documents_inserted);
    }
    // Return all URLs data in database
    public List<UrlData> getAllURLsData() {
        MongoCursor<Document> cur = urlsCollection.find(new BasicDBObject("indexed",0)).cursor();
        List<UrlData> DataList = new ArrayList<>();
        while (cur.hasNext()) {
            Document doc = cur.next();
            UrlData currentData = new UrlData((String)doc.get("url"),(String)doc.get("filepath"),(double)doc.get("popularity"));
            DataList.add(currentData);
        }
        return DataList;
    }

    public int getDocumentsSize() {
        BasicDBObject query = new BasicDBObject();
        int size = (int)urlsCollection.countDocuments(query);
        return size;
    }
}
