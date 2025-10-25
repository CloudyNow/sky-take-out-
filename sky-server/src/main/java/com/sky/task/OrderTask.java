package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
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
     * 订单超时订单
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟触发一次
    public void peocessTimeoutOrder(){
        log.info("定时处理超市时间：{}", LocalDateTime.now());

        //获取当前时间,当前时间-15分钟
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        //select * from orders where status = 1 and order_time < (当前时间-15分钟)
        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        if (orderList != null && orderList.size() > 0){
            for (Orders orders : orderList) {
               if(orders != null) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
               }
            }
        }
    }

    /**
     * 处理一直一直派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")//
    public void processDeliveryOrder(){
        log.info("定时处理派送中的订单：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if (orderList != null && orderList.size() > 0){
            for (Orders orders : orderList) {
                orders.setCancelReason("订单超时，自动取消");
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
