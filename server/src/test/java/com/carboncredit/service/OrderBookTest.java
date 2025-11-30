package com.carboncredit.service;

import com.carboncredit.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderBookTest {

    private OrderBook orderBook;

    @BeforeEach
    public void setup() {
        orderBook = new OrderBook();
    }

    @Test
    public void testAddAndGetBestOrders() {
        Order buy1 = createOrder("b1", Order.OrderType.BUY, 100.0);
        Order buy2 = createOrder("b2", Order.OrderType.BUY, 110.0);
        Order sell1 = createOrder("s1", Order.OrderType.SELL, 90.0);
        Order sell2 = createOrder("s2", Order.OrderType.SELL, 80.0);

        orderBook.addOrder(buy1);
        orderBook.addOrder(buy2);
        orderBook.addOrder(sell1);
        orderBook.addOrder(sell2);

        assertEquals(buy2, orderBook.getBestBuy()); // Highest buy
        assertEquals(sell2, orderBook.getBestSell()); // Lowest sell
    }

    @Test
    public void testHasMatch() {
        Order buy = createOrder("b1", Order.OrderType.BUY, 100.0);
        Order sell = createOrder("s1", Order.OrderType.SELL, 90.0);

        orderBook.addOrder(buy);
        orderBook.addOrder(sell);

        assertTrue(orderBook.hasMatch());
    }

    @Test
    public void testNoMatch() {
        Order buy = createOrder("b1", Order.OrderType.BUY, 80.0);
        Order sell = createOrder("s1", Order.OrderType.SELL, 90.0);

        orderBook.addOrder(buy);
        orderBook.addOrder(sell);

        assertFalse(orderBook.hasMatch());
    }

    @Test
    public void testRemoveOrder() {
        Order buy = createOrder("b1", Order.OrderType.BUY, 100.0);
        orderBook.addOrder(buy);
        assertEquals(buy, orderBook.getBestBuy());

        orderBook.removeOrder("b1");
        assertNull(orderBook.getBestBuy());
    }

    private Order createOrder(String id, Order.OrderType type, double price) {
        Order order = new Order();
        order.setId(id);
        order.setType(type);
        order.setPricePerUnit(price);
        order.setQuantity(10.0);
        return order;
    }
}
