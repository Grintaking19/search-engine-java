package Ranker;
import java.net.UnknownHostException;
import java.util.*;
import Crawler.MongoDBClass;
import indexer.UrlData;
//import indexer.IndexDBManager; (To be updated)
public class PageRanker
{
    MongoDBClass d;
    {
        d= new MongoDBClass();
    }
    // This is used to call functions applied to the indexer database
    DBRanker RankerDB;
    {
        RankerDB= new DBRanker();
    }
    // Key is the url and value is url_data through which we can access population
    public Map<String, UrlData> popularity_Map= new HashMap<String,UrlData>();
    // Key is the url and the value is set of parent urls referring to it
    public Map<String, Set<String>> parents_urls_Map= new HashMap<String,Set<String>>();
    public Map<String, Integer> refrenced_urls_from_parent= new HashMap<String,Integer>();
    public PageRanker(int iterations,double damping_factor) // Number of iteration to stop and damping factor is 0.85 at default
    {

        Set <String> Parents = null;  // To be updated
        Set <String> refrences = null; // To be updated with function
        double pagerank =0; // Initialized by zero
        //ArrayList<MongoDBClass.VisitedUrlData> d=new ArrayList<>();
        List<MongoDBClass.VisitedUrlData> urls=this.d.getVisitedUrlsFromCollection();
        for(MongoDBClass.VisitedUrlData url : urls)
        {
            this.refrenced_urls_from_parent.put(url.getUrl(), url.getCountChildren());
            this.parents_urls_Map.put(url.getUrl(), url.getParents());   ////// To be Updated with karim's Crawler Function
        }
            for(int i=0;i<iterations;i++)
            {
                for(MongoDBClass.VisitedUrlData url : urls)
                {
                    for(String parent : parents_urls_Map.get(url.getUrl()))
                    {
                        pagerank+=((popularity_Map.get(parent)==null)?0:popularity_Map.get(parent).popularity)/refrenced_urls_from_parent.get(parent);
                    }
                    double rank=(1-damping_factor)+damping_factor*(pagerank);
                    UrlData currentdata = new UrlData(url.getUrl(), url.getFilePath(), rank);
                    popularity_Map.put(url.getUrl(),currentdata);
                    pagerank=0;
                }
            }
        this.RankerDB.insertUrlMap(popularity_Map);
    }
}
