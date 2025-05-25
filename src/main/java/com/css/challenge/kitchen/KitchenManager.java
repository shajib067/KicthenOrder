package com.css.challenge.kitchen;

import com.css.challenge.log.ActionLogger;
import com.css.challenge.model.Order;
import com.css.challenge.storage.OrderStorage;

import java.util.concurrent.locks.ReentrantLock;

public class KitchenManager {

    private final OrderStorage storage;
    private final ReentrantLock lock = new ReentrantLock();
    private final ActionLogger log;

    public KitchenManager(ActionLogger log, int hotShelfSize, int coldShelfSize, int roomShelfSize) {
        this.log = log;
        this.storage = new OrderStorage(hotShelfSize, coldShelfSize, roomShelfSize);
    }

    public void placeOrder(Order order) {
        lock.lock();
        try {
            order.updateExpiry(order.getTemp());

            if (!storage.tryPlace(order)) {
                order.updateExpiry("room");
                boolean moved = tryMakeRoomOnShelf(order);
                if (!moved && storage.isRoomShelfFull()) {
                    Order discarded = storage.discardLeastFresh();
                    if (discarded != null) log.logAction("discard", discarded.getId());
                }
                storage.addToRoomShelf(order);
            }

            log.logAction("place", order.getId());
        } finally {
            lock.unlock();
        }
    }

    private boolean tryMakeRoomOnShelf(Order newOrder) {
        for (Order existing : storage.getRoomShelfOrders()) {
            if (storage.tryMoveToTempShelf(existing)) {
                log.logAction("move", existing.getId());
                return true;
            }
        }
        return false;
    }

    public void pickupOrder(String id) {
        lock.lock();
        try {
            if (storage.pickup(id)) {
                log.logAction("pickup", id);
            }
        } finally {
            lock.unlock();
        }
    }
}
