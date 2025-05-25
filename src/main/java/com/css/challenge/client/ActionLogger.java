package com.css.challenge.client;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActionLogger {

    private final List<Action> actions = new CopyOnWriteArrayList<>();

    ActionLogger(){}

    public void logAction(String type, String id) {
        long timestampMicros = Instant.now().toEpochMilli() * 1000;
        actions.add(new Action(timestampMicros, id, type));
        System.out.printf("%s | %s\n", type.toUpperCase(), id);
    }

    public List<Action> getActions() {
        return actions;
    }
}
