package com.css.challenge.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KitchenManagerTest {

    private ActionLogger log;
    private KitchenManager manager;

    @BeforeEach
    public void setup() {
        log = new ActionLogger();
        manager = new KitchenManager(log, 6, 6, 12); // cold, hot, room capacities
    }

    @Test
    public void testOverflowShelfDiscardsLeastFreshOrder() throws InterruptedException {
        // Create 19 cold orders to overflow cold + room shelves
        for (int i = 0; i < 19; i++) {
            Order o = new Order("cold-" + i, "IceCream" + i, "cold", 100);
            manager.placeOrder(o);
        }

        List<Action> actions = log.getActions();

        // Check that at least one discard occurred
        boolean discardFound = actions.stream()
            .anyMatch(a -> "discard".equals(a.getAction()));

        assertTrue(discardFound, "At least one order should have been discarded");

        // Optional check: Was "cold-6" discarded?
        String discardedId = actions.stream()
            .filter(a -> "discard".equals(a.getAction()))
            .map(Action::getId)
            .findFirst().orElse("");
        assertEquals("cold-6", discardedId, "Order cold-6 was discarded");
    }
}
