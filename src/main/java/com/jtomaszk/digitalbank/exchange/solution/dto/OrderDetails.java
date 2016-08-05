package com.jtomaszk.digitalbank.exchange.solution.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author Jarema Tomaszkiewicz.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString(of = {"price", "amount"})
@Builder
public class OrderDetails implements Serializable, Comparable<OrderDetails> {
    private Integer amount;
    private final Integer price;
//    private Integer currentAmount;

//    public Integer getCurrentAmount() {
//        EidPreconditions.checkNotNull(amount, "20160805:103416", "lazy init");
//
//        if (currentAmount == null) {
//            currentAmount = amount;
//        }
//        return currentAmount;
//    }

    @Override
    public int compareTo(OrderDetails o) {
        return price.compareTo(o.getPrice());
    }
}
