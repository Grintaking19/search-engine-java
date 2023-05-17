package indexer;

import Ranker.DBRanker;
import Ranker.PageRanker;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;


import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Indexer_main {

    public static int[] docSize;

    public static void main(String[] args) throws IOException {

            Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );

        com.mongodb.MongoClient mongoClient = new com.mongodb.MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("Indexer");


        MongoCollection<Document> wordsCollection = database.getCollection("keywords");

        wordsCollection.deleteMany(new BasicDBObject("popularity",5));


        mongoLogger.setLevel(Level.SEVERE);
        DBRanker RankerDB = new DBRanker();
        IndexDBManager SearchIndexDB = new IndexDBManager();
            ///Run PageRanker Algorithms
            PageRanker ranker = new PageRanker(1,0.85);
        List<UrlData> toBeIndexed =RankerDB.getAllURLsData()  ;
          //  for (int i=0;i<3;i++){
              //  toBeIndexed.add(new UrlData("fetch-request-with-token-in-header.htm",(String)(System.getProperty("user.dir")) +"\\src\\main\\java\\files\\fetch-request-with-token-in-header.htm",5));
       // toBeIndexed.add(new UrlData("fetch-request-with-token-in.htm","D:\\CUFE\\apt\\project\\searchEngine\\src\\main\\java\\files\\fetch-request-with-token-in-header.htm",5));

        //}
        long time = (long) System.currentTimeMillis();
            ExecutorService executor = Executors.newFixedThreadPool(100);
            for(int i = 0 ; i < toBeIndexed.size() ; i++)
            {
                executor.execute(new Indexer(toBeIndexed.get(i),SearchIndexDB));
            }
            executor.shutdown();
        time = (long) System.currentTimeMillis() - time;
        System.out.println("Time taken to serialize invertedIndex matrix: " + time + " mSec.");




    }
}
