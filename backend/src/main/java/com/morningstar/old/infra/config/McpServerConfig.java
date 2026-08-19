package com.morningstar.old.infra.config;

import lombok.RequiredArgsConstructor;
import org.noear.solon.Solon;
import org.noear.solon.ai.chat.prompt.MethodPromptProvider;
import org.noear.solon.ai.chat.resource.MethodResourceProvider;
import org.noear.solon.ai.chat.tool.MethodToolProvider;
import org.noear.solon.ai.mcp.server.IMcpServerEndpoint;
import org.noear.solon.ai.mcp.server.McpServerEndpointProvider;
import org.noear.solon.ai.mcp.server.annotation.McpServerEndpoint;
import org.noear.solon.web.servlet.SolonServletFilter;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * 内嵌 Solon，把实现了 IMcpServerEndpoint 的 Spring 组件注册为 MCP 服务端点。
 *
 * <p>独立一个目录，让 Solon 扫描范围最小化；通过 {@link SolonServletFilter} 把 /mcp/* 路由进 Solon。</p>
 */
@Configuration
@RequiredArgsConstructor
public class McpServerConfig {

    private final List<IMcpServerEndpoint> serverEndpoints;

    @PostConstruct
    public void start() {
        // 启动内嵌 Solon（禁用组件扫描，端点由 springCom2Endpoint 手动收集）
        Solon.start(McpServerConfig.class, new String[]{}, app -> app.enableScanning(false));

        springCom2Endpoint();
    }

    @PreDestroy
    public void stop() {
        if (Solon.app() != null) {
            // 停止 solon（根据配置，可支持两段式安全停止）
            Solon.stopBlock(false, Solon.cfg().stopDelay());
        }
    }

    /**
     * 提取容器里实现 IMcpServerEndpoint 接口的 bean，注册为 MCP 服务端点
     */
    protected void springCom2Endpoint() {
        for (IMcpServerEndpoint serverEndpoint : serverEndpoints) {
            Class<?> serverEndpointClz = AopUtils.getTargetClass(serverEndpoint);
            McpServerEndpoint anno = AnnotationUtils.findAnnotation(serverEndpointClz, McpServerEndpoint.class);

            if (anno == null) {
                continue;
            }

            McpServerEndpointProvider serverEndpointProvider = McpServerEndpointProvider.builder()
                    .from(serverEndpointClz, anno)
                    .build();

            serverEndpointProvider.addTool(new MethodToolProvider(serverEndpointClz, serverEndpoint));
            serverEndpointProvider.addResource(new MethodResourceProvider(serverEndpointClz, serverEndpoint));
            serverEndpointProvider.addPrompt(new MethodPromptProvider(serverEndpointClz, serverEndpoint));

            serverEndpointProvider.postStart();
        }
    }

    @Bean
    public FilterRegistrationBean<SolonServletFilter> mcpServerFilter() {
        // 通过 Servlet Filter 实现 http 能力对接
        FilterRegistrationBean<SolonServletFilter> filter = new FilterRegistrationBean<>();
        filter.setName("SolonFilter");
        filter.addUrlPatterns("/mcp/*");
        filter.setFilter(new SolonServletFilter());
        return filter;
    }
}
