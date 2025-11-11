package com.tzy.mianshiyuan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tzy.mianshiyuan.mapper")
public class MianshiyuanApplication {

    public static void main(String[] args) {
        SpringApplication.run(MianshiyuanApplication.class, args);
    }

}
