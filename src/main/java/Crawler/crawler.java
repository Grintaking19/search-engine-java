package Crawler;//package src.Crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

// we must implement runnable as this is a multithreaded web crawler
public class crawler implements Runnable{
    //max depth of crawling
//    private static final int MAX_DEPTH=3;
    MongoDBClass dataBaseCrawler;
    private static final int MAX_NUMBER_OF_LINKS=600;
    private Object countUrls;
    private Queue<String> urlsQueue = new LinkedList<>();
    private int id;
    private ArrayList<String> visitedUrls=new ArrayList<>();
    //constructor
    public crawler(MongoDBClass dataBaseCrawler,ArrayList<String> visitedUrls, String link, int num, Object countUrls){
        this.dataBaseCrawler=dataBaseCrawler;
        this.urlsQueue.add(link);
        this.visitedUrls=visitedUrls;
        this.id=num;
        this.countUrls=countUrls;


    }
    //as we implement runnable we need to override function run
    @Override
    public void run() {
        crawl();
    }

    private void crawl(){
        String popedUrl;
        Document doc;
        String next_link;
        String filePath;
        //while the queue of urls still have urls then crawl the top of the queue
        while(! this.urlsQueue.isEmpty()){
            //check if we reached the MAX_NUMBER_OF_LINKS crawled
            synchronized (this.countUrls) {
                if (visitedUrls.size() > MAX_NUMBER_OF_LINKS) {
                    System.out.println(visitedUrls.size());
                    break;
                }
            }
            popedUrl=urlsQueue.remove();

            doc=request(popedUrl);
            if(doc !=null){
                //download html page
                filePath=downloadHtmlDoc(popedUrl);
                if(filePath !=""){
                    //we want to add this popedurl to the collection of htmlDocs in the database
                    synchronized (this.dataBaseCrawler){
                        this.dataBaseCrawler.insertRecord(popedUrl,filePath);
                    }
                }
                else{
                    System.out.println("Error empty link!");
                }
                //add the popped url to visitedUrls list
                this.addUrlToVisitedUrls(popedUrl);
                for(Element link: doc.select("a[href]")) {
                    next_link=link.absUrl("href");
                    //check if this link is visited before inserting in urlsQueue
                    if(this.visitedUrls.contains(next_link)==false){
                        //crawl(level+1,next_link);
                        urlsQueue.add(next_link);
                    }
                }
            }

        }
    }
    public static String removeSpecialCharacters(String str) {
        // Using regex to replace all special characters with an empty string
        return str.replaceAll("[^a-zA-Z0-9 ]", "");
    }
    public static String trimLastNCharacters(String str) {
        if (230 >= str.length()) {
            return str;
        }
        return str.substring(str.length() - 230);
    }
    public static String downloadHtmlDoc(String link) {
        try {
            String filePath = removeSpecialCharacters(trimLastNCharacters(link)) + ".html";
            URL url = new URL(link);
            BufferedReader readHtml = new BufferedReader(new InputStreamReader(url.openStream()));
            BufferedWriter writeHtml = new BufferedWriter(new FileWriter("D:\\CUFE\\Spring 2023\\apt\\project\\htmlDocs\\" + filePath));

            // read each line from stream till end
            String docLine;

            while ((docLine = readHtml.readLine()) != null) {
                //write line to
                writeHtml.write(docLine);
            }
            readHtml.close();
            writeHtml.close();
            return filePath;
        }
        // Exceptions
        catch (MalformedURLException mue) {
            System.out.println("MalformedURL Exception occurred");
        } catch (IOException ie) {
            System.out.println("IOException occurred");
        }
        return"";
    }
    //this function takes a url and lock the visitedUrls list to forbid race condition
    private  void addUrlToVisitedUrls(String url){
        if(this.visitedUrls.contains(url)==false && this.visitedUrls.size()<MAX_NUMBER_OF_LINKS)
        {
            synchronized(visitedUrls){
                this.visitedUrls.add(url);
            }
        }

    }
//    private  void incrementCountUrlsAndCheckMax(){
//        synchronized(countUrls){
//            this.countUrls+=1;
//        }
//    }


    private Document request(String url) {
        try{
            Connection con= Jsoup.connect(url);
            Document doc=con.get();
            if(con.response().statusCode()==200){
                System.out.println("\n**bot id: "+id+" recieved webpage at url: "+url);
                String title= doc.title();
                System.out.println(title);
                this.visitedUrls.add(url);
                return doc;
            }
            return null;
        }
        catch (IOException e)
        {
            return null;
        }
    }

    static ArrayList<String> readSeedUrlsFile() {
        ArrayList<String>seedUrlsList=new ArrayList<>();
        try {
            File myObj = new File("D:\\CUFE\\Spring 2023\\apt\\intellijprojects\\search-engine\\src\\main\\java\\Crawler\\seedList.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String url = myReader.nextLine();
                seedUrlsList.add(url);
            }
        } catch(FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return seedUrlsList;
    }


    public static void main(String[] args) throws InterruptedException {
        //create an instance of the database
        MongoDBClass dataBaseCrawler=new MongoDBClass();

        //create threads list
        ArrayList<Thread> botsList=new ArrayList<>();
        //create visitedUrls list
        ArrayList<String> visitedUrls=new ArrayList<>();
        //create seedUrls list
        ArrayList<String> seedUrls;
        //create a counter to count number of links crawled
        Object countUrls=new Object();

        //read seed list file and put it in list
        seedUrls=readSeedUrlsFile();
        int numThreads=10;
        System.out.println(seedUrls.size());
        if(seedUrls.isEmpty()) {
           return;
        }
        //loop on numThreads and create a new thread
        for (int i = 0; i < numThreads; i++) {
            Thread threadInst = new Thread(new crawler(dataBaseCrawler, visitedUrls, seedUrls.get(i),  i,countUrls));
            //set id of thread to be its index
            String id = Integer.toString(i);
            threadInst.setName(id);
            //start thread
            threadInst.start();
            //add thread to the botsList
            botsList.add(threadInst);
        }
        //join all threads
        for(int i = 0; i < numThreads; i++){
            try{
                botsList.get(i).join();
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }

    }

}
