package com.xuecheng.orders.utils;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.xuecheng.base.exmsg.OrderExMsg;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.orders.model.po.XcPayRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class AlipayUtil {

    @Autowired
    private AlipayClient alipayClient;

    @Value("${alipay.public_key}")
    public String PUBLIC_KEY; //公钥

    public String notifyURL = "http://5e371f26.r28.cpolar.top/orders/receiveNotify"; //服务器异步通知页面路径

    public String returnURL = "http://商户网关地址/alipay.trade.wap.pay-JAVA-UTF-8/return_url.jsp"; //页面跳转步通知页面路径

    //发起支付
    public void requestPay(XcPayRecord xcPayRecord, HttpServletResponse httpResponse){
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest(); //创建API对应的request
        alipayRequest.setNotifyUrl(notifyURL);
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\""+xcPayRecord.getPayNo()+"\"," +
                " \"total_amount\":\""+xcPayRecord.getTotalPrice()+"\"," +
                " \"subject\":\""+xcPayRecord.getOrderName()+"\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }"); //填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();//请求支付宝下单接口,发起http请求,调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + AlipayConstants.CHARSET_UTF8);
        try {
            httpResponse.getWriter().write(form); //直接将完整的表单html输出到页面
            httpResponse.getWriter().flush();
            httpResponse.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //查询支付结果
    public AlipayTradeQueryResponse queryPayResult(String payNo){
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setNotifyUrl(notifyURL);
//        request.setReturnUrl(returnURL);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no", payNo);
        request.setBizContent(jsonObject.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if (!response.isSuccess()) throw new CustomException(OrderExMsg.QUERY_PAY_RESULT_FAILED);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new CustomException(OrderExMsg.REQUEST_QUERY_FAILED);
        }
        return response;
    }

    //验签
    public Boolean rsaCheckV1(HttpServletRequest request) {
        boolean flag;
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
        }
        try {
            //调用SDK验证签名
            flag = AlipaySignature.rsaCheckV1(params, PUBLIC_KEY, AlipayConstants.CHARSET_UTF8, AlipayConstants.SIGN_TYPE_RSA2);
        } catch (AlipayApiException e) {
            throw new CustomException(OrderExMsg.VERIFY_FAILED);
        }
        return flag;
    }

}
