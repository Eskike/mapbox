package com.mapbox.services.android.navigation.v5.eh.loop;

class ShutdownMessage implements Message {
    private final Looper looper;

    ShutdownMessage(final Looper looper) {
        this.looper = looper;
    }

    @Override
    public void execute() {
        looper.halt();
    }
}
