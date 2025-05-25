package com.css.challenge.client;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import com.css.challenge.client.shelf.ShelfManager;

public class KitchenManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final ReentrantLock lock = new ReentrantLock();
    private final Random random = new Random();
    private final ActionLogger log;
    
    private final ShelfManager shelfManager;
    private final long minPickupMicros;
    private final long maxPickupMicros;

    public KitchenManager(long minPickupMicros, long maxPickupMicros, ShelfManager shelfManager, ActionLogger log) {
        this.minPickupMicros = minPickupMicros;
        this.maxPickupMicros = maxPickupMicros;
        this.shelfManager = shelfManager;
        this.log = log;
    }

    public void placeOrder(Order order) {
        lock.lock();
        try {
            boolean placed = shelfManager.placeOrder(order);
            if (placed) {
                log.logAction("place", order.getId());
            } else {
                log.logAction("discard", order.getId());  // Couldn’t place
            }
        } finally {
            lock.unlock();
        }

        // Schedule pickup
        long delay = random.nextLong(minPickupMicros, maxPickupMicros);
        scheduler.schedule(() -> pickupOrder(order.getId()), delay, TimeUnit.MICROSECONDS);
    }

    public void pickupOrder(String id) {
        lock.lock();
        try {
            boolean removed = shelfManager.removeOrder(id);
            if (removed) {
                log.logAction("pickup", id);
            }
        } finally {
            lock.unlock();
        }
    }

    public void killExecutor() {
        this.scheduler.shutdown();
    }
}
