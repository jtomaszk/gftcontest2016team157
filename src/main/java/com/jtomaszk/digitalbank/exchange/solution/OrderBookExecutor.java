package com.jtomaszk.digitalbank.exchange.solution;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.Transaction;
import com.google.common.collect.Maps;
import com.jtomaszk.digitalbank.exchange.solution.dto.CancellationOrder;
import com.jtomaszk.digitalbank.exchange.solution.dto.ModificationOrder;
import com.jtomaszk.digitalbank.exchange.solution.dto.Order;
import lombok.extern.java.Log;
import pl.wavesoftware.eid.utils.EidPreconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Jarema Tomaszkiewicz
 */
@Log
class OrderBookExecutor {

    private final Map<String, ProductOrderBook> orderBooks = Collections.synchronizedMap(Maps.newHashMap());
    private final Map<Integer, Order> allOrders = Collections.synchronizedMap(Maps.newHashMap());

    public void add(CancellationOrder cancellationOrder) {
        EidPreconditions.checkArgument(allOrders.containsKey(cancellationOrder.getCancelledOrderId()), "20160727:110026");
        log.fine(cancellationOrder::toString);
        Order order = allOrders.get(cancellationOrder.getCancelledOrderId());
        orderBooks.get(order.getProduct()).add(cancellationOrder);
    }

    public void add(ModificationOrder modificationOrder) {
        log.fine(modificationOrder::toString);
        Order order = allOrders.get(modificationOrder.getModifiedOrderId());
        ProductOrderBook orderBook = orderBooks.get(order.getProduct());
        orderBook.add(modificationOrder);
    }

    public void add(Order order) {
        log.fine(order::toString);
        allOrders.put(order.getId(), order);
        String product = order.getProduct();

        if (!orderBooks.containsKey(product)) {
            ProductOrderBook productOrderBook = new ProductOrderBook(product);
            productOrderBook.init();
            orderBooks.put(product, productOrderBook);
        }
        ProductOrderBook orderBook = orderBooks.get(product);
        orderBook.add(order);
    }

    public Collection<OrderBook> getOrderBooks() {
        return orderBooks.values()
          .stream()
          .filter(ProductOrderBook::isActive)
          .map(ProductOrderBook::getActiveOrders)
          .collect(Collectors.toList());
    }

    public Collection<Transaction> getTransactions() {
        return orderBooks.values()
                .stream()
                .flatMap(b -> b.getTransactions().stream())
                .collect(Collectors.toList());
    }

    public void processingDone() {
        orderBooks.values()
          .stream()
          .forEach(ProductOrderBook::processingDone);
    }
}
