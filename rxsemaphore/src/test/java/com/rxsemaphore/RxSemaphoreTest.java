package com.rxsemaphore;

import io.reactivex.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;


public class RxSemaphoreTest {

    private RxSemaphore rxSemaphore;

    private Long response;
    private final Long emittedItem = 10l;
    private Throwable throwable;
    private final Throwable thrownThrowable = new Exception("Error");

    @Before
    public void init() {
        if (rxSemaphore == null)
            rxSemaphore = new RxSemaphore();

        response = null;
        throwable = null;
    }

    //-------------------------------------- Single ------------------------------------------------
    @Test
    public void emitOneValueWithSingle_whenViewActive_shouldPerformLogicImmediately() throws Exception {
        activeView();

        Single.just(emittedItem)
                .compose(getRxSemaphoreTransformer())
                .subscribe(integer -> response = integer);

        Assert.assertEquals(emittedItem, response);
    }

    @Test
    public void emitOneValueWithSingle_whenViewInactiveThenActive_shouldPerformLogicWhenViewBecomeActive() throws Exception {
        deactivateView();

        Single.just(emittedItem)
                .compose(getRxSemaphoreTransformer())
                .subscribe(integer -> response = integer);

        Assert.assertNotEquals(emittedItem, response);

        activeView();

        Assert.assertEquals(emittedItem, response);
    }

    @Test
    public void emitErrorWithSingle_whenViewInactiveThenActive_shouldThrowWhenViewBecomeActive() throws Exception {

        deactivateView();

        Single.error(new Exception("Error"))
                .compose(getRxSemaphoreTransformer())
                .subscribe(o -> {
                }, throwable -> this.throwable = throwable);

        Assert.assertNull(throwable);

        activeView();

        Assert.assertEquals(thrownThrowable.getMessage(), throwable.getMessage());
    }

    //-------------------------------------- Completable -------------------------------------------
    @Test
    public void emitOneValueWithCompletable_whenViewActive_shouldPerformLogicImmediately() throws Exception {
        activeView();

        Completable.complete()
                .compose(getRxSemaphoreTransformer())
                .subscribe(() -> response = emittedItem);

        Assert.assertEquals(emittedItem, response);
    }

    @Test
    public void emitOneValueWithCompletable_whenViewInactiveThenActive_shouldPerformLogicWhenViewBecomeActive() throws Exception {
        deactivateView();

        Completable.complete()
                .compose(getRxSemaphoreTransformer())
                .subscribe(() -> response = emittedItem);

        Assert.assertNotEquals(emittedItem, response);

        activeView();

        Assert.assertEquals(emittedItem, response);
    }

    @Test
    public void emitErrorWithCompletable_whenViewInactiveThenActive_shouldThrowWhenViewBecomeActive() throws Exception {

        deactivateView();

        Completable.error(new Exception("Error"))
                .compose(getRxSemaphoreTransformer())
                .subscribe(() -> {
                }, throwable -> this.throwable = throwable);

        Assert.assertNull(throwable);

        activeView();

        Assert.assertEquals(thrownThrowable.getMessage(), throwable.getMessage());
    }

    //-------------------------------------- Maybe -------------------------------------------------
    @Test
    public void emitOneValueWithMay_whenViewActive_shouldPerformLogicImmediately() throws Exception {
        activeView();

        Maybe.just(emittedItem)
                .compose(getRxSemaphoreTransformer())
                .subscribe(integer -> response = integer);

        Assert.assertEquals(emittedItem, response);
    }

    @Test
    public void emitOneValueWithMay_whenViewInactiveThenActive_shouldPerformLogicWhenViewBecomeActive() throws Exception {
        deactivateView();

        Maybe.just(emittedItem)
                .compose(getRxSemaphoreTransformer())
                .subscribe(integer -> response = integer);

        Assert.assertNotEquals(emittedItem, response);

        activeView();

        Assert.assertEquals(emittedItem, response);
    }

    @Test
    public void emitErrorWithMay_whenViewInactiveThenActive_shouldThrowWhenViewBecomeActive() throws Exception {
        deactivateView();

        Maybe.error(new Exception("Error"))
                .compose(getRxSemaphoreTransformer())
                .subscribe(o -> {
                }, throwable -> this.throwable = throwable);

        Assert.assertNull(throwable);

        activeView();

        Assert.assertEquals(thrownThrowable.getMessage(), throwable.getMessage());
    }

