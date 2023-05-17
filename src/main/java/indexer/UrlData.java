package indexer;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jdk.jfr.Threshold;
import lombok.ToString;
import org.bson.Document;

import java.io.IOException;
import java.util.logging.Logger;

@ToString
public class UrlData {
    public String URL;
    public String FilePath;
    public double popularity;

    public UrlData(String URL, String filePath, double popularity) {
        this.URL = URL;
        FilePath = filePath;
        this.popularity = popularity;
    }

}
