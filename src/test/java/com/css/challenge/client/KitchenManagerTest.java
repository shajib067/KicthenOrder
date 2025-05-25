package com.css.challenge.client;

import org.junit.jupiter.api.AfterEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KitchenManagerTest {

    private static long minMicros = 6_000_000; // 2s
    private static long maxMicros = 8_000_000; // 4s
    
    private ActionLogger log = new ActionLogger();

    private KitchenManager manager = new KitchenManager(minMicros, maxMicros, log);

	@AfterEach
	public void killThread() {
		manager.killExecutor();
	}

    @org.junit.jupiter.api.Test
    public void testOverflowShelfDiscardsLeastFreshOrder() throws InterruptedException {

        // Create 19 cold orders to overflow cold and room shelf both
        for (int i = 0; i < 19; i++) {
            Order o = new Order("cold-" + i, "IceCream" + i, "cold", 100);
            manager.placeOrder(o);
        }

        Thread.sleep(6000);

        List<Action> actions = log.getActions();

        // Check a "discard" happened
        boolean discardFound = actions.stream()
            .anyMatch(a -> a.getAction().equals("discard"));

        assertTrue(discardFound, "At least one order should have been discarded");

        // Optional: Ensure cold-6 (first item in room shelf freshness) was the one discarded
        String discardedId = actions.stream()
            .filter(a -> a.getAction().equals("discard"))
            .map(Action::getId)
            .findFirst().orElse("");

        assertEquals(discardedId, "cold-6");
    }
}
