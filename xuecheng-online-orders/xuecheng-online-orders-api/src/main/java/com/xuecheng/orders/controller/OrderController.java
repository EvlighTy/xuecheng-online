package com.xuecheng.orders.controller;

import com.xuecheng.orders.model.dto.AddOrderDTO;
import com.xuecheng.orders.model.vo.PayRecordVO;
import com.xuecheng.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/generatepaycode")
    public PayRecordVO createOrder(@RequestBody AddOrderDTO addOrderDTO) {
        log.info("用户请求支付二维码");
        return orderService.createOrder(addOrderDTO);
    }

    @GetMapping("/requestpay")
    public void requestPay(String payNo, HttpServletResponse httpResponse) {
        log.info("用户请求支付");
        orderService.requestPay(payNo,httpResponse);
    }

    @GetMapping("/payresult")
    public PayRecordVO queryPayResult(String payNo) {
        log.info("查询支付结果");
        return orderService.queryPayResult(payNo);
    }

    @PostMapping("/receiveNotify")
    public void receiveNotify(HttpServletRequest request, HttpServletResponse response){
        log.info("接收支付结果通知");
        orderService.receiveNotify(request,response);
    }

}
