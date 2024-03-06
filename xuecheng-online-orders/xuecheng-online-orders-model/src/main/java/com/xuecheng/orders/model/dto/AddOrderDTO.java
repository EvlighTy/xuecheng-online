package com.xuecheng.orders.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author Mr.M
 * @version 1.0
 * @description 创建商品订单
 * @date 2022/10/4 10:21
 */
@Data
@ToString
public class AddOrderDTO {
    private Float totalPrice;
    private String orderType;
    private String orderName;
    private String orderDescrip;
    private String orderDetail;
    private String outBusinessId;
}
