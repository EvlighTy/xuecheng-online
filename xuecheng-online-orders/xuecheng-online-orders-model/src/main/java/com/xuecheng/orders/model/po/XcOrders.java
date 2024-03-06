package com.xuecheng.orders.model.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author itcast
 */
@Data
@ToString
@TableName("xc_orders")
public class XcOrders implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Float totalPrice;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;
    private String status;
    private String userId;
    private String orderType;
    private String orderName;
    private String orderDescrip;
    private String orderDetail;
    private String outBusinessId;
}
