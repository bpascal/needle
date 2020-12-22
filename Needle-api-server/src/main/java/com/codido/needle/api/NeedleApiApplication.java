package com.codido.needle.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.codido.needle.core.mapper")
public class NeedleApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeedleApiApplication.class, args);
    }

}
