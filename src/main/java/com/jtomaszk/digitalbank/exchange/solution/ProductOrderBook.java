package com.jtomaszk.digitalbank.exchange.solution;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.Transaction;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jtomaszk.digitalbank.exchange.solution.dto.CancellationOrder;
import com.jtomaszk.digitalbank.exchange.solution.dto.ModificationOrder;
import com.jtomaszk.digitalbank.exchange.solution.dto.Order;
import com.jtomaszk.digitalbank.exchange.solution.dto.OrderSide;
import com.jtomaszk.digitalbank.exchange.solution.util.OrderEntryMapper;
import com.jtomaszk.digitalbank.exchange.solution.util.SortedOrdersCollection;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codepoetics.protonpack.StreamUtils.takeWhile;

/**
 * Created by Jarema Tomaszkiewicz
 */
@Log
@RequiredArgsConstructor
class ProductOrderBook {

    public static final double CLEAN_THRESHOLD = 0.9;
    private final AtomicInteger transactionCount = new AtomicInteger(1);
    private final String product;
    private final Collection<Order> ordersBuy = new SortedOrdersCollection();
    private final Collection<Order> ordersSell = new SortedOrdersCollection();
    private final Map<Integer, Order> active = Collections.synchronizedMap(Maps.newHashMap());
    private RingBuffer<OrderEvent> ringBuffer;
    @Getter
    private final Collection<Transaction> transactions = new LinkedBlockingQueue<>();
    private Disruptor<OrderEvent> disruptor;

    public void init() {
        // Executor that will be used to construct new threads for consumers
        ThreadFactory executor = Executors.defaultThreadFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;

        // Construct the Disruptor
        disruptor = new Disruptor<>(OrderEvent::new, bufferSize, executor);

        // Connect the handler
        //noinspection unchecked
        disruptor.handleEventsWith((event, sequence, endOfBatch) -> modifyOrder(event));

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        ringBuffer = disruptor.getRingBuffer();
    }

    public void processingDone() {
        disruptor.shutdown();
        transactions.addAll(executeTransactions());
    }

    public void add(Order order) {
        ringBuffer.publishEvent((event, sequence, arg0) -> event.set(arg0), order);
    }

    public void add(ModificationOrder order) {
        ringBuffer.publishEvent((event, sequence, arg0) -> event.set(arg0), order);
    }

    public void add(CancellationOrder order) {
        ringBuffer.publishEvent((event, sequence, arg0) -> event.set(arg0), order);
    }

    private void modifyOrder(OrderEvent orderEvent) {
        if (orderEvent.isOrder()) {
            setOrderActiveAndAdd(orderEvent.getOrder());
        }
        if (orderEvent.isModificationOrder()) {
            modifyOrder(orderEvent.getModificationOrder());
        }
        if (orderEvent.isCancellationOrder()) {
            cancelOrder(orderEvent.getCancellationOrder());
        }
        transactions.addAll(executeTransactions());
    }

    private List<Transaction> executeTransactions() {
        List<Transaction> ret = executeTransactionsLoop();
        cleanCompletedOrders();
        return ret;
    }

    private void cleanCompletedOrders() {
        if (shouldClean()) {
            cleanCompleted(ordersSell);
            cleanCompleted(ordersBuy);
        }
    }

    private List<Transaction> executeTransactionsLoop() {
        List<Transaction> ret = Lists.newLinkedList();
        List<Transaction> i;
        do {
            i = executeBuyOrders();
            ret.addAll(i);
        } while (i.size() > 0);
        return ret;
    }

    private boolean shouldClean() {
        return active.size() < ((ordersBuy.size() + ordersSell.size()) * CLEAN_THRESHOLD);
    }

    private void cleanCompleted(Collection<Order> orderList) {
        Iterator<Order> it = orderList.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (!active.containsKey(o.getId())) {
                it.remove();
            }
        }
    }

    private List<Transaction> executeBuyOrders() {
        return ordersBuy.stream()
          .filter(Order::orderIsNotCompleted)
          .flatMap(this::executeTransactions)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList());
    }

    private Stream<Optional<Transaction>> executeTransactions(Order buyOrder) {
        return takeWhile(ordersSell.stream(), orderIsNotCompletedWith(buyOrder))
          .filter(buyOrder::canExecuteWith)
          .map(order -> {
              Optional<Transaction> transaction = order.tryExecuteTransactionsWith(buyOrder, transactionCount);
              ifCompletedSetInactive(order);
              ifCompletedSetInactive(buyOrder);
              return transaction;
          });
    }

    private void ifCompletedSetInactive(Order order) {
        if (order.getDetails().getAmount() == 0) {
            setInactive(order.getId());
        }
    }

    private Predicate<Order> orderIsNotCompletedWith(Order buyOrder) {
        return order -> buyOrder.orderIsNotCompleted();
    }

    private void modifyOrder(ModificationOrder modificationOrder) {
        Order modifiedOrder = setInactive(modificationOrder.getModifiedOrderId());

        Optional.ofNullable(modifiedOrder)
          .filter(Order::orderIsNotCompleted)
          .ifPresent(modifyOrderWith(modificationOrder));
    }

    private Consumer<Order> modifyOrderWith(ModificationOrder modificationOrder) {
        return o -> {
            setInactiveAndRemove(o);
            Order newOrder = o.createModifiedOrder(modificationOrder);
            setOrderActiveAndAdd(newOrder);
        };
    }

    private void setOrderActiveAndAdd(Order order) {
        active.put(order.getId(), order);
        if (order.getSide() == OrderSide.BUY) {
            ordersBuy.add(order);
        } else {
            ordersSell.add(order);
        }
    }

    private void setInactiveAndRemove(Order order) {
        order.getDetails().setAmount(0);
        setInactive(order.getId());

        if (order.getSide() == OrderSide.BUY) {
            ordersBuy.remove(order);
        } else {
            ordersSell.remove(order);
        }
    }

    public void cancelOrder(CancellationOrder order) {
        Integer orderId = order.getCancelledOrderId();
        Optional.ofNullable(setInactive(orderId))
          .ifPresent(this::setInactiveAndRemove);
    }

    private Order setInactive(Integer orderId) {
        return active.remove(orderId);
    }

    public OrderBook getActiveOrders() {
        return OrderBook.builder()
          .product(product)
          .buyEntries(OrderEntryMapper.mapToOrderEntryList(ordersBuy))
          .sellEntries(OrderEntryMapper.mapToOrderEntryList(ordersSell))
          .build();
    }

    public boolean isActive() {
        return active.size() > 0;
    }

}
