package com.proxy.event;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * A Bus driver singleton pattern that allows one to easily send messages over {@link
 * ThreadEnforcer#ANY} thread or the {@link ThreadEnforcer#MAIN} ui thread.
 */
@SuppressWarnings("unused")
public class OttoBusDriver {
    private static final String EVENT_POSTED = " Posted";
    private static final String EVENT_UNREGISTERED = "UnRegistering listener: ";
    private static final String EVENT_REGISTERED = "Registering listener: ";
    private static Bus sMainBus;
    private static Bus sAnyBus;

    /**
     * Private constructor for this helper class.
     */
    private OttoBusDriver() {
    }

    /**
     * Singleton Bus.
     *
     * @return this bus
     */
    public static Bus getAnyBusDriver() {
        synchronized (OttoBusDriver.class) {
            if (sAnyBus == null) {
                sAnyBus = new Bus(ThreadEnforcer.ANY);
            }
        }
        return sAnyBus;
    }

    /**
     * Singleton Main Bus.
     *
     * @return this bus
     */
    public static Bus getMainBusDriver() {
        synchronized (OttoBusDriver.class) {
            if (sMainBus == null) {
                sMainBus = new Bus(ThreadEnforcer.MAIN);
            }
        }
        return sMainBus;
    }

    /**
     * Register a subscriber to a thread which {@link ThreadEnforcer#ANY} does no verification.
     *
     * @param subscriber event subscriber.
     */
    public static void register(Object subscriber) {
        Timber.v(EVENT_REGISTERED + subscriber);
        getAnyBusDriver().register(subscriber);
    }

    /**
     * Unregister a subscriber on the thread issued by {@link ThreadEnforcer#ANY}.
     *
     * @param subscriber event subscriber.
     */
    public static void unregister(Object subscriber) {
        Timber.v(EVENT_UNREGISTERED + subscriber);
        getAnyBusDriver().unregister(subscriber);
    }

    /**
     * Post an event on a thread which {@link ThreadEnforcer#ANY} does no verification.
     *
     * @param event event object.
     */
    public static void post(Object event) {
        Timber.v(event.getClass().getSimpleName() + EVENT_POSTED);
        getAnyBusDriver().post(event);
    }

    /**
     * Register a subscriber on the main ui thread enforced by {@link ThreadEnforcer#MAIN}.
     *
     * @param subscriber event subscriber class.
     */
    public static void registerMainThread(Class subscriber) {
        Timber.v(EVENT_REGISTERED + subscriber);
        getMainBusDriver().register(subscriber);
    }

    /**
     * Unregister a subscriber on the main ui thread enforced by {@link ThreadEnforcer#MAIN}.
     *
     * @param subscriber event subscriber class.
     */

    public static void unregisterMainThread(Class subscriber) {
        Timber.v(EVENT_UNREGISTERED + subscriber);
        getMainBusDriver().unregister(subscriber);
    }

    /**
     * Post an event to subscribers on the main ui thread enforced by {@link ThreadEnforcer#MAIN}.
     *
     * @param event event object.
     */
    @DebugLog
    public static void postMainThread(Object event) {
        Timber.v(event.getClass().getSimpleName() + EVENT_POSTED);
        getMainBusDriver().post(event);
    }

}
