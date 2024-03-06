package com.xuecheng.orders.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.service.OrdersGoodsService;
import org.springframework.stereotype.Service;

@Service
public class OrdersGoodsServiceImpl extends ServiceImpl<XcOrdersGoodsMapper, XcOrdersGoods> implements OrdersGoodsService {
}
