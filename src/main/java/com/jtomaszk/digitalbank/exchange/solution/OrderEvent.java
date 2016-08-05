package com.jtomaszk.digitalbank.exchange.solution;

import com.jtomaszk.digitalbank.exchange.solution.dto.AbstractOrder;
import com.jtomaszk.digitalbank.exchange.solution.dto.CancellationOrder;
import com.jtomaszk.digitalbank.exchange.solution.dto.ModificationOrder;
import com.jtomaszk.digitalbank.exchange.solution.dto.Order;

/**
 * Created by Jarema Tomaszkiewicz
 */
public class OrderEvent {
    private AbstractOrder value;

    public void set(AbstractOrder value) {
        this.value = value;
    }

    public boolean isOrder() {
        return value instanceof Order;
    }

    public boolean isModificationOrder() {
        return value instanceof ModificationOrder;
    }

    public boolean isCancellationOrder() {
        return value instanceof CancellationOrder;
    }

    public ModificationOrder getModificationOrder() {
        return (ModificationOrder) value;
    }

    public Order getOrder() {
        return (Order) value;
    }

    public CancellationOrder getCancellationOrder() {
        return (CancellationOrder) value;
    }
}
