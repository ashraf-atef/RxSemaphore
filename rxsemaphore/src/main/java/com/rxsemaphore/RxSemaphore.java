package com.rxsemaphore;

import io.reactivex.subjects.BehaviorSubject;

public class RxSemaphore {

    private BehaviorSubject<Signal> lifecycle;
    private RxSemaphoreTransformer rxSemaphoreTransformer;

    public RxSemaphore() {
        this.lifecycle = BehaviorSubject.create();
        this.rxSemaphoreTransformer = new RxSemaphoreTransformer(lifecycle);
    }

    /**
     *
     * @param <T>
     * @return the transformer that you will compose it into your stream to apply semaphore logic
     */
    @SuppressWarnings("unchecked")
    public <T> RxSemaphoreTransformer<T> getRxLifecycleAwareTransformer() {
        return (RxSemaphoreTransformer<T>) rxSemaphoreTransformer;
    }

    /**
     * Call this function when you become ready to start receiving the stream emissions
     */
    public void release() {
        lifecycle.onNext(Signal.ACTIVE);
    }

    /**
     * Call this function when you become you want to stop receiving the stream emissions
     */
    public void acquire() {
        lifecycle.onNext(Signal.INACTIVE);
    }
}
