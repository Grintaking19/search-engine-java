package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class AppController {

    @PostMapping
    public String getresult2() {
        System.out.println("3666");
        return null;//queryProcessor.getResults("hi");
    }

    @GetMapping("/search")
    public String getresult() {
        return "null";//queryProcessor.getResults("hi");

    }
    @GetMapping("/")
    public String home() {
        return "Welcome to the home page!";
    }
}
