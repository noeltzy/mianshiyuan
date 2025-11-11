package com.tzy.mianshiyuan;

import com.tzy.mianshiyuan.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MianshiyuanApplicationTests {

    @Resource
    UserMapper userMapper;
    @Test
    void contextLoads() {
        Long l = userMapper.selectCount(null);
        System.out.println(l);

    }

}
