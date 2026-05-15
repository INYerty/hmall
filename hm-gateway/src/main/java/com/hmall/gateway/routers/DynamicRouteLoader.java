package com.hmall.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter routeDefinitionWriter;

    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";
    private final Set<String> routeIds = new HashSet<>();

    @PostConstruct
    public void initRouteConfigListener() throws NacosException {
        log.info("初始化动态路由监听器");
        ConfigService configService = nacosConfigManager.getConfigService();
        //1.项目启动时，先拉取一次配置存到路由表，且添加配置监听器
        String configInfo = configService.getConfigAndSignListener(dataId, group, 5000, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                //2.监听到配置变化时，更新路由表   注意这里是异步的，所以这里需要等待，第一次的路由表信息
                updateRouteConfig(configInfo);
            }
        });
        //3.在这里将配置信息保存到路由表（第一次）
        updateRouteConfig(configInfo);
    }

    //TODO 这里需要考复习
    public void updateRouteConfig(String configInfo) {
        log.info("监听到路由配置信息：{}",configInfo);
        //1. 解析配置文件转成routeDefinition
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        //2.旧的全删了，重新添加新的路由表
        for (String routeId : routeIds) {
            routeDefinitionWriter.delete(Mono.just(routeId));
        }
        //清空旧的routeIds集合，准备存新的更新的路由id
        routeIds.clear();
        //3. 更新路由表
        for (RouteDefinition routeDefinition : routeDefinitions) {
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            //记录路由id，便于下一次更新时删除
            routeIds.add(routeDefinition.getId());
        }

    }
}
