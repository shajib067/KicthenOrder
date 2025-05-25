package com.css.challenge.harness;

import com.css.challenge.client.*;

import java.time.Duration;
import java.util.List;

public class KitchenSimulator {

    private final KitchenManager kitchen;
    private final Duration rate;
    private final Duration max;

    public KitchenSimulator(KitchenManager kitchen, Duration rate, Duration max) {
        this.kitchen = kitchen;
        this.rate = rate;
        this.max = max;
    }

    public void runSimulation(List<Order> orders) throws InterruptedException {
        for (Order order : orders) {
            //System.out.println("Received: " + order);
            kitchen.placeOrder(order);
            Thread.sleep(rate.toMillis());
        }
        Thread.sleep(max.toMillis() + 1000); // Allow last pickups to happen
    }
}
