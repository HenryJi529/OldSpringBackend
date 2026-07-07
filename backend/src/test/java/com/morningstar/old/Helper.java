package com.morningstar.old;

import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

public class Helper {
    @Test
    public void generateJwtSecret(){
        System.out.println(RandomStringUtils.randomAlphanumeric(SignatureAlgorithm.HS256.getMinKeyLength()));
    }
}
