package com.css.challenge.client;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


public class KitchenManager {
    
	private final int hotShelfSize;
	private final int coldShelfSize;
	private final int roomShelfSize;

    private final Map<String, Order> heater = new HashMap<>();
    private final Map<String, Order> cooler = new HashMap<>();
    private final Map<String, Order> shelf = new HashMap<>();
    private final PriorityQueue<Order> shelfQueue;

    private final ReentrantLock lock = new ReentrantLock();

    private ActionLogger log;
    
    public KitchenManager(ActionLogger log, int hotShelfSize, int coldShelfSize, int roomShelfSize) {
        this.shelfQueue = new PriorityQueue<>((a, b) -> Long.compare(a.getExpiry(), b.getExpiry()));
        this.log = log;
        this.hotShelfSize = hotShelfSize;
        this.coldShelfSize = coldShelfSize;
        this.roomShelfSize = roomShelfSize;
    }

    public void placeOrder(Order order) {
        lock.lock();
        try {
            order.updateExpiry(order.getTemp());

            boolean placed = tryPlace(order);
            if (!placed) {
            	order.updateExpiry("room");
                boolean moved = tryMakeRoomOnShelf();
                if (!moved && shelf.size() >= roomShelfSize) {
                    discardLeastFresh();
                }
                shelf.put(order.getId(), order);
                shelfQueue.add(order);
                log.logAction("place", order.getId());
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean tryPlace(Order order) {
        if (order.getTemp().equals("hot") && heater.size() < hotShelfSize) {
            heater.put(order.getId(), order);
            log.logAction("place", order.getId());
            return true;
        }
        if (order.getTemp().equals("cold") && cooler.size() < coldShelfSize) {
            cooler.put(order.getId(), order);
            log.logAction("place", order.getId());
            return true;
        }
        if (shelf.size() < roomShelfSize) {
            shelf.put(order.getId(), order);
            shelfQueue.add(order);
            log.logAction("place", order.getId());
            return true;
        }
        return false;
    }

    private boolean tryMakeRoomOnShelf() {
        Iterator<Order> it = shelf.values().iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o.getTemp().equals("hot") && heater.size() < hotShelfSize) {
                it.remove();
                shelfQueue.remove(o);
                heater.put(o.getId(), o);
                log.logAction("move", o.getId());
                return true;
            } else if (o.getTemp().equals("cold") && cooler.size() < coldShelfSize) {
                it.remove();
                shelfQueue.remove(o);
                cooler.put(o.getId(), o);
                log.logAction("move", o.getId());
                return true;
            }
        }
        return false;
    }

    private void discardLeastFresh() {
        Order toDiscard = shelfQueue.poll();
        if (toDiscard != null) {
            shelf.remove(toDiscard.getId());
            log.logAction("discard", toDiscard.getId());
        }
    }

    public void pickupOrder(String id) {
        lock.lock();
        try {
            if (heater.remove(id) != null || cooler.remove(id) != null || shelf.remove(id) != null) {
                shelfQueue.removeIf(o -> o.getId().equals(id));
                log.logAction("pickup", id);
            }
        } finally {
            lock.unlock();
        }
    }
}
