package com.mapbox.services.android.navigation.v5.eh.loop;

class RunnableMessage implements Message {

    private Runnable runnable;

    RunnableMessage(final Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void execute() {
        runnable.run();
    }
}
