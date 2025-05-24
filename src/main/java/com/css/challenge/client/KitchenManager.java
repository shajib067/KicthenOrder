package com.css.challenge.client;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class KitchenManager {
    private final int MAX_HOT = 6, MAX_COLD = 6, MAX_SHELF = 12;

    private final Map<String, Order> heater = new HashMap<>();
    private final Map<String, Order> cooler = new HashMap<>();
    private final Map<String, Order> shelf = new HashMap<>();
    private final PriorityQueue<Order> shelfQueue;

    private final ReentrantLock lock = new ReentrantLock();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final List<Action> actions = new CopyOnWriteArrayList<>();
    private final Random random = new Random();

    private final long minPickupMicros;
    private final long maxPickupMicros;

    public KitchenManager(long minPickupMicros, long maxPickupMicros) {
        this.minPickupMicros = minPickupMicros;
        this.maxPickupMicros = maxPickupMicros;
        this.shelfQueue = new PriorityQueue<>((a, b) -> Long.compare(a.getExpiry(), b.getExpiry()));
    }

    public void placeOrder(Order order) {
        lock.lock();
        try {
            order.updateExpiry(order.getTemp());

            boolean placed = tryPlace(order);
            if (!placed) {
            	order.updateExpiry("room");
                boolean moved = tryMakeRoomOnShelf();
                if (!moved && shelf.size() >= MAX_SHELF) {
                    discardLeastFresh();
                }
                shelf.put(order.getId(), order);
                shelfQueue.add(order);
                logAction("place", order.getId());
            }
        } finally {
            lock.unlock();
        }

        // Schedule pickup
        long delay = random.nextLong(minPickupMicros, maxPickupMicros);
        scheduler.schedule(() -> pickupOrder(order.getId()), delay, TimeUnit.MICROSECONDS);
    }

    private boolean tryPlace(Order order) {
        if (order.getTemp().equals("hot") && heater.size() < MAX_HOT) {
            heater.put(order.getId(), order);
            logAction("place", order.getId());
            return true;
        }
        if (order.getTemp().equals("cold") && cooler.size() < MAX_COLD) {
            cooler.put(order.getId(), order);
            logAction("place", order.getId());
            return true;
        }
        if (shelf.size() < MAX_SHELF) {
            shelf.put(order.getId(), order);
            shelfQueue.add(order);
            logAction("place", order.getId());
            return true;
        }
        return false;
    }

    private boolean tryMakeRoomOnShelf() {
        Iterator<Order> it = shelf.values().iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o.getTemp().equals("hot") && heater.size() < MAX_HOT) {
                it.remove();
                shelfQueue.remove(o);
                heater.put(o.getId(), o);
                logAction("move", o.getId());
                return true;
            } else if (o.getTemp().equals("cold") && cooler.size() < MAX_COLD) {
                it.remove();
                shelfQueue.remove(o);
                cooler.put(o.getId(), o);
                logAction("move", o.getId());
                return true;
            }
        }
        return false;
    }

    private void discardLeastFresh() {
        Order toDiscard = shelfQueue.poll();
        if (toDiscard != null) {
            shelf.remove(toDiscard.getId());
            logAction("discard", toDiscard.getId());
        }
    }

    public void pickupOrder(String id) {
        lock.lock();
        try {
            if (heater.remove(id) != null || cooler.remove(id) != null || shelf.remove(id) != null) {
                shelfQueue.removeIf(o -> o.getId().equals(id));
                logAction("pickup", id);
            }
        } finally {
            lock.unlock();
        }
    }

    private void logAction(String type, String id) {
        long timestampMicros = Instant.now().toEpochMilli() * 1000;
        actions.add(new Action(timestampMicros, id, type));
        System.out.printf("%s | %s\n", type.toUpperCase(), id);
    }

    public List<Action> getActions() {
        return actions;
    }
    
    public void killExecutor() {
    	this.scheduler.shutdown();
    }
}

