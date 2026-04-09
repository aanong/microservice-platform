package com.demo.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.order.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
}
