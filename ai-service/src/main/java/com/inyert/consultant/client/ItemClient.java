package com.inyert.consultant.client;

import com.hmall.api.dto.ItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
//TODO  写死了8081
/**
 * 商品服务调用客户端
 */
@FeignClient("item-service")
public interface ItemClient {

    /**
     * 根据ID集合查询商品列表
     */
    @GetMapping("/items")
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);
}

