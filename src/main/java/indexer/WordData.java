package indexer;

import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

public class WordData {
    public int count = 0;
    public String url;
    public double popularity;
    public int lengthOfDoc;
    public String filepath;
    public Map<String, Integer> position= new HashMap<String, Integer>();

    public WordData(){
        position.put("title" , 0);
        position.put("h1" , 0);
        position.put("h2" , 0);
        position.put("h3" , 0);
        position.put("h4" , 0);
        position.put("h5" , 0);
        position.put("h6" , 0);
        position.put("body" , 0);
    }
    public WordData(Document object){
        position.put("title" , (Integer) object.get("title"));
        position.put("h1" , (Integer) object.get("h1"));
        position.put("h2" , (Integer) object.get("h2"));
        position.put("h3" , (Integer) object.get("h3"));
        position.put("h4" , (Integer) object.get("h4"));
        position.put("h5" , (Integer) object.get("h5"));
        position.put("h6" , (Integer) object.get("h6"));
        position.put("body" , (Integer) object.get("body"));
    }

}
