package Crawler;//package src.Crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// we must implement runnable as this is a multithreaded web crawler
public class crawler implements Runnable{
    //max depth of crawling
//    private static final int MAX_DEPTH=3;
    MongoDBClass dataBaseCrawler;
    private static final int MAX_NUMBER_OF_LINKS=6000;
    private Object countUrls;
    private ArrayList<String> urlsQueue = new ArrayList<>();
    private int id;
    private ArrayList<String> visitedUrls=new ArrayList<>();
    private ArrayList<Set<String>> parentsList=new ArrayList<>();
    private ArrayList<Set<String>> visitedUrlsParentsList=new ArrayList<>();
    private ArrayList<Integer> countChildUrlsList=new ArrayList<>();
    //the 2 locks Objects on visitedUrls and urlsQueue to prevent race condition
    private final Object lockVisitedUrls;
    private final Object lockUrlsQueue;


    //constructor
    public crawler(MongoDBClass dataBaseCrawler,ArrayList<String> visitedUrls,Object lockVisitedUrls,ArrayList<Set<String>> visitedUrlsParentsList, ArrayList<String> urlsQueue, Object lockUrlsQueue,ArrayList<Set<String>> parentsList, int num,ArrayList<Integer>childCountInVisited, Object countUrls){
        this.dataBaseCrawler=dataBaseCrawler;
        this.urlsQueue=urlsQueue;
        this.visitedUrls=visitedUrls;
        this.id=num;
        this.countUrls=countUrls;
        this.lockVisitedUrls=lockVisitedUrls;
        this.lockUrlsQueue=lockUrlsQueue;
        this.parentsList=parentsList;
        this.visitedUrlsParentsList=visitedUrlsParentsList;
        this.countChildUrlsList=childCountInVisited;


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
        boolean limitReached;
        boolean linkExist;
        boolean isQueueHasElements;
        int indexPopedUrlInVisited;
        int indexNextUrlInVisited;
        Set<String>parentsSet;
        int countChildren;
        //while the queue of urls still have urls then crawl the top of the queue
        synchronized (lockUrlsQueue){
            isQueueHasElements=! this.urlsQueue.isEmpty();

        }

        while(isQueueHasElements){
            //check if we reached the MAX_NUMBER_OF_LINKS crawled
            synchronized (this.lockVisitedUrls) {
                limitReached = visitedUrls.size() >= MAX_NUMBER_OF_LINKS;
            }
            if (limitReached) {
                System.out.println(visitedUrls.size());
                break;
            }
            //this must be synchronized to avoid that 2 threads pop same element
            synchronized (this.lockUrlsQueue){
                //remove the first Url from the queue and remove its set of parents
                if(!this.urlsQueue.isEmpty()){
                    popedUrl=urlsQueue.remove(0);
                    parentsSet=this.parentsList.remove(0);
                    System.out.println("popped id=    "+id+"   "+popedUrl);
                    //apply removal of the "popedUrl" on database collection
                    //******this.dataBaseCrawler.deleteUrlFromUrlsQueueCollection(popedUrl);
                }
                else{
                    //urlsQueue is empty
                    System.out.println("id=    "+id+"  terminated");
                    return;
                }
            }


            //get the request from the url
            doc=request(popedUrl);
            if(doc !=null){
                //download html page
                filePath=downloadHtmlDoc(popedUrl);
                if(filePath !=""){
                    //Mark the Url as visited
                    //we want to add this popedurl to the collection of htmlDocs in the database
                    synchronized (this.lockVisitedUrls){
                        if(this.visitedUrls.contains(popedUrl)==false && this.visitedUrls.size()<MAX_NUMBER_OF_LINKS) {
                            System.out.println("visited     "+popedUrl);
                            //apply the insertion of popedUrl and its ParentsUrls on the collection of database
                            this.dataBaseCrawler.insertVisitedUrlRecord(popedUrl, filePath,parentsSet);
                            //add the popped url to visitedUrls list
                            this.addUrlToVisitedUrls(popedUrl);
                            //add the poped Parents set to the visistedUrlsParentsList
                            this.visitedUrlsParentsList.add(parentsSet);
                            this.countChildUrlsList.add(0);
                        }
                        else{
                            indexPopedUrlInVisited=this.visitedUrls.indexOf(popedUrl);

                            // Get the Set at index and add a new String to it
                            Set<String> set = visitedUrlsParentsList.get(indexPopedUrlInVisited);
                            set.add(popedUrl);

                            // Set the updated Set back into the ArrayList
                            this.visitedUrlsParentsList.set(indexPopedUrlInVisited, set);

                            this.dataBaseCrawler.updateParentsListInVisitedUrlRecord(popedUrl,set);
                            continue;
                        }
                    }

                }else{
                    System.out.println("Error "+popedUrl+" empty filePath!   "+filePath);
                    continue;
                }
                countChildren=0;
                synchronized (lockVisitedUrls){
                    indexPopedUrlInVisited=this.visitedUrls.indexOf(popedUrl);
                }

                //loop on the urls inside the popedUrl
                for(Element link: doc.select("a[href]")) {
                    next_link=link.absUrl("href");
                    //check if this link is visited before inserting in urlsQueue
                    //normalize the url
                    try {
                        next_link=normalizeUrl(next_link);
                    } catch (URISyntaxException e) {
                        System.out.println("URISyntaxException in normaaaaaaaa   "+next_link);
                        //throw new RuntimeException(e);
                        continue;
                    } catch (MalformedURLException e) {
                        System.out.println("MalformedURLException in normaaaaaaaaaaa   "+next_link);
                        //throw new RuntimeException(e);
                        continue;
                    }
                    if(next_link!="") {
                        synchronized (this.lockVisitedUrls) {
                            linkExist = this.visitedUrls.contains(next_link);
                        }
                        //increment number of links that popedUrl points to
                        countChildren++;
                        //if the child link does not exist in the visitedUrls so insert it into urlsQueue
                        if (!linkExist) {
                            synchronized (this.lockUrlsQueue){
                                if(!urlsQueue.contains(next_link)){
                                    //System.out.println("add     "+next_link);
                                    urlsQueue.add(next_link);
                                    //add the popedUrl to its parents set
                                    Set<String> set = new HashSet<>();
                                    set.add(popedUrl);
                                    parentsList.add(set);
                                    //******this.dataBaseCrawler.insertUnvisitedUrlInQueue(next_link);
                                }
                            }
                        }
                        else{
                            //link is already visited
                            //update its parents list ; add the popedUrl to this list
                            synchronized (lockVisitedUrls) {

                                indexNextUrlInVisited=this.visitedUrls.indexOf(next_link);

                                // Get the Set at index and add a new String to it
                                Set<String> set = visitedUrlsParentsList.get(indexNextUrlInVisited);
                                set.add(popedUrl);

                                // Set the updated Set back into the ArrayList
                                this.visitedUrlsParentsList.set(indexNextUrlInVisited, set);

                                this.dataBaseCrawler.updateParentsListInVisitedUrlRecord(next_link,set);
                            }

                        }
//                        synchronized (this.visitedUrls) {
//                            if (this.visitedUrls.contains(next_link) == false) {
//                                //crawl(level+1,next_link);
//                                urlsQueue.add(next_link);
//                            }
//                        }
                    }

                }
                synchronized (lockVisitedUrls) {
                    //now we have the count available
                    this.countChildUrlsList.set(indexPopedUrlInVisited, countChildren);
                    //apply the insertion of popedUrl and its ParentsUrls on the collection of database
                    this.dataBaseCrawler.updateChildCountInVisitedUrl(popedUrl,countChildren);
                }
            }
            //update the boolean isQueueHasElements
            synchronized (this.lockUrlsQueue){
                isQueueHasElements=! this.urlsQueue.isEmpty();
            }
        }
        System.out.println("emptyyyy queue "+Thread.currentThread().getName());
    }

