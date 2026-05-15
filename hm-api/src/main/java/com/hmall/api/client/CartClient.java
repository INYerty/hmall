package com.hmall.api.client;

/*TODO: client 接口,作用是在一个微服务中调用其他微服务的时候，发起http请求，调用其他微服务提供的接口
   cartservice会调用item的查询接口因此在itemclient中添加接口queryItemByIds方法*/

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

//这个方法不用实现，将来openfeign会自动实现
@FeignClient("cart-service")
public interface CartClient {
    @DeleteMapping("/carts")
    void removeByItemIds(@RequestParam("ids") Collection<Long> ids);
}
