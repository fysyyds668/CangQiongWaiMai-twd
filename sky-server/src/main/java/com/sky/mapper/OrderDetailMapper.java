package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单详细信息
     * @param orderDetailList
     */
    public void insertBatch(List<OrderDetail> orderDetailList);
}
