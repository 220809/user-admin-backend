package com.dingzk.useradmin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dingzk.useradmin.mapper")
public class UserAdminBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAdminBackendApplication.class, args);
    }

}
