package com.mapbox.services.android.navigation.v5.eh.loop;

import java.util.concurrent.CountDownLatch;

/**
 * Quick latch that can be used to block till a loop is started.
 */
public class StartupLatch {

    private final CountDownLatch latch;

    /**
     * Creates the latch.
     */
    public StartupLatch() {
        this.latch = new CountDownLatch(1);
    }

    /**
     * Called when the Latched loop is started.
     */
    public void started() {
        latch.countDown();
    }

    /**
     * Called by the client to wait for the Loop startup.
     */
    public void await() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
