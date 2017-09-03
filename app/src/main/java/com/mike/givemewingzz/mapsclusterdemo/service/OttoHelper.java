package com.mike.givemewingzz.mapsclusterdemo.service;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class OttoHelper {

    // Our singleton event bus.
    private static Bus bestBus = new Bus(ThreadEnforcer.MAIN);

    private OttoHelper() {
    }

    public static Bus getBus() {
        return bestBus;
    }

    public static void register(Object object) {
        bestBus.register(object);
    }

    public static void unregister(Object object) {
        bestBus.unregister(object);
    }

    public static void post(Object event) {
        bestBus.post(event);
    }

}
