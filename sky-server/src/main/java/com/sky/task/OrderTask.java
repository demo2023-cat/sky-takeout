package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    /**
     * 定时处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder(){
        log.info("处理超时订单");
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if(!ordersList.isEmpty()){
            ordersList.forEach(orders -> {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("超时未支付，订单取消");
                orders.setCancelTime(LocalDateTime.now());
            });
            orderMapper.updateBatch(ordersList);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processTimeOutDeliveryOrder(){
        log.info("处理派送未确认完成订单");
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        if(!ordersList.isEmpty()){
            ordersList.forEach(orders -> {
                orders.setStatus(Orders.COMPLETED);
                orders.setDeliveryTime(LocalDateTime.now());
            });
            orderMapper.updateBatch(ordersList);
        }
    }
}
