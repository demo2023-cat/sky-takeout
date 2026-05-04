package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理异常（地址为空，购物车为空）
        AddressBook byId = addressBookService.getById(ordersSubmitDTO.getAddressBookId());
        if (byId == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(BaseContext.getCurrentId());
        if (shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表加一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(byId.getPhone());
        orders.setAddress(byId.getDetail());
        orders.setConsignee(byId.getConsignee());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orderMapper.insert(orders);
        //订单明细表插入n条数据
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(cart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).toList();
        orderDetailMapper.insertBatch(orderDetailList);

        //清理购物车
        shoppingCartService.clean(orders.getUserId());
        //返回VO
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 1. 获取订单号
        String orderNumber = ordersPaymentDTO.getOrderNumber();

        // 2. 【核心修改】直接调用支付成功逻辑，修改数据库状态
        // 这一步执行后，订单在数据库里的状态就会从“待付款”变成“待接单”
        log.info("模拟支付成功，修改订单状态，订单号：{}", orderNumber);
        paySuccess(orderNumber);

        // 3. 构造一个空的 VO 返回给前端
        // 既然不走微信支付了，jsonObject 也就没用了，直接新建一个空的 VO 即可
        return new OrderPaymentVO();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查询订单：{}", ordersPageQueryDTO);
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public int cancel(OrdersCancelDTO ordersCancelDTO) {
         log.info("取消订单，参数：{}", ordersCancelDTO);
         Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());
         if(ordersDB == null || ordersDB.getStatus() > Orders.DELIVERY_IN_PROGRESS){
             throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
         }
         if(ordersDB.getStatus().equals(Orders.PAID)){
//             String refund = weChatPayUtil.refund(
//                     ordersDB.getNumber(),
//                     ordersDB.getNumber(),
//                     new BigDecimal(0.01),
//                     new BigDecimal(0.01));
             log.info("申请退款");
         }
         Orders orders = Orders.builder()
                 .id(ordersCancelDTO.getId())
                 .cancelReason(ordersCancelDTO.getCancelReason())
                 .status(Orders.CANCELLED)
                 .cancelTime(LocalDateTime.now())
                 .build();
         return orderMapper.update(orders);

    }

    @Override
    public int reject(OrdersRejectionDTO ordersRejectionDTO){
        log.info("拒绝订单：{}", ordersRejectionDTO);
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());
        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
           throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if(ordersDB.getStatus().equals(Orders.PAID)){
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
            log.info("申请退款");
        }
        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        return orderMapper.update(orders);
    }

    @Override
    public PageResult historyPageQuery(OrdersPageQueryDTO ordersPageQuery) {
        ordersPageQuery.setUserId(BaseContext.getCurrentId());
        log.info("查询历史订单：{}", ordersPageQuery);
        PageHelper.startPage(ordersPageQuery.getPage(), ordersPageQuery.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQuery);
        List<OrderVO> orderVOList = new ArrayList<>();
        
        if (page != null && !page.isEmpty()) {
            // 1. 提取当前页所有订单的 ID 列表
            List<Long> orderIds = page.getResult().stream().map(Orders::getId).collect(Collectors.toList());
            
            // 2. 批量查询这些订单的明细 (只发1条SQL，解决 N+1 查询问题)
            List<OrderDetail> allOrderDetails = orderDetailMapper.getOrderDetailsByOrderIds(orderIds);
            
            // 3. 将订单明细按 orderId 分组存入 Map，方便后续匹配
            Map<Long, List<OrderDetail>> orderDetailMap = allOrderDetails.stream()
                    .collect(Collectors.groupingBy(OrderDetail::getOrderId));
            
            // 4. 遍历组装结果
            for (Orders order : page) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                
                // 从 Map 中获取对应的明细，找不到则给空列表
                List<OrderDetail> orderDetailList = orderDetailMap.getOrDefault(order.getId(), new ArrayList<>());
                orderVO.setOrderDetailList(orderDetailList);
                
                String orderDishes = orderDetailList.stream().map(orderDetail -> orderDetail.getName() + "*" + orderDetail.getNumber())
                        .collect(Collectors.joining("、"));
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        
        long total = page != null ? page.getTotal() : 0L;
        return new PageResult(total, orderVOList);
    }

}
