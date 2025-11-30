package com.carboncredit.service;

import com.carboncredit.model.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Component
public class OrderBook {

    // Buy Orders: Highest Price First, then Oldest First
    private final TreeMap<Double, List<Order>> buyOrders = new TreeMap<>(Collections.reverseOrder());

    // Sell Orders: Lowest Price First, then Oldest First
    private final TreeMap<Double, List<Order>> sellOrders = new TreeMap<>();

    // Quick lookup for cancellation
    private final Map<String, Order> orderMap = new ConcurrentHashMap<>();

    public synchronized void addOrder(Order order) {
        orderMap.put(order.getId(), order);
        if (order.getType() == Order.OrderType.BUY) {
            buyOrders.computeIfAbsent(order.getPricePerUnit(), k -> new ArrayList<>()).add(order);
        } else {
            sellOrders.computeIfAbsent(order.getPricePerUnit(), k -> new ArrayList<>()).add(order);
        }
    }

    public synchronized void removeOrder(String orderId) {
        Order order = orderMap.remove(orderId);
        if (order != null) {
            if (order.getType() == Order.OrderType.BUY) {
                List<Order> orders = buyOrders.get(order.getPricePerUnit());
                if (orders != null) {
                    orders.remove(order);
                    if (orders.isEmpty()) {
                        buyOrders.remove(order.getPricePerUnit());
                    }
                }
            } else {
                List<Order> orders = sellOrders.get(order.getPricePerUnit());
                if (orders != null) {
                    orders.remove(order);
                    if (orders.isEmpty()) {
                        sellOrders.remove(order.getPricePerUnit());
                    }
                }
            }
        }
    }

    public synchronized Order getBestBuy() {
        if (buyOrders.isEmpty())
            return null;
        List<Order> orders = buyOrders.firstEntry().getValue();
        return orders.isEmpty() ? null : orders.get(0);
    }

    public synchronized Order getBestSell() {
        if (sellOrders.isEmpty())
            return null;
        List<Order> orders = sellOrders.firstEntry().getValue();
        return orders.isEmpty() ? null : orders.get(0);
    }

    public synchronized boolean hasMatch() {
        Order bestBuy = getBestBuy();
        Order bestSell = getBestSell();
        return bestBuy != null && bestSell != null && bestBuy.getPricePerUnit() >= bestSell.getPricePerUnit();
    }

    public synchronized void clear() {
        buyOrders.clear();
        sellOrders.clear();
        orderMap.clear();
    }
}
