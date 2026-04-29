package com.kairos.application.order;

import java.util.List;

public record CreateOrderCommand(
        Long userId,
        List<OrderItemCommand> items,
        String recipient,
        String phone,
        String address,
        String addressDetail,
        String memo
) {}