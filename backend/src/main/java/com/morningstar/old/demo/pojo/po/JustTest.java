package com.morningstar.old.demo.pojo.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("just_test")
public class JustTest {
    private Integer id;
}
