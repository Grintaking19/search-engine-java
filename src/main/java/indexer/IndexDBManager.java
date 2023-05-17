package indexer;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IndexDBManager {
    com.mongodb.MongoClient mongoClient;
    MongoDatabase SearchIndexDB;
    MongoCollection<Document> wordsCollection ;
    MongoCollection<Document> spamCollection;
    MongoCollection<Document> historyCollection;

    public IndexDBManager() {

        com.mongodb.MongoClient mongoClient = new com.mongodb.MongoClient("localhost", 27017);

        MongoDatabase database = mongoClient.getDatabase("Indexer");

        this.mongoClient  = mongoClient;
        SearchIndexDB =  database;
        wordsCollection = SearchIndexDB.getCollection("keywords");
        spamCollection = SearchIndexDB.getCollection("Spam");
        historyCollection = SearchIndexDB.getCollection("history");
    }


    public void insertDocumentMap(Map<String,WordData> DocumentMap, UrlData currentData){
        //First we need delete all data related to this index then add the need data
        wordsCollection.deleteMany(new BasicDBObject("url",currentData.URL));
        List<Document> indexerEntry = new ArrayList<>();
        for(Map.Entry<String, WordData> entry: DocumentMap.entrySet())
        {
            indexerEntry.add(new Document("word", entry.getKey())
                    .append("url", entry.getValue().url)
                    .append("count", entry.getValue().count)
                    .append("lengthOfDocument", entry.getValue().lengthOfDoc)
                    .append("popularity" , currentData.popularity)
                    .append("filepath" , currentData.FilePath)
                    .append("position", entry.getValue().position));
        }
        wordsCollection.insertMany(indexerEntry);
    }

    public SearchWord getSearchWordExact(String keyword) {
        MongoCursor<Document> cur =  wordsCollection.find(new BasicDBObject("word", keyword)).cursor();
        SearchWord searchWord = new SearchWord();
        searchWord.word = keyword;
        while (cur.hasNext()) {
            Document doc = cur.next();
            WordData currentData = new WordData((Document) doc.get("position"));
            currentData.url = (String) doc.get("url");
            currentData.count = (int) doc.get("count");
            currentData.lengthOfDoc = (int) doc.get("lengthOfDocument");
            currentData.popularity = (double) doc.get("popularity");
            currentData.filepath = (String) doc.get("filepath");
            searchWord.data.add(currentData);
        }
        searchWord.df = searchWord.data.size();
        return searchWord;
    }
}
