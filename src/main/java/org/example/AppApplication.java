package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;


@SpringBootApplication
public class AppApplication {

    public static void main(String[] args) {



        SpringApplication.run(AppApplication.class, args);
        System.out.println("hu");

    }

}
