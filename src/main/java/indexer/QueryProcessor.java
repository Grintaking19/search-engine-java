package indexer;

//import Crawler.Database;
//import Ranker.Ranker;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import Ranker.RankerDBManager;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class QueryProcessor {
    IndexDBManager SearchIndexDB;
  //  RankerDBManager RankerDB;
    public QueryProcessor() throws UnknownHostException {
        SearchIndexDB = new IndexDBManager();
       // RankerDB = new RankerDBManager();
    }

    public LinkedHashMap<UrlData, Double> getResults(String S) throws IOException {
        //Check if it is a phaseSearch or not

        List <String> ListOfWords = List.of(S.replaceAll("\"","").split(" "));
        List <String> ListOfQueries = new ArrayList<>();
        PorterStemmer Stemmer = new PorterStemmer();
        List <SearchWord> searchedWords = new ArrayList<>();
        for(String Word : ListOfWords)
        {
            searchedWords.add(SearchIndexDB.getSearchWordExact(Stemmer.stemWord(Word)));
        }
        //if it is a phrase update
        if(S.startsWith("\"") && S.endsWith("\""))
        {
            S = S.replaceAll("\"","");
            searchedWords = PhraseSearch(searchedWords,S);
        }
       // Ranker rank = new Ranker(RankerDB.getDocumentsSize(), searchedWords);
        //LinkedHashMap<String, Double> reverseSortedMap = rank.sortSearched();
        return null ;//reverseSortedMap;
    }


    //Function very bad needs to be refactored
    public List<SearchWord> PhraseSearch(List <SearchWord> searchWords , String Phase) throws IOException {
        List <SearchWord> PhraseSearchWord = new ArrayList<>();
        for(SearchWord searchWord : searchWords){
            SearchWord SWord = new SearchWord();
            SWord.word = searchWord.word;
            for(WordData url : searchWord.data){
                //Now we will check if this Phase exists exactly in the Document we will Search everything
                //We will Get first Sentence that contains this word
                File input = new File(url.filepath);
                Document doc = Jsoup.parse(input,"UTF-8");
                Boolean isFound = false;
                if(doc.title().contains(Phase))
                {
                    SWord.data.add(url);
                    continue;
                }
                //Check P
                List<String> ParagraphText = doc.getElementsByTag("p").eachText();
                for(String Paragraph : ParagraphText)
                {
                    if(Paragraph.contains(Phase))
                    {
                        SWord.data.add(url);
                        isFound = true;
                        break;
                    }
                }
                if(isFound)
                {
                    continue;
                }
                //Check span
                ParagraphText = doc.getElementsByTag("span").eachText();
                for(String Paragraph : ParagraphText)
                {
                    if(Paragraph.contains(Phase))
                    {
                        SWord.data.add(url);
                        isFound = true;
                        break;
                    }
                }
                if(isFound)
                {
                    continue;
                }
                //Check Headers
                Elements docBodyElements = doc.body().getAllElements();
                //Since we only have six h levels we will loop over them
                for(int i = 0 ; i < 6 ; i++)
                {
                    Elements headerElements = docBodyElements.select("h"+(i+1));
                    List<String> Text = headerElements.eachText();
                    for(String Paragraph : Text)
                    {
                        if(Paragraph.contains(Phase))
                        {
                            SWord.data.add(url);
                            isFound = true;
                            break;
                        }
                    }
                    if(isFound)
                    {
                        break;
                    }
                }
                if(isFound)
                {
                    continue;
                }
                //Check li
                ParagraphText = doc.getElementsByTag("li").eachText();
                for(String Paragraph : ParagraphText)
                {
                    if(Paragraph.contains(Phase))
                    {
                        SWord.data.add(url);
                        isFound = true;
                        continue;
                    }
                }
                if(isFound)
                {
                    continue;
                }
                //check dt
                ParagraphText = doc.getElementsByTag("dt").eachText();
                for(String Paragraph : ParagraphText)
                {
                    if(Paragraph.contains(Phase))
                    {
                        SWord.data.add(url);
                        break;
                    }
                }
                if(isFound)
                {
                    continue;
                }
            }
            PhraseSearchWord.add(SWord);
        }

        return PhraseSearchWord;
    }

    public String getDescription(Document doc , String word){
        for (Element metaTag : doc.getElementsByTag("meta")) {
            if (metaTag.attr("name").toLowerCase().equals("description")) {
                //Get the Description
                String Description = metaTag.attr("content");
                if(Description.isEmpty())
                    break;
                return Description;
            }
        }
        //We will Get first Sentence that contains this word
        if(doc.title().contains(word))
            return doc.title();
        //Check P
        List<String> ParagraphText = doc.getElementsByTag("p").eachText();
        for(String Paragraph : ParagraphText)
        {
            if(Paragraph.contains(word))
            {
                return Paragraph;
            }
        }
        //Check span
        ParagraphText = doc.getElementsByTag("span").eachText();
        for(String Paragraph : ParagraphText)
        {
            if(Paragraph.contains(word))
            {
                return Paragraph;
            }
        }
        //Check Headers
        Elements docBodyElements = doc.body().getAllElements();
        //Since we only have six h levels we will loop over them
        for(int i = 0 ; i < 6 ; i++)
        {
            Elements headerElements = docBodyElements.select("h"+(i+1));
            List<String> Text = headerElements.eachText();
            for(String Paragraph : Text)
            {
                if(Paragraph.contains(word))
                {
                    return Paragraph;
                }
            }
        }
        //Check li
        ParagraphText = doc.getElementsByTag("li").eachText();
        for(String Paragraph : ParagraphText)
        {
            if(Paragraph.contains(word))
            {
                return Paragraph;
            }
        }
        //check dt
        ParagraphText = doc.getElementsByTag("dt").eachText();
        for(String Paragraph : ParagraphText)
        {
            if(Paragraph.contains(word))
            {
                return Paragraph;
            }
        }
        return "No Description";
    }

    public static void main(String[] args) throws IOException {

    }
}
