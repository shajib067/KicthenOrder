package com.css.challenge.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.css.challenge.client.shelf.ShelfManager;
import com.css.challenge.client.shelf.Temparature;

import java.util.List;
import static org.junit.Assert.*;

public class KitchenManagerTest {

    private static long minMicros = 6_000_000; // 2s
    private static long maxMicros = 8_000_000; // 4s
    private KitchenManager manager;
    private ActionLogger log;

    @Before
    public void init() {
    	log = new ActionLogger();
    	ShelfManager shelfManager = new ShelfManager(log);
    	shelfManager.addShelf(Temparature.HOT, 6);
    	shelfManager.addShelf(Temparature.COLD, 6);
    	shelfManager.addShelf(Temparature.ROOM, 12);
    	manager = new KitchenManager(minMicros, maxMicros, shelfManager, log);
    }
	@After
	public void killThread() {
		manager.killExecutor();
	}

	@Test
	public void testOverflowShelfDiscardsLeastFreshOrder() throws InterruptedException {
	    // Prevent pickups during test
	    long testMinMicros = 30_000_000; 
	    long testMaxMicros = 30_000_001;

	    ShelfManager shelfManager = new ShelfManager(log);
	    shelfManager.addShelf(Temparature.HOT, 6);
	    shelfManager.addShelf(Temparature.COLD, 6);
	    shelfManager.addShelf(Temparature.ROOM, 12);
	    manager = new KitchenManager(testMinMicros, testMaxMicros, shelfManager, log);

	    for (int i = 0; i < 19; i++) {
	        Order o = new Order("cold-" + i, "IceCream" + i, "cold", 100);
	        manager.placeOrder(o);
	    }

	    Thread.sleep(1000); // just let placement finish

	    List<Action> actions = log.getActions();

	    boolean discardFound = actions.stream()
	        .anyMatch(a -> a.getAction().equals("discard"));

	    assertTrue("At least one order should have been discarded", discardFound);
	}

}
