package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController("user.OrderController")
@RequestMapping("/user/order")
@Api(tags = "C端订单接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private WebSocketServer webSocketServer;
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitD) {
        log.info("用户下单：{}", ordersSubmitD);
        return Result.success(orderService.submitOrder(ordersSubmitD));
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("查看历史订单")
    public Result<PageResult> pageQuery(OrdersPageQueryDTO ordersPageQuery){
        log.info("查看历史订单：{}", ordersPageQuery);
        return Result.success(orderService.historyPageQuery(ordersPageQuery));
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查看订单详情")
    public Result getOrderDetail(@PathVariable Long id){
        log.info("查看订单详情：{}", id);
        return Result.success(orderService.getOrderDetailByOrderId(id));
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id){
        log.info("取消订单：{}", id);
        return Result.success(orderService.cancelByUser(id));
    }

    @PostMapping("repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id){
        log.info("再来一单id：{}", id);
        orderService.repetition(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("提醒订单")
    public Result reminder(@PathVariable Long id) {
        log.info("提醒订单：{}", id);
        orderService.reminder(id);
        return Result.success();
    }
}





