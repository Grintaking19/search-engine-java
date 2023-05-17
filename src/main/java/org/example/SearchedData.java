package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
@Data
@ToString

public class SearchedData {

    int total_number;
    List<page> pages;

    public SearchedData(int total_number) {
        this.total_number = total_number;
        pages=new ArrayList<>();
    }
    @AllArgsConstructor
    @Data
    @ToString
    public class page {
        String title;
        String Discription;
        String url;
    }

    public void addPage(  String title,String Discription, String url){
        pages.add(new page(title,Discription,url));
    }

}
