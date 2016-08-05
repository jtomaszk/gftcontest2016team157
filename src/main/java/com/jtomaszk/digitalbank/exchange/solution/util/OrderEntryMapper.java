package com.jtomaszk.digitalbank.exchange.solution.util;

import com.gft.digitalbank.exchange.model.OrderEntry;
import com.jtomaszk.digitalbank.exchange.solution.dto.Order;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Jarema Tomaszkiewicz <jarema.tomaszkiewicz@cgi.com>
 */
public class OrderEntryMapper {
    public static List<OrderEntry> mapToOrderEntryList(Collection<Order> orderList) {
        AtomicInteger i = new AtomicInteger(1);
        return orderList.stream()
                .filter(Order::orderIsNotCompleted)
                .sorted()
                .map(mapToOrderEntry(i))
                .collect(Collectors.toList());
    }

    private static Function<Order, OrderEntry> mapToOrderEntry(AtomicInteger index) {
        return order -> OrderEntry.builder()
                .amount(order.getDetails().getAmount())
                .broker(order.getBroker())
                .client(order.getClient())
                .id(index.getAndIncrement())
                .price(order.getDetails().getPrice())
                .build();
    }
}
