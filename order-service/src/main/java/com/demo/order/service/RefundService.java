package com.demo.order.service;

import com.demo.order.dto.RefundRequest;
import com.demo.order.entity.RefundMain;

public interface RefundService {

    RefundMain refund(RefundRequest request);
}