    @Test
    public void emitEmptyWithMay_whenViewInactiveThenActive_shouldThrowWhenViewBecomeActive() throws Exception {
        deactivateView();

        Maybe.empty()
                .compose(getRxSemaphoreTransformer())
                .subscribe(o -> {
                        },
                        Throwable::printStackTrace,
                        () -> response = emittedItem);

        Assert.assertNotEquals(emittedItem, response);

        activeView();

        Assert.assertEquals(emittedItem, response);
    }


    //-------------------------------------- Observable --------------------------------------------
    @Test
    public void emitOneValueWithObservable_whenViewActive_shouldPerformLogicImmediately() throws Exception {
        activeView();

        Observable.just(emittedItem)
                .compose(getRxSemaphoreTransformer())
                .subscribe(integer -> response = integer);

        Assert.assertEquals(emittedItem, response);
    }

    @Test
    public void emitOneValueWithObservable_whenViewInactiveThenActive_shouldPerformLogicWhenViewBecomeActive() throws Exception {
        deactivateView();

        Observable.just(emittedItem)
                .compose(getRxSemaphoreTransformer())
                .subscribe(integer -> response = integer);

        Assert.assertNotEquals(emittedItem, response);

        activeView();

        Assert.assertEquals(emittedItem, response);
    }

    @Test
    public void emitErrorWithObservable_whenViewInactiveThenActive_shouldThrowWhenViewBecomeActive() throws Exception {

        deactivateView();

        Observable.error(new Exception("Error"))
                .compose(getRxSemaphoreTransformer())
                .subscribe(o -> {
                }, throwable -> this.throwable = throwable);

        Assert.assertNull(throwable);

        activeView();

        Assert.assertEquals(thrownThrowable.getMessage(), throwable.getMessage());
    }

    @Test
    public void emitManyValuesWithObservable_whenViewInactiveThenActive_shouldPerformLogicWhenViewBecomeActive() throws Exception {
        deactivateView();

        Observable.interval(1, TimeUnit.SECONDS)
                .take(3)
                .compose(getRxSemaphoreTransformer())
                .subscribe(integer -> response = integer);

        sleep(1);

        Assert.assertNull(response);

        activeView();

        sleep(0.1);

        Assert.assertEquals(0, response.intValue());

        sleep(1);

        Assert.assertEquals(1, response.intValue());

        sleep(1);

        Assert.assertEquals(2, response.intValue());
    }

    //-------------------------------------- Flowable --------------------------------------------
    @Test
    public void emitOneValueWithFlowable_whenViewActive_shouldPerformLogicImmediately() throws Exception {
        activeView();

        Flowable.just(emittedItem)
                .compose(getRxSemaphoreTransformer())
                .subscribe(integer -> response = integer);

        Assert.assertEquals(emittedItem, response);
    }

    @Test
    public void emitOneValueWithFlowable_whenViewInactiveThenActive_shouldPerformLogicWhenViewBecomeActive() throws Exception {
        deactivateView();

        Flowable.just(emittedItem)
                .compose(getRxSemaphoreTransformer())
                .subscribe(integer -> response = integer);

        Assert.assertNotEquals(emittedItem, response);

        activeView();

        Assert.assertEquals(emittedItem, response);
    }

    @Test
    public void emitErrorWithFlowable_whenViewInactiveThenActive_shouldThrowWhenViewBecomeActive() throws Exception {

        deactivateView();

        Flowable.error(new Exception("Error"))
                .compose(getRxSemaphoreTransformer())
                .subscribe(o -> {
                }, throwable -> this.throwable = throwable);

        Assert.assertNull(throwable);

        activeView();

        Assert.assertEquals(thrownThrowable.getMessage(), throwable.getMessage());
    }

    @Test
    public void emitManyValuesWithOFlowable_whenViewInactiveThenActive_shouldPerformLogicWhenViewBecomeActive() throws Exception {
        deactivateView();

        Flowable.interval(1, TimeUnit.SECONDS)
                .take(3)
                .compose(getRxSemaphoreTransformer())
                .subscribe(integer -> response = integer);

        sleep(1);

        Assert.assertNull(response);

        activeView();

        sleep(0.1);

        Assert.assertEquals(0, response.intValue());

        sleep(1);

        Assert.assertEquals(1, response.intValue());

        sleep(1);

        Assert.assertEquals(2, response.intValue());
    }

    private void activeView() {
        rxSemaphore.release();

    }

    private void deactivateView() {
        rxSemaphore.acquire();
    }

    private void sleep(double seconds) throws InterruptedException {
        Thread.sleep((int) (seconds * 1000));
    }

    @SuppressWarnings("unchecked")
    private <T> RxSemaphoreTransformer<T> getRxSemaphoreTransformer() {
        return rxSemaphore.getRxLifecycleAwareTransformer();
    }
}