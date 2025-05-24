package com.css.challenge.client;

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

}
