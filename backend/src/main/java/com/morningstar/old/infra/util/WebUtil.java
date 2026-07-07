package com.morningstar.old.infra.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.IOException;
import com.morningstar.old.infra.response.R;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class WebUtil {
    public static void renderJson(R<Object> r, HttpServletResponse response) {
        try {
            String respStr = JsonUtil.objectMapper().writeValueAsString(r);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(200);
            response.getWriter().write(respStr);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}

