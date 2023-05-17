package Ranker;

import java.util.*;

import indexer.SearchWord;
import indexer.UrlData;

import static java.lang.Math.log;

public class RelevanceRanker
{
    // Array_of_words got from query processor (Still Not Done)
    public List<SearchWord> words;   // String is temporary but it must be a struct of search words coming from indexer
    // Number of documents retrieved
    public int ndocs;
    // List Of IDF of each word used in searching
    public double [] IDF;
    // Some weights added to prioritize data according to position in the document(double are used to be easily multiplied by TF-IDF value )
    public static final double titleweight = 12;
    public static final double h1weight = 10;
    public static final double h2weight = 8;
    public static final double h3weight = 6;
    public static final double h4weight = 4;
    public static final double h5weight = 2;
    public static final double h6weight = 1.5;
    public static final double bodyweight = 1;
    // Hash map to map each url with IF_IDF value
    public Map<String, Double> TF_IDF= new HashMap<>();
    // Hash map to retrieve the popularity to be multiplied with relevance of the pages
    //public Map<String, Double> popularityMap= new HashMap<>();
    // Total Result of multiplying TF_IDF with popularity
    public Map<UrlData, Double> Final_Result= new HashMap<>();
    // TF must be calculated for each document containing the word
    public double calculate_TF (int wordindex,int documentno)
    {
        double TF = this.words.get(wordindex).data.get(documentno).count / this.words.get(wordindex).data.get(documentno).lengthOfDoc;
        return TF;
    }
    // IDF is constant for each word,so I put it in an arraylist as it never changes for same word
    public void calculate_IDF (int wordindex)
    {
        if(this.words.get(wordindex).df!=0) {
            this.IDF[wordindex] = log(this.ndocs / this.words.get(wordindex).df);
        }else{
            this.IDF[wordindex]=0;
        }
    }
    // Constructor
    public RelevanceRanker(int docs,List<SearchWord> searchwords)
    {
        this.ndocs=docs;
        this.words=searchwords;
        this.IDF=new double[searchwords.size()];
        Total_Relevance_Calculation();
    }
    // Function to return the total weight of word as how many times it appeared in each part of a certain document
    public double calculate_weight(int wordindex,int docnum)
    {
        double title_weight = this.words.get(wordindex).data.get(docnum).position.get("title") * titleweight;
        double h1_weight = this.words.get(wordindex).data.get(docnum).position.get("h1") * h1weight;
        double h2_weight = this.words.get(wordindex).data.get(docnum).position.get("h2") * h2weight;
        double h3_weight = this.words.get(wordindex).data.get(docnum).position.get("h3") * h3weight;
        double h4_weight = this.words.get(wordindex).data.get(docnum).position.get("h4") * h4weight;
        double h5_weight = this.words.get(wordindex).data.get(docnum).position.get("h5") * h5weight;
        double h6_weight = this.words.get(wordindex).data.get(docnum).position.get("h6") * h6weight;
        double body_weight = this.words.get(wordindex).data.get(docnum).position.get("body") * bodyweight;
        //double final_weight = title_weight + h1_weight + h2_weight + h3_weight + h4_weight + h5_weight + h6_weight + body_weight;
        return title_weight + h1_weight + h2_weight + h3_weight + h4_weight + h5_weight + h6_weight + body_weight;
    }
    // The idea here is to
    public void Total_Relevance_Calculation()
    {
        for (int i=0;i<this.words.size();i++) // Loop on all search words
        {
            calculate_IDF(i); // IDF of this word is saved in IDF[i]
            for (int j=0;j<this.words.get(i).data.size();j++) // Loop on all documents in which this page appear
            {
                if(TF_IDF.get(words.get(i).data.get(j).url) != null)
                {
                    double current=TF_IDF.get(words.get(i).data.get(j).url);
                    TF_IDF.put(words.get(i).data.get(j).url,(calculate_TF(i,j)*IDF[i]*calculate_weight(i,j))+current);
                }
                else
                {
                    TF_IDF.put(words.get(i).data.get(j).url,calculate_TF(i,j)*IDF[i]*calculate_weight(i,j));
                }
                UrlData Current_URLData = new UrlData(words.get(i).data.get(j).url,words.get(i).data.get(j).filepath,words.get(i).data.get(j).popularity);
                //popularityMap.put(words.get(i).data.get(j).url ,words.get(i).data.get(j).popularity);
                Final_Result.put(Current_URLData,words.get(i).data.get(j).popularity * TF_IDF.get(words.get(i).data.get(j).url));
            }

        }
    }
        public LinkedHashMap <UrlData, Double> sort_documents()
    {
        List<Map.Entry<UrlData, Double>> list = new LinkedList<>(Final_Result.entrySet());
        Comparator<Map.Entry<UrlData, Double>> valueComparator = (e1, e2) -> e2.getValue().compareTo(e1.getValue());
        Collections.sort(list, valueComparator);

        // Linked Hash map is used as it maintains the order
        LinkedHashMap<UrlData, Double> reversedorder = new LinkedHashMap<>();
        for (Map.Entry<UrlData, Double> entry : list) {
            reversedorder.put(entry.getKey(), entry.getValue());
        }
        return reversedorder;
    }

}
