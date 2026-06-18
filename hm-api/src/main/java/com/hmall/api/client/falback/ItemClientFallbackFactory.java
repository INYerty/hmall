package com.hmall.api.client.falback;

import cn.hutool.core.collection.CollUtil;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

/*TODO：fallbackFactory 是一个工厂类，作用是当调用itemclient接口失败时，
    返回一个itemclient的实现类，这个实现类中所有的方法都要有一个默认的实现*/

@Slf4j
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        log.error("item-service服务调用失败", cause);
        return new ItemClient() {
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("查询商品失败", cause);
                return CollUtils.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.info("扣减库存失败", cause);
                throw new RuntimeException("扣减库存失败");
            }

            @Override
            public void restoreStock(List<OrderDetailDTO> items) {
                log.info("恢复库存失败", cause);
                throw new RuntimeException("恢复库存失败");
            }
        };
    }
}
