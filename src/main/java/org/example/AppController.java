package org.example;

import indexer.QueryProcessor;
import indexer.UrlData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


@RestController

public class AppController {


    @CrossOrigin(origins = "*")
    @PostMapping("/search")
    public SearchedData getSearchResults(@RequestBody searchReq req,
                                          @RequestParam("page") int page,
                                          @RequestParam("pageSize") int pageSize) throws IOException {
        System.out.println("hi");
        System.out.println(req);
        String query=req.query;
        QueryProcessor queryProcessor = new QueryProcessor();
        LinkedHashMap<UrlData, Double> reverseSortedMap = queryProcessor.getResults(query);

        List<UrlData> searchData = new ArrayList<>(reverseSortedMap.keySet());
        System.out.println(searchData);


       int count=searchData.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, count);

        if (startIndex >= endIndex) {
            // Invalid page, return an empty list or throw an exception
            return new SearchedData(count);
        }
        SearchedData searchedData=new SearchedData(count);
        List<UrlData> pages=searchData.subList(startIndex, endIndex);
        for (int i = 0; i < pages.size(); i++) {
            System.out.println("jhj");

            File input = new File((String)(System.getProperty("user.dir"))+"//html//"+pages.get(i).FilePath);
            Document CurrentDoc = Jsoup.parse(input,"UTF-8");
            searchedData.addPage(CurrentDoc.title(),queryProcessor.getDescription(CurrentDoc,query.replaceAll("\"","")),pages.get(i).URL);
        }
        System.out.println(searchedData);

        return searchedData;
    }
    @GetMapping("/")
    public String home() {
        return "Welcome to the home page!";
    }
}
