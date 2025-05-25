package com.css.challenge.client.shelf;

import java.util.PriorityQueue;

import com.css.challenge.client.Order;

public class RoomShelf extends Shelf {
    private final PriorityQueue<String> shelfQueue;

    public RoomShelf(int maxSize) {
        super(maxSize, Temparature.ROOM);
        this.shelfQueue = new PriorityQueue<>((a, b) -> 
        					Long.compare(this.getOrder(a).getExpiry(), this.getOrder(b).getExpiry()));
    }

    public String discardOrder() {
        String orderId = shelfQueue.poll();
        if(orderId != null) {
        	this.removeOrder(orderId);
        }
        return orderId;
    }
    
    @Override
    public boolean addOrder(Order order) {
    	if(shelfQueue.contains(order.getId())) {
    		shelfQueue.remove(order.getId());
    		this.removeOrder(order.getId());
    	}
    	return super.addOrder(order);
    }
    
    @Override
    public boolean removeOrder(String orderId) {
    	if(super.removeOrder(orderId))
    		return shelfQueue.remove(orderId);
    	else
    		return false;
    }
    
    // get hot or cold order
    public Order getHotOrColdOrder(Temparature temp) {
    	for(Order order : this.getAllOrders().values())
    		if(order.getTemp().equals(temp.toString()))
    			return order;
    	return null;
    }
}