    public String normalizeUrl(String url) throws URISyntaxException, MalformedURLException {
        String normalizedUrl="";
        try {
            URI uri = new URI(url).normalize();
            normalizedUrl = uri.toURL().toString();
        } catch (URISyntaxException | IllegalArgumentException e) {
            System.out.println("Invalid URL in normalize : " + url);
        }
        return normalizedUrl;
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

            System.out.println("MalformedURL Exception occurred i did not download ");
        } catch (IOException ie) {
            System.out.println("IOException occurred i did not download");
        }
        System.out.println(" i did not download");
        return"";
    }
    //this function takes a url and lock the visitedUrls list to forbid race condition
    private  void addUrlToVisitedUrls(String url){
//        if(this.visitedUrls.contains(url)==false && this.visitedUrls.size()<MAX_NUMBER_OF_LINKS)
//        {

                this.visitedUrls.add(url);

//        }

    }
//    private  void incrementCountUrlsAndCheckMax(){
//        synchronized(countUrls){
//            this.countUrls+=1;
//        }
//    }

    public static String getRobotTxt(String link) throws IOException {
        URL url = new URL("https://"+link + "/robots.txt");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder robotsContent = new StringBuilder();
            while ((line = in.readLine()) != null) {
                robotsContent.append(line).append("\n");
            }
            in.close();
            return robotsContent.toString();
        } else {

            System.out.println("Failed to get robots.txt file. Response code: " + responseCode +" "+link);
            return "";
        }
    }

    public static String getDomainName(String url)  {


        URL uri = null;
        try {
            //String encodedUrl = url.replaceAll("=", "%20");
            //encodedUrl = URLEncoder.encode(encodedUrl, "UTF-8");
            uri = new URL(url);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//            //return "";
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
//        } catch (UnsupportedEncodingException e) {
//            //return "";
//            throw new RuntimeException(e);
//        }
        String domainName = uri.getHost();
        return domainName;
    }


    private static boolean isUrlDisallowed(String url) {
        String hostUrl=getDomainName(url);
        String robotsContent= null;
        try {
            robotsContent = getRobotTxt(hostUrl);
        } catch (IOException e) {
            System.out.println("IOException i could not reayd robotsss.txtx host is: "+hostUrl);
            return true;//yes you are disallowed
            //throw new RuntimeException(e);
        }
        if(robotsContent==""){
            //yes you are disallowed
            System.out.println("emptyyyyy i could not reayd robotsss.txtx host is::"+hostUrl);
            return true;
        }
        String[] lines = robotsContent.split("\n");
        String userAgent = ".*"; // match any user agent
        boolean isUserAgentMatched = false;
        Pattern pattern;

        for (String line : lines) {
            line = line.trim();
            if (line.toLowerCase().startsWith("user-agent")) {
                pattern = Pattern.compile("^user-agent:\\s*(.*)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    if (userAgent.equals(matcher.group(1)) || userAgent.equals(".*")) {
                        isUserAgentMatched = true;
                    } else {
                        isUserAgentMatched = false;
                    }
                }
            } else
             if (line.toLowerCase().startsWith("disallow")) {
                 if (isUserAgentMatched) {
                     pattern = Pattern.compile("^disallow:\\s*(.*)", Pattern.CASE_INSENSITIVE);
                     Matcher matcher = pattern.matcher(line);
                     if (matcher.matches()) {
                         String disallowedUrl = matcher.group(1);
                         if (url.endsWith(disallowedUrl)) {
                             // url is disallowed
                             System.out.println("FFFFFFFFFFFFFFF DISALAOED");
                             return true;
                         }
                     }
                 }
            }
        }
        // url is allowed
        return false;
    }

    private Document request(String url) {
        try{
            Connection con= Jsoup.connect(url);
            Document doc=con.get();
            //check first if the url is allowed for the agent
            if(isUrlDisallowed(url)){
                return null;
            }
            if (con.response().statusCode() == 200) {
                //System.out.println("\n**bot id: "+id+" recieved webpage at url: "+url);
                //String title= doc.title();
                //System.out.println(title);
                //this.visitedUrls.add(url);
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
        ArrayList<String>seedUrlsQueue= new ArrayList<>();
        try {
            File myObj = new File("D:\\CUFE\\Spring 2023\\apt\\intellijprojects\\search-engine\\src\\main\\java\\Crawler\\seedList.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String url = myReader.nextLine();
                seedUrlsQueue.add(url);
            }
        } catch(FileNotFoundException e) {
            System.out.println("Error! Seed set file not found.");
            e.printStackTrace();
        }
        return seedUrlsQueue;
    }


    public static void main(String[] args) throws InterruptedException {
        //create an instance of the database

        MongoDBClass dataBaseCrawler=new MongoDBClass();

        //create threads list
        ArrayList<Thread> botsList=new ArrayList<>();
        //create allData list
        ArrayList<MongoDBClass.VisitedUrlData> allData=new ArrayList<>();
        //create visitedUrls list
        ArrayList<String> visitedUrls=new ArrayList<>();
        //create visitedUrls list
        ArrayList<String> fileP =new ArrayList<>();
        ArrayList<Set<String>> parentss=new ArrayList<>();
        ArrayList<Integer> counttt=new ArrayList<>();
        //create

        //fill the visitedUrls with the data in the collection of database (if exists)
        allData=dataBaseCrawler.getVisitedUrlsFromCollection();
        for(MongoDBClass.VisitedUrlData record: allData){
            visitedUrls.add(record.getUrl());
            fileP.add(record.getFilePath());
            parentss.add(record.getParents());
            counttt.add(record.getCountChildren());

        }

        //create seedUrls list
        ArrayList<String> urlsQueue;
        //fill the urlsQueue with the data in the collection of database (if exists)
        urlsQueue=dataBaseCrawler.getUrlsQueueFromCollection();
        //if the collection of urlsQueue was initially empty, so we want to fill data from seedSet
        if(urlsQueue.isEmpty()){
            //read seed list file and put it in list
            urlsQueue = readSeedUrlsFile();
            //if the seed set file was empty also so exit
            if (urlsQueue.isEmpty()) {
                System.out.println("Error! seed set file is empty!");
                return;
            }else {
                //if we loaded from the seedSet so update the collection in db
                // insert all the urls in the seedSet to the collection "urlsQueueCollection"
                for (String url : urlsQueue) {
                    dataBaseCrawler.insertUnvisitedUrlInQueue(url);
                }
            }
        }
        //
        ArrayList<Set<String>> parentsList=new ArrayList<Set<String>>();
        // Add 12 empty Sets to the ArrayList
        for (int i = 0; i < 12; i++) {
            parentsList.add(new HashSet<String>());
        }
        ArrayList<Set<String>> visitedUrlsParentsList=new ArrayList<Set<String>>();
        //create a counter to count number of links crawled
        Object countUrls=new Object();
        ArrayList<Integer> childCountInVisited=new ArrayList<Integer>();

        //number of threads that will run the crawler (entered by the user)
        int numThreads;
        // Create a new Scanner instance to read user input
        Scanner scanner = new Scanner(System.in);
        // Prompt the user to enter an integer
        System.out.print("Please enter an integer: ");
        // Read the user input as an integer
        numThreads = scanner.nextInt();
        if(numThreads==0) {
            System.out.println("Error! Number of threads entered is "+urlsQueue.size());
            return;
        }
        //create lock object to prevent race condition when writing and reading from visitedUrls in both (collection database and local arrayList)
        Object lockVisitedUrls=new Object();
        //create lock object to prevent race condition when writing and reading from urlsQueue in both (collection database and local arrayList)
        Object lockUrlsQueue=new Object();
        int j=0;
        //loop on numThreads and create a new thread
        for (int i = 0; i < numThreads; i++) {
            Thread threadInst = new Thread(new crawler(dataBaseCrawler, visitedUrls,lockVisitedUrls,parentss, urlsQueue,lockUrlsQueue,parentsList , i,childCountInVisited,countUrls));
            //set id of thread to be its index
            String id = Integer.toString(i);
            threadInst.setName(id);
            //start thread
            threadInst.start();
            if(i>10 )
            {
//                try {
//                    threadInst.sleep(1000);
//                }catch (InterruptedException e){
//
//                }
            }
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
