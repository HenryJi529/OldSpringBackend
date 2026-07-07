package com.morningstar.old.demo.mapper;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class JustTestTest {
    @Autowired
    private JustTestMapper justTestMapper;

    @Test
    public void test() {
        System.out.println(justTestMapper.selectById(1));
        System.out.println(justTestMapper.selectRandomN(1));
    }

}
