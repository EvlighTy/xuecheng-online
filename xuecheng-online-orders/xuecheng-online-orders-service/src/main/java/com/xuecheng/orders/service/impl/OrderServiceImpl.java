package com.xuecheng.orders.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.enumeration.MessageType;
import com.xuecheng.base.enumeration.OrderStatus;
import com.xuecheng.base.enumeration.PayChannel;
import com.xuecheng.base.enumeration.PayStatus;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.base.exmsg.AuthExMsg;
import com.xuecheng.base.exmsg.CommonExMsg;
import com.xuecheng.base.exmsg.OrderExMsg;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDTO;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.model.vo.PayRecordVO;
import com.xuecheng.orders.model.vo.PayStatusVO;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.service.OrdersGoodsService;
import com.xuecheng.orders.utils.AlipayUtil;
import com.xuecheng.orders.utils.RabbitMQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<XcOrdersMapper, XcOrders> implements OrderService {

    @Autowired
    private AlipayUtil alipayUtil;

    @Autowired
    private XcOrdersMapper xcOrdersMapper;

    @Autowired
    private XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Autowired
    private OrdersGoodsService ordersGoodsService;

    @Autowired
    private XcPayRecordMapper xcPayRecordMapper;

    @Autowired @Lazy
    private OrderService orderService;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private RabbitMQUtil rabbitMQUtil;

    @Value("${alipay.qr_code_url}")
    private String QRCodeURL;

    //用户请求支付二维码
    @Transactional
    @Override
    public PayRecordVO createOrder(String userId, AddOrderDTO addOrderDTO) {
        //保存订单信息
        XcOrders xcOrders = save2Order(userId, addOrderDTO);
        //保存支付信息
        XcPayRecord xcPayRecord = save2PayRecord(xcOrders);
        //生成支付二维码
        String qrCode;
        try {
            qrCode = QRCodeUtil.createQRCode(String.format(QRCodeURL, xcPayRecord.getPayNo()), 200, 200);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("生成二维码出错");
        }
        PayRecordVO payRecordVO = BeanUtil.copyProperties(xcPayRecord, PayRecordVO.class);
        payRecordVO.setQrcode(qrCode);
        return payRecordVO;
    }

    //用户请求支付
    @Override
    public void requestPay(String payNo, HttpServletResponse httpResponse) {
        /*业务逻辑校验(如果payNo不存在则提示重新发起支付)*/
        LambdaQueryWrapper<XcPayRecord> queryWrapper = new LambdaQueryWrapper<XcPayRecord>()
                .eq(XcPayRecord::getPayNo, payNo);
        XcPayRecord xcPayRecord = xcPayRecordMapper.selectOne(queryWrapper);
        if(xcPayRecord==null) throw new CustomException(OrderExMsg.QR_CODE_INVALID);
        /*业务逻辑校验(订单未支付)*/
        if(xcPayRecord.getStatus().equals(PayStatus.PAID.getValue())) throw new CustomException(OrderExMsg.ORDER_PAID);
        //调用支付宝接口发起支付
        alipayUtil.requestPay(xcPayRecord,httpResponse);
    }

    //查询支付结果
    @Override
    public PayRecordVO queryPayResult(String payNo) {
        //调用支付宝接口查询支付结果
        AlipayTradeQueryResponse response = alipayUtil.queryPayResult(payNo);
        //保存支付结果
        PayStatusVO payStatusVO = new PayStatusVO();
        payStatusVO.setOut_trade_no(payNo);
        payStatusVO.setTrade_status(response.getTradeStatus());
        payStatusVO.setApp_id(null);
        payStatusVO.setTrade_no(response.getTradeNo());
        payStatusVO.setTotal_amount(response.getTotalAmount());
        //更新订单表和支付记录表
        orderService.update2OrderAndPayRecord(payNo,payStatusVO.getTrade_no(),payStatusVO.getTrade_status());
        //返回已更新的信息
        LambdaQueryWrapper<XcPayRecord> queryWrapper = new LambdaQueryWrapper<XcPayRecord>()
                .eq(XcPayRecord::getPayNo, payNo);
        XcPayRecord xcPayRecord = xcPayRecordMapper.selectOne(queryWrapper);
        return BeanUtil.copyProperties(xcPayRecord, PayRecordVO.class);
    }

    //更新订单和支付信息
    @Transactional
    @Override
    public void update2OrderAndPayRecord(String payNo,String outPayNo,String tradeStatus) {
        /*业务逻辑校验(支付记录存在)*/
        LambdaQueryWrapper<XcPayRecord> queryWrapper = new LambdaQueryWrapper<XcPayRecord>()
                .eq(XcPayRecord::getPayNo, payNo);
        XcPayRecord xcPayRecord = xcPayRecordMapper.selectOne(queryWrapper);
        if(xcPayRecord==null) throw new CustomException(OrderExMsg.ORDER_NOT_EXIST);
        /*业务逻辑校验(关联订单存在)*/
        XcOrders xcOrders = xcOrdersMapper.selectById(xcPayRecord.getOrderId());
        if(xcOrders==null) throw new CustomException(OrderExMsg.ORDER_NOT_EXIST);
        /*业务逻辑校验(支付记录的状态为成功则不做处理)*/
        if(xcPayRecord.getStatus().equals(PayStatus.PAID.getValue())) return;
        //更新支付记录表
        LambdaUpdateWrapper<XcPayRecord> updateWrapper = new LambdaUpdateWrapper<XcPayRecord>()
                .eq(XcPayRecord::getId, xcPayRecord.getId())
                .set(XcPayRecord::getOutPayChannel, PayChannel.ALIPAY.getValue()) //支付类型
                .set(XcPayRecord::getOutPayNo,outPayNo); //支付宝订单号
        //更新订单表
        LambdaUpdateWrapper<XcOrders> updateWrapper1 = new LambdaUpdateWrapper<XcOrders>()
                .eq(XcOrders::getId, xcPayRecord.getOrderId());
        if(tradeStatus.equals("TRADE_SUCCESS")){
            //交易成功
            updateWrapper.set(XcPayRecord::getStatus,PayStatus.PAID.getValue())
                    .set(XcPayRecord::getPaySuccessTime, LocalDateTime.now());
            updateWrapper1.set(XcOrders::getStatus, OrderStatus.PAID.getValue());
        }else {
            //交易失败
        }
        int update = xcPayRecordMapper.update(null, updateWrapper);
        if(update!=1) throw new CustomException(CommonExMsg.UPDATE_FAILED);
        int update1 = xcOrdersMapper.update(null, updateWrapper1);
        if(update1!=1) throw new CustomException(CommonExMsg.UPDATE_FAILED);
        //保存消息到消息表
        MqMessage mqMessage = mqMessageService.addMessage(MessageType.PAY_RESULT_NOTIFY.getValue(), xcOrders.getOutBusinessId(), null, null);
        //保存消息到消息队列
        rabbitMQUtil.sendMessage(mqMessage,mqMessage.getId());
    }

    //接收支付结果通知
    @Override
    public void receiveNotify(HttpServletRequest request, HttpServletResponse response) {
        Boolean signVerified = alipayUtil.rsaCheckV1(request);
        try {
            if (signVerified) {
                //更新订单状态
                String payNo = new String(request.getParameter("out_trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8); //商户订单号
                String outPayNo = new String(request.getParameter("trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8); //支付宝交易号
                String tradeStatus = new String(request.getParameter("trade_status").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8); //交易状态
                orderService.update2OrderAndPayRecord(payNo,outPayNo,tradeStatus);
                //告诉支付宝已经收到通知并处理成功
                response.getWriter().write("success");
            } else {
                //告诉支付宝验签失败
                response.getWriter().write("failure");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //保存订单信息(订单表+订单详细表)
    private XcOrders save2Order(String userId, AddOrderDTO addOrderDTO) {
        /*业务逻辑校验(合法用户)*/
        if (userId==null) throw new CustomException(AuthExMsg.LOGIN_FIRST);
        /*业务逻辑校验(一个选课记录只能创建一个订单)*/
        LambdaQueryWrapper<XcOrders> queryWrapper = new LambdaQueryWrapper<XcOrders>()
                .eq(XcOrders::getOutBusinessId, addOrderDTO.getOutBusinessId());
        XcOrders xcOrders = getOne(queryWrapper);
        if(xcOrders!=null) return xcOrders;
        //创建新订单(订单基本信息表，订单详细表)
        /*订单基本信息表*/
        xcOrders = BeanUtil.copyProperties(addOrderDTO, XcOrders.class);
        long orderId = IdWorkerUtils.getInstance().nextId();
        xcOrders.setId(orderId); //订单id(雪花算法)
        xcOrders.setUserId(userId); //用户id
        xcOrders.setStatus(OrderStatus.UNPAID.getValue()); //订单状态(初始为未支付)
        boolean save = save(xcOrders);
        if(!save) throw new CustomException(CommonExMsg.INSERT_FAILED);
        /*订单详细表*/
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(addOrderDTO.getOrderDetail(), XcOrdersGoods.class);
        xcOrdersGoods.forEach(g->g.setOrderId(orderId));
        boolean saved = ordersGoodsService.saveBatch(xcOrdersGoods);
        if(!saved) throw new CustomException(CommonExMsg.INSERT_FAILED);
        return xcOrders;
    }

    //保存支付信息
    private XcPayRecord save2PayRecord(XcOrders xcOrders) {
        /*业务逻辑校验，订单已存在且状态为未支付则返回原订单*/ //todo(如果二维码已失效)
        LambdaQueryWrapper<XcPayRecord> queryWrapper = new LambdaQueryWrapper<XcPayRecord>()
                .eq(XcPayRecord::getOrderId, xcOrders.getId());
        XcPayRecord xcPayRecord = xcPayRecordMapper.selectOne(queryWrapper);
        if(xcPayRecord!=null && xcPayRecord.getStatus().equals(PayStatus.UNPAID.getValue())) return xcPayRecord;
        //创建新支付信息
        xcPayRecord = BeanUtil.copyProperties(xcOrders, XcPayRecord.class);
        xcPayRecord.setId(null);
        xcPayRecord.setOrderId(xcOrders.getId()); //订单id
        long payNo = IdWorkerUtils.getInstance().nextId();
        xcPayRecord.setPayNo(payNo); //支付单号(雪花算法)
        xcPayRecord.setStatus(PayStatus.UNPAID.getValue()); //支付状态
        xcPayRecord.setCurrency("CNY"); //币种
        int insert = xcPayRecordMapper.insert(xcPayRecord);
        if(insert!=1) throw new CustomException(CommonExMsg.INSERT_FAILED);
        return xcPayRecord;
    }

}
