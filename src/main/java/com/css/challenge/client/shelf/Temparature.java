package com.css.challenge.client.shelf;

public enum Temparature {
    HOT("hot"),
    COLD("cold"),
    ROOM("room");

    private final String value;

    Temparature(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    public static Temparature fromValue(String value) {
        for (Temparature temp : Temparature.values()) {
            if (temp.value.equalsIgnoreCase(value)) {
                return temp;
            }
        }
        throw new IllegalArgumentException("Unknown temperature value: " + value);
    }

}
