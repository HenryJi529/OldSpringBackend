package com.morningstar.old.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.morningstar.old.demo.pojo.po.JustTest;

public interface JustTestMapper extends BaseMapper<JustTest> {
    JustTest selectRandomN(int N);
}


