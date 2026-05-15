package com.hmall.gateway.filters;

import org.springframework.util.AntPathMatcher;
import cn.hutool.jwt.JWTUtil;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.security.sasl.AuthenticationException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    //注入配置类
    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public int getOrder() {
        return 0;
    }
    @Override
    //exchange：封装了请求和响应对象，
    // chain：过滤器链，调用chain.filter(exchange)将请求传递给下一个过滤器
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1、获取request
        ServerHttpRequest request = exchange.getRequest();

        //判断是否需要登录拦截
        if(isExclude(request.getPath().toString())){
            return chain.filter(exchange);
        }

        //2、获取token
        List<String> headers = request.getHeaders().get("authorization");
        String token = null;
        if(headers != null && !headers.isEmpty()){
            token = headers.get(0);
        }

        //3、校验并解析token
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            //拦截，设置状态码401
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //拦截，返回,在此终止请求，不会向后传递
            return response.setComplete();
        }

        //4、传递用户信息
        System.out.println("userId :" + userId);
        String info = userId.toString();
        ServerWebExchange swe = exchange.mutate()
                //在请求头中添加用户信息，传递给后续服务
                .request(builder -> builder.header("user-info", info))
                .build();

        //5、放行
        return chain.filter(swe);
    }

    private boolean isExclude(String path) {
        for (String excludePath : authProperties.getExcludePaths()) {
            if(antPathMatcher.match(excludePath, path)){
                return true;
            }
        }
        return false;
    }
}
