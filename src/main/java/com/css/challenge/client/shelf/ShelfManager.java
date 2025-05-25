package com.css.challenge.client.shelf;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.css.challenge.client.ActionLogger;
import com.css.challenge.client.Order;

public class ShelfManager {

    private final Map<Temparature, Shelf> shelves = new ConcurrentHashMap<>();
    private final ActionLogger log;
    
    public ShelfManager(ActionLogger log) {
    	this.log = log;
    }

    public void addShelf(Temparature temp, int maxSize) {
        if (temp == Temparature.ROOM) {
            shelves.put(temp, new RoomShelf(maxSize));
        } else {
            shelves.put(temp, new Shelf(maxSize, temp));
        }
    }

    public void removeShelf(Temparature temp) {
        shelves.remove(temp);
    }

    public boolean hasShelf(Temparature temp) {
        return shelves.containsKey(temp);
    }

    public Shelf getShelf(Temparature temp) {
        return shelves.get(temp);
    }

    public boolean storeOrder(Order order) {
        Temparature temp = Temparature.fromValue(order.getTemp());
        Shelf shelf = shelves.get(temp);
        if (shelf == null) return false;

        // Try placing on primary shelf
        if (!shelf.isFull()) {
            log.logAction("place", order.getId());
        	return shelf.addOrder(order);
        }

        // Fallback to room shelf
        RoomShelf roomShelf = (RoomShelf) shelves.get(Temparature.ROOM);

        if (roomShelf.isFull()) {
            if (!moveHotOrColdToRoomShelf(roomShelf)) {
                String orderId = roomShelf.discardOrder();
                log.logAction("discard", orderId);
            }
        }

        if (!roomShelf.isFull()) {
            return roomShelf.addOrder(order);  // only add if space was created
        } else {
            return false; // couldn't store anywhere
        }
    }

    private boolean moveHotOrColdToRoomShelf(RoomShelf roomShelf) {
        Shelf hotShelf = shelves.get(Temparature.HOT);
        if (hotShelf != null && !hotShelf.isFull()) {
            Order oldOrder = roomShelf.getHotOrColdOrder(Temparature.HOT);
            if (oldOrder != null) {
                roomShelf.removeOrder(oldOrder.getId());
                return hotShelf.addOrder(oldOrder);
            }
        }

        Shelf coldShelf = shelves.get(Temparature.COLD);
        if (coldShelf != null && !coldShelf.isFull()) {
            Order oldOrder = roomShelf.getHotOrColdOrder(Temparature.COLD);
            if (oldOrder != null) {
                roomShelf.removeOrder(oldOrder.getId());
                return coldShelf.addOrder(oldOrder);
            }
        }

        return false;
    }

    public boolean removeOrder(String orderId) {
        for (Shelf shelf : shelves.values()) {
            if (shelf.removeOrder(orderId)) {
                return true;
            }
        }
        return false;
    }

    public Order getOrder(String orderId) {
        for (Shelf shelf : shelves.values()) {
            Order order = shelf.getOrder(orderId);
            if (order != null) {
                return order;
            }
        }
        return null;
    }

    public int totalOrders() {
        return shelves.values().stream()
            .mapToInt(Shelf::size)
            .sum();
    }

    public void clearAllShelves() {
        shelves.clear();
    }

    public Map<Temparature, Shelf> getAllShelves() {
        return Map.copyOf(shelves); // shallow copy, safe if shelves are immutable from caller's view
    }
    
    
    public boolean placeOrder(Order order) {
    		RoomShelf shelf = (RoomShelf) shelves.get(Temparature.ROOM);
            order.updateExpiry(order.getTemp());

            boolean placed = tryPlace(order);
            if (!placed) {
            	order.updateExpiry("room");
                boolean moved = tryMakeRoomOnShelf();
                if (!moved && shelf.isFull()) {
                    discardLeastFresh();
                }
                shelf.addOrder(order);
                log.logAction("place", order.getId());
            }
            return true;
    }

    private boolean tryPlace(Order order) {
    	RoomShelf shelf = (RoomShelf) shelves.get(Temparature.ROOM);
        Shelf heater = shelves.get(Temparature.HOT);
        Shelf cooler = shelves.get(Temparature.COLD);
        if (order.getTemp().equals("hot") && !heater.isFull()) {
            heater.addOrder(order);
            log.logAction("place", order.getId());
            return true;
        }
        if (order.getTemp().equals("cold") && !cooler.isFull()) {
            cooler.addOrder(order);
            log.logAction("place", order.getId());
            return true;
        }
        if (!shelf.isFull()) {
            shelf.addOrder(order);
            log.logAction("place", order.getId());
            return true;
        }
        return false;
    }

    private boolean tryMakeRoomOnShelf() {
        RoomShelf shelf = (RoomShelf) shelves.get(Temparature.ROOM);
        Shelf heater = shelves.get(Temparature.HOT);
        Shelf cooler = shelves.get(Temparature.COLD);
    	Iterator<Order> it = shelf.getAllOrders().values().iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o.getTemp().equals("hot") && !heater.isFull()) {
                shelf.removeOrder(o.getId());
                heater.addOrder(o);
                log.logAction("move", o.getId());
                return true;
            } else if (o.getTemp().equals("cold") && !cooler.isFull()) {
                shelf.removeOrder(o.getId());
                cooler.addOrder(o);
                log.logAction("move", o.getId());
                return true;
            }
        }
        return false;
    }

    private void discardLeastFresh() {
        RoomShelf shelf = (RoomShelf) shelves.get(Temparature.ROOM);
    	String toDiscard = shelf.discardOrder();
        if (toDiscard != null) {
            log.logAction("discard", toDiscard);
        }
    }
}
