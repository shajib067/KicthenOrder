package com.css.challenge.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Order is a json-friendly representation of an order. */
public class Order {
  private final String id; // order id
  private final String name; // food name
  private final String temp; // ideal temperature
  private final int freshness; // freshness in seconds
  private transient long expiry;
  
  public Order(
      @JsonProperty("id") String id,
      @JsonProperty("name") String name,
      @JsonProperty("temp") String temp,
      @JsonProperty("freshness") int freshness) {
    this.id = id;
    this.name = name;
    this.temp = temp;
    this.freshness = freshness;
  }

  public static List<Order> parse(String json) throws IOException {
    return new ObjectMapper().readValue(json, new TypeReference<List<Order>>() {});
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getTemp() {
    return temp;
  }

  public int getFreshness() {
    return freshness;
  }
  
  public long getExpiry() {
	return expiry;
  }

  public void setExpiry(long expiry) {
	this.expiry = expiry;
  }
  
  public void updateExpiry(String shelfTemp) {
	  expiry = System.nanoTime() + (shelfTemp.equals(temp) 
			  ? TimeUnit.SECONDS.toNanos(freshness) 
			  : (TimeUnit.SECONDS.toNanos(freshness) / 2));
  }
  
  @Override
  public String toString() {
    return "{id: " + id + ", name: " + name + ", temp: " + temp + ", freshness:" + freshness + " }";
  }
}
