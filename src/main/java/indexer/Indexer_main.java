package indexer;

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
            mongoLogger.setLevel(Level.SEVERE);
          //  RankerDBManager RankerDB = new RankerDBManager();
            IndexDBManager SearchIndexDB = new IndexDBManager();
            ///Run PageRanker Algorithms
           // pageRanker ranker = new pageRanker(1,0.85);
        List<UrlData> toBeIndexed =  new ArrayList<>();
          //  for (int i=0;i<3;i++){
                toBeIndexed.add(new UrlData("fetch-request-with-token-in-header.htm","D:\\CUFE\\apt\\project\\searchEngine\\src\\main\\java\\files\\fetch-request-with-token-in-header.htm",5));
            //}

            ExecutorService executor = Executors.newFixedThreadPool(20);
            for(int i = 0 ; i < toBeIndexed.size() ; i++)
            {
                executor.execute(new Indexer(toBeIndexed.get(i),SearchIndexDB));
            }
            executor.shutdown();




    }
}
