package com.somoa.serviceback;

import com.somoa.serviceback.global.config.BlockHoundCustomConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class ServiceBackApplication {

    public static void main(String[] args) {
        BlockHound.install(new BlockHoundCustomConfiguration());
        SpringApplication.run(ServiceBackApplication.class, args);
    }

}
