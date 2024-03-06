package com.xuecheng.orders.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.orders.model.dto.AddOrderDTO;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.vo.PayRecordVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface OrderService extends IService<XcOrders> {

    PayRecordVO createOrder(AddOrderDTO addOrderDTO);

    void requestPay(String payNo, HttpServletResponse httpResponse);

    PayRecordVO queryPayResult(String payNo);

    void update2OrderAndPayRecord(String payNo,String outPayNo,String tradeStatus);

    void receiveNotify(HttpServletRequest request, HttpServletResponse response);
}
