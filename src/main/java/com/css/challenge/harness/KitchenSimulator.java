package com.css.challenge.harness;

import com.css.challenge.kitchen.KitchenManager;
import com.css.challenge.model.Order;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KitchenSimulator {

    private final KitchenManager kitchen;
    private final Duration rate;
    private final Duration min;
    private final Duration max;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Random random = new Random();

    public KitchenSimulator(KitchenManager kitchen, Duration rate, Duration min, Duration max) {
        this.kitchen = kitchen;
        this.rate = rate;
        this.min = min;
        this.max = max;
    }

    public void runSimulation(List<Order> orders) throws InterruptedException {
    	CountDownLatch latch = new CountDownLatch(orders.size());
        for (Order order : orders) {
            //System.out.println("Received: " + order);
            kitchen.placeOrder(order);
            // Schedule pickup
            long delay = random.nextLong(min.toNanos() / 1000, max.toNanos() / 1000);
            scheduler.schedule(() -> {
            	kitchen.pickupOrder(order.getId());
            	latch.countDown();
            }, delay, TimeUnit.MICROSECONDS);
            Thread.sleep(rate.toMillis());
        }
        latch.await(); // wait until all orders picked up
    }
    
    public void killExecutor() {
    	this.scheduler.shutdown();
    }
}
