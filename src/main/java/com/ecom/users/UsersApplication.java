package com.ecom.users;

import com.ecom.users.config.RsakeysConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(RsakeysConfig.class)
public class UsersApplication {

    public static void main(String[] args) {SpringApplication.run(UsersApplication.class, args);}


}
