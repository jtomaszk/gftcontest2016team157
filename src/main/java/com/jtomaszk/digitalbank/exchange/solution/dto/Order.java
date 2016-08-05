package com.jtomaszk.digitalbank.exchange.solution.dto;

import com.gft.digitalbank.exchange.model.Transaction;
import com.google.common.base.Preconditions;
import lombok.*;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jtomaszk.digitalbank.exchange.solution.util.TransactionUtil.getTransactionAmount;
import static com.jtomaszk.digitalbank.exchange.solution.util.TransactionUtil.getTransactionPrice;

/**
 * @author Jarema Tomaszkiewicz.
 */
@Getter
@Setter
@ToString
@Builder
@Log
@EqualsAndHashCode(of = "id")
public class Order implements AbstractOrder, Serializable, Comparable<Order> {
    protected final MessageType messageType;
    protected final Integer id;
    protected final Long timestamp;
    protected final String broker;

    private final OrderSide side;
    private final String client;
    private final String product;

    private final OrderDetails details;

    @Override
    public int compareTo(Order o) {
        Preconditions.checkArgument(side == o.getSide(), "Cannot compare different sides");
        Preconditions.checkNotNull(details);
        Preconditions.checkNotNull(o.getDetails());

        if (side == OrderSide.BUY) {
            int priceCompare = o.getDetails().compareTo(this.getDetails());
            return priceCompare == 0 ? this.getTimestamp().compareTo(o.getTimestamp()) : priceCompare;
        } else {
            int priceCompare = this.getDetails().compareTo(o.getDetails());
            return priceCompare == 0 ? this.getTimestamp().compareTo(o.getTimestamp()) : priceCompare;
        }
    }

    public Order createModifiedOrder(ModificationOrder modificationOrder) {
        return Order.builder()
                .product(this.getProduct())
                .client(this.getClient())
                .details(modificationOrder.getDetails())
                .side(this.getSide())
                .messageType(this.getMessageType())
                .broker(this.getBroker())
                .id(this.getId())
                .timestamp(modificationOrder.getTimestamp())
                .build();
    }

    public boolean orderIsNotCompleted() {
        return this.getDetails().getAmount() > 0;
    }

    public boolean isPriceLowerEqual(Order otherOrder) {
        return this.getDetails().compareTo(otherOrder.getDetails()) <= 0;
    }

    public boolean isPriceHigherEqual(Order otherOrder) {
        return this.getDetails().compareTo(otherOrder.getDetails()) >= 0;
    }

    public boolean canExecuteWith(Order otherOrder) {
        return this.orderIsNotCompleted() && otherOrder.orderIsNotCompleted() && this.isPriceHigherEqual(otherOrder);
    }

    public synchronized Optional<Transaction> tryExecuteTransactionsWith(Order buyOrder, AtomicInteger transactionCount) {
        Integer transactionAmount = getTransactionAmount(this.getDetails(), buyOrder.getDetails());
        Integer price = getTransactionPrice(this, buyOrder);

        if (buyOrder.decreaseOrderAmount(transactionAmount)) {
            this.decreaseOrderAmount(transactionAmount);
            return Optional.of(buildTransaction(buyOrder, transactionAmount, price, transactionCount));
        } else {
            return Optional.empty();
        }
    }

    private Transaction buildTransaction(Order buyOrder, Integer transactionAmount, Integer price, AtomicInteger transactionCount) {

        Transaction transaction = Transaction.builder()
                .id(transactionCount.getAndIncrement())
                .amount(transactionAmount)
                .clientBuy(buyOrder.getClient())
                .brokerBuy(buyOrder.getBroker())
                .clientSell(this.getClient())
                .brokerSell(this.getBroker())
                .price(price)
                .product(this.getProduct())
                .build();

        log.fine(() -> "New transaction" + transaction.toString());
        return transaction;
    }

    private synchronized boolean decreaseOrderAmount(Integer transactionAmount) {
        OrderDetails details = this.getDetails();

        int newAmount = details.getAmount() - transactionAmount;
        if (newAmount >= 0) {
            details.setAmount(newAmount);
            return true;
        } else {
            return false;
        }
    }
}
