package Crawler;//package src.Crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;


// we must implement runnable as this is a multithreaded web crawler
public class crawler implements Runnable{
    //max depth of crawling
//    private static final int MAX_DEPTH=3;
    private static final int MAX_NUMBER_OF_LINKS=6000;
    private Object countUrls;
    private Queue<String> urlsQueue = new LinkedList<>();
    private int id;
    private ArrayList<String> visitedUrls=new ArrayList<String>();
    //constructor
    public crawler(ArrayList<String> visitedUrls, String link, int num, Object countUrls){
        this.urlsQueue.add(link);
        this.visitedUrls=visitedUrls;
        this.id=num;
        this.countUrls=countUrls;

    }
    //as we implement runnable we need to override function run
    @Override
    public void run() {
        crawl(1);
    }

    private void crawl(int level){
        //while the queue of urls still have urls then crawl the top of the queue
        while(! this.urlsQueue.isEmpty()){
            //check if we reached the MAX_NUMBER_OF_LINKS crawled
            synchronized (this.countUrls) {
                if (visitedUrls.size() > MAX_NUMBER_OF_LINKS) {
                    System.out.println(visitedUrls.size());
                    break;
                }
            }
            String popedUrl=urlsQueue.remove();

            //System.out.println("level= "+level+"\n");
            Document doc=request(popedUrl);
            if(doc !=null){
                //add the popped url to visitedUrls list
                this.addUrlToVisitedUrls(popedUrl);
                for(Element link: doc.select("a[href]")) {
                    String next_link=link.absUrl("href");
                    //check if this link is visited before inserting in urlsQueue
                    if(this.visitedUrls.contains(next_link)==false){
                        //crawl(level+1,next_link);
                        urlsQueue.add(next_link);
                    }
                }
            }

        }
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

    class counUrls{

    }


    public static void main(String[] args) throws InterruptedException {
        //create threads list
        ArrayList<Thread> botsList=new ArrayList<>();
        //create visitedUrls list
        ArrayList<String> visitedUrls=new ArrayList<String>();
        //create seedUrls list
        ArrayList<String> seedUrls=new ArrayList<String>();
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
            Thread threadInst = new Thread(new crawler( visitedUrls, seedUrls.get(i),  i,countUrls));
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
