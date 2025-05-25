package com.css.challenge.client.shelf;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.css.challenge.client.Order;

public class Shelf {
    private final Map<String, Order> storage;
    private final int maxSize;
    private final Temparature temp;

    public Shelf(int maxSize, Temparature temp) {
        this.maxSize = maxSize;
        this.temp = temp;
        this.storage = new ConcurrentHashMap<>();
    }

    public Temparature getTemperature() {
        return temp;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public boolean isFull() {
        return storage.size() >= maxSize;
    }

    public boolean addOrder(Order order) {
        return isFull() ? false : storage.put(order.getId(), order) == null;
    }

    public boolean removeOrder(String orderId) {
        return storage.remove(orderId) != null;
    }

    public Order getOrder(String orderId) {
        return storage.get(orderId);
    }

    public Map<String, Order> getAllOrders() {
        return new HashMap<>(storage);
    }

    public int size() {
        return storage.size();
    }
}
