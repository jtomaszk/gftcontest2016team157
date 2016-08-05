package com.jtomaszk.digitalbank.exchange.solution.util;

import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import com.jtomaszk.digitalbank.exchange.solution.dto.Order;
import pl.wavesoftware.eid.exceptions.EidRuntimeException;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Jarema Tomaszkiewicz
 */
public class SortedOrdersCollection implements Collection<Order> {

    private SortedMultiset<Order> tree = TreeMultiset.create();

    @Override
    public int size() {
        return tree.size();
    }

    @Override
    public boolean isEmpty() {
        return tree.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return tree.contains(o);
    }

    @Override
    public Iterator<Order> iterator() {
        return tree.iterator();
    }

    @Override
    public Object[] toArray() {
        return tree.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return tree.toArray(a);
    }

    @Override
    public boolean add(Order element) {
        return tree.add(element);
    }

    @Override
    public boolean remove(Object o) {
        return tree.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        int i = 0;
        for (Object v : c) {
            if (!contains(v)) {
                return false;
            }
            i++;
        }
        return i > 0;
    }

    @Override
    public boolean addAll(Collection<? extends Order> c) {
        int i = 0;
        for (Order v : c) {
            if (add(v)) {
                i++;
            }
        }
        return i == c.size();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int i = 0;
        for (Object v : c) {
            if (remove(v)) {
                i++;
            }
        }
        return i == c.size();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new EidRuntimeException("20160805:095712", "Not implemented");
    }

    @Override
    public void clear() {
        tree.clear();
    }

}
