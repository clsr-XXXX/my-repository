package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderPaymentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 用户提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 获取当前用户id
        Long userId = BaseContext.getCurrentId();

        // 查询当前用户的购物车数据
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new RuntimeException("购物车为空，不能下单");
        }

        // 查询地址
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new RuntimeException("地址信息有误，不能下单");
        }

        // 订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ShoppingCart cart : shoppingCartList) {
            totalAmount = totalAmount.add(cart.getAmount().multiply(BigDecimal.valueOf(cart.getNumber())));
        }
        totalAmount = totalAmount.add(BigDecimal.valueOf(ordersSubmitDTO.getPackAmount()));

        // 生成订单号
        String orderNumber = "ORDER" + System.currentTimeMillis();

        // 构建订单数据
        Orders orders = Orders.builder()
                .number(orderNumber)
                .status(Orders.PENDING_PAYMENT)
                .userId(userId)
                .addressBookId(ordersSubmitDTO.getAddressBookId())
                .orderTime(LocalDateTime.now())
                .payMethod(ordersSubmitDTO.getPayMethod())
                .payStatus(Orders.UN_PAID)
                .amount(totalAmount)
                .remark(ordersSubmitDTO.getRemark())
                .phone(addressBook.getPhone())
                .address(addressBook.getDetail())
                .consignee(addressBook.getConsignee())
                .estimatedDeliveryTime(ordersSubmitDTO.getEstimatedDeliveryTime())
                .deliveryStatus(ordersSubmitDTO.getDeliveryStatus())
                .packAmount(ordersSubmitDTO.getPackAmount())
                .tablewareNumber(ordersSubmitDTO.getTablewareNumber())
                .tablewareStatus(ordersSubmitDTO.getTablewareStatus())
                .build();

        // 向订单表插入1条数据
        orderMapper.insert(orders);

        // 订单明细数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .orderId(orders.getId())
                    .dishId(cart.getDishId())
                    .setmealId(cart.getSetmealId())
                    .name(cart.getName())
                    .image(cart.getImage())
                    .dishFlavor(cart.getDishFlavor())
                    .number(cart.getNumber())
                    .amount(cart.getAmount())
                    .build();
            orderDetailList.add(orderDetail);
        }

        // 向订单明细表插入n条数据
        orderDetailMapper.insertBatch(orderDetailList);

        // 清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        // 封装返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        // 根据订单号查询当前用户的订单
        Orders orders = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());

        // 订单不存在
        if (orders == null) {
            throw new RuntimeException("订单不存在");
        }

//        // 允许待付款、待接单、已接单等状态都可以模拟支付（仅用于联调环境）
//        // 只要不是已取消、已完成、退款等终态即可
        Integer status = orders.getStatus();
//        if (status != null && (status.equals(Orders.CANCELLED) || status.equals(Orders.COMPLETED) || status.equals(Orders.REFUND))) {
//            throw new RuntimeException("订单状态不正确");
//        }

        // 只禁止已完成、已取消这两种终态
        if (status != null &&
                (status.equals(Orders.COMPLETED) ||
                        status.equals(Orders.CANCELLED))) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);  // 用项目常量，更规范
        }
//
//        // 模拟支付成功，更新订单状态
//        Orders orders1 = Orders.builder()
//                .id(orders.getId())
//                .payStatus(Orders.PAID)
//                .checkoutTime(LocalDateTime.now())
//                .build();
//        orderMapper.update(orders1);
//
//        // 返回模拟的支付结果
//        return OrderPaymentVO.builder()
//                .nonceStr("模拟随机字符串")
//                .paySign("模拟签名")
//                .timeStamp(String.valueOf(System.currentTimeMillis()))
//                .signType("RSA")
//                .packageStr("prepay_id=模拟prepay_id")
//                .build();


        // 模拟支付成功，更新订单状态
        Orders updateOrders = Orders.builder()
                .id(orders.getId())
                .status(Orders.TO_BE_CONFIRMED)  // 待接单
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(updateOrders);

        // ============== 关键：构造更真实的模拟支付参数 ==============
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);  // 秒级时间戳
        String nonceStr = "simulated_nonce_" + System.currentTimeMillis();     // 更像真实的随机串
        String packageStr = "prepay_id=wx" + System.currentTimeMillis() + "prepayid_simulate";

        return OrderPaymentVO.builder()
                .timeStamp(timeStamp)
                .nonceStr(nonceStr)
                .signType("RSA")                    // 小程序目前常用 RSA
                .packageStr(packageStr)             // 注意字段名是 package（前端会用）
                .paySign("simulated_pay_sign_1234567890abcdef")  // 随便一个长一点的字符串
                .build();
    }
}
