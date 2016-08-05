package com.jtomaszk.digitalbank.exchange.solution.util;

import com.jtomaszk.digitalbank.exchange.solution.dto.Order;
import com.jtomaszk.digitalbank.exchange.solution.dto.OrderDetails;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jarema Tomaszkiewicz <jarema.tomaszkiewicz@cgi.com>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionUtil {

    public static Integer getTransactionPrice(Order sellOrder, Order buyOrder) {
        OrderDetails sell = sellOrder.getDetails();
        OrderDetails buy = buyOrder.getDetails();

        Integer price;
        if (buyOrder.getTimestamp() > sellOrder.getTimestamp()) {
            price = sell.getPrice();
        } else {
            price = buy.getPrice();
        }
        return price;
    }

    public static Integer getTransactionAmount(OrderDetails sell, OrderDetails buy) {
        Integer transactionAmount;
        if (buy.getAmount() >= sell.getAmount()) {
            transactionAmount = sell.getAmount();
        } else {
            transactionAmount = buy.getAmount();
        }
        return transactionAmount;
    }
}
