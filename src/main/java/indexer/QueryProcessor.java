package indexer;

//import Crawler.Database;
//import Ranker.Ranker;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import Ranker.RankerDBManager;
import Ranker.DBRanker;
import Ranker.RelevanceRanker;
import lombok.AllArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class QueryProcessor {
    IndexDBManager SearchIndexDB;
    DBRanker RankerDB;

    public QueryProcessor() throws UnknownHostException {
        SearchIndexDB = new IndexDBManager();
        RankerDB = new DBRanker();
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
        System.out.println("jfdjfdjhfghjfhjkhgfdjkhdgkfjkjdf     searchedData");
        System.out.println(S);

        //if it is a phrase update
        if(S.startsWith("\"") && S.endsWith("\""))
        {
            System.out.println("searchedData");
            S = S.replaceAll("\"","");
            searchedWords = PhraseSearch(searchedWords,S);
        }
        RelevanceRanker  rank = new RelevanceRanker(RankerDB.getDocumentsSize(), searchedWords);
        LinkedHashMap<UrlData, Double> reverseSortedMap = rank.sort_documents();
        return reverseSortedMap;
    }


    //Function very bad needs to be refactored
    public List<SearchWord> PhraseSearch(List <SearchWord> searchWords , String Phase) throws IOException {
        Set<SearchWord> PhraseSearchWord = new HashSet<>();
        Set<String> check = new HashSet<>();

        for(SearchWord searchWord : searchWords){
            SearchWord SWord = new SearchWord();
            SWord.word = searchWord.word;
            for(WordData url : searchWord.data){
                //Now we will check if this Phase exists exactly in the Document we will Search everything
                //We will Get first Sentence that contains this word
                File input = new File((String)(System.getProperty("user.dir"))+"//html//"+url.filepath);
                Document doc = Jsoup.parse(input,"UTF-8");
                Boolean isFound = false;
                if(doc.title().contains(Phase))
                {
                    if(!check.contains(url.url)) {
                        check.add(url.url);
                        SWord.data.add(url);
                    }
                    continue;
                }
                //Check P
                List<String> ParagraphText = doc.getElementsByTag("p").eachText();
                for(String Paragraph : ParagraphText)
                {
                    if(Paragraph.contains(Phase))
                    {if(!check.contains(url.url)) {
                        check.add(url.url);
                         SWord.data.add(url);}
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
                        if(!check.contains(url.url)) {
                            check.add(url.url);
                        SWord.data.add(url);}
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
                        {if(!check.contains(url.url)) {
                            check.add(url.url);
                            SWord.data.add(url);}
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
                        if(!check.contains(url.url)) {
                            check.add(url.url);
                        SWord.data.add(url);}
                        isFound = true;
                        break;
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
                        if(!check.contains(url.url)) {
                            check.add(url.url);
                        SWord.data.add(url);}
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
        System.out.println("PhraseSearchWord  "+PhraseSearchWord);
        List<SearchWord> phraseSearchWordList = new ArrayList<>(PhraseSearchWord);
        return phraseSearchWordList;
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
