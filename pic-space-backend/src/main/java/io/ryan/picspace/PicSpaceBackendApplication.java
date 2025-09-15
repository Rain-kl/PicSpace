package io.ryan.picspace;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
public class PicSpaceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PicSpaceBackendApplication.class, args);
    }

}
