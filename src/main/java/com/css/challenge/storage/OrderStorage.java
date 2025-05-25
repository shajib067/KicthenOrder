package com.css.challenge.storage;

import java.util.*;

import com.css.challenge.model.Order;

public class OrderStorage {
    private final int hotShelfSize;
    private final int coldShelfSize;
    private final int roomShelfSize;

    private final Map<String, Order> heater = new HashMap<>();
    private final Map<String, Order> cooler = new HashMap<>();
    private final Map<String, Order> shelf = new HashMap<>();
    private final PriorityQueue<Order> shelfQueue = new PriorityQueue<>((a, b) -> Long.compare(a.getExpiry(), b.getExpiry()));

    public OrderStorage(int hotShelfSize, int coldShelfSize, int roomShelfSize) {
        this.hotShelfSize = hotShelfSize;
        this.coldShelfSize = coldShelfSize;
        this.roomShelfSize = roomShelfSize;
    }

    public boolean tryPlace(Order order) {
        switch (order.getTemp()) {
            case "hot":
                if (heater.size() < hotShelfSize) {
                    heater.put(order.getId(), order);
                    return true;
                }
                break;
            case "cold":
                if (cooler.size() < coldShelfSize) {
                    cooler.put(order.getId(), order);
                    return true;
                }
                break;
        }

        if (shelf.size() < roomShelfSize) {
            shelf.put(order.getId(), order);
            shelfQueue.add(order);
            return true;
        }

        return false;
    }

    public boolean tryMoveToTempShelf(Order order) {
        if (order.getTemp().equals("hot") && heater.size() < hotShelfSize) {
            removeFromRoomShelf(order);
            heater.put(order.getId(), order);
            return true;
        } else if (order.getTemp().equals("cold") && cooler.size() < coldShelfSize) {
            removeFromRoomShelf(order);
            cooler.put(order.getId(), order);
            return true;
        }
        return false;
    }

    public void addToRoomShelf(Order order) {
        shelf.put(order.getId(), order);
        shelfQueue.add(order);
    }

    public Order discardLeastFresh() {
        Order toDiscard = shelfQueue.poll();
        if (toDiscard != null) {
            shelf.remove(toDiscard.getId());
        }
        return toDiscard;
    }

    public boolean pickup(String id) {
        boolean removed = heater.remove(id) != null || cooler.remove(id) != null || shelf.remove(id) != null;
        if (removed) {
            shelfQueue.removeIf(o -> o.getId().equals(id));
        }
        return removed;
    }

    public Collection<Order> getRoomShelfOrders() {
        return new ArrayList<>(shelf.values());
    }

    private void removeFromRoomShelf(Order o) {
        shelf.remove(o.getId());
        shelfQueue.remove(o);
    }

    public boolean isRoomShelfFull() {
        return shelf.size() >= roomShelfSize;
    }
}
