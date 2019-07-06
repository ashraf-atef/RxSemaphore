package com.rxsemaphore;


import io.reactivex.*;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;
import org.reactivestreams.Publisher;

/**
 * @param <T> A transformer that make your RX stream doesn't emit or throw exception until the
 * lifecycle behaviour subject emit active event(Resume).
 */
public class RxSemaphoreTransformer<T> implements
        FlowableTransformer<T, T>,
        ObservableTransformer<T, T>,
        CompletableTransformer,
        SingleTransformer<T, T>,
        MaybeTransformer<T, T> {

    private BehaviorSubject<Signal> lifecycleBehaviorSubject;

    public RxSemaphoreTransformer(BehaviorSubject<Signal> lifecycleBehaviorSubject) {
        this.lifecycleBehaviorSubject = lifecycleBehaviorSubject;
    }

    /**
     * @param upstream
     * @return Do the same logic like {@link RxSemaphoreTransformer#applyLogic(Observable)} but we
     * can't call this function because by converting the flowable to observable and passing it to
     * this function we will lose the back pressure strategy and when can't return it back when
     * we convert the returned ovservable from apply logic function to flowable because there is no
     * function that get the back pressure strategy of flowable.
     */
    @Override
    public Publisher<T> apply(Flowable<T> upstream) {
        return upstream
                .map(mapTToWrapperFunction)
                .onErrorReturn(mapThrowableToWrapperFunction)
                .concatMap(tWrapper -> getLifecycleObservable(tWrapper)
                        .toFlowable(BackpressureStrategy.LATEST))
                .map(mapWrapperToTFunction);
    }

    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        return applyLogic(upstream);
    }

    @Override
    public CompletableSource apply(Completable upstream) {
        return applyLogic((Observable<T>) upstream.andThen(Observable.just("")))
                .ignoreElements();
    }

    @Override
    public SingleSource<T> apply(Single<T> upstream) {
        return applyLogic(upstream.toObservable())
                .elementAtOrError(0);
    }

    /**
     * @param upstream
     * @return Do the same logic like {@link RxSemaphoreTransformer#applyLogic(Observable)} except
     * few things. We need to handle if the upstream maybe emit zero item then we need to make him
     * to emit empty wrapper instead of complete the stream and after the we receive active signal
     * we will ignore any wrapper that contains empty T and Throwable.
     */
    @Override
    public MaybeSource<T> apply(Maybe<T> upstream) {
        return upstream
                .map(mapTToWrapperFunction)
                .switchIfEmpty(Maybe.just(new Wrapper<T>()))
                .onErrorReturn(mapThrowableToWrapperFunction)
                .concatMap(tWrapper -> getLifecycleObservable(tWrapper).firstElement())
                .filter(tWrapper -> tWrapper.t != null || tWrapper.throwable != null)
                .map(mapWrapperToTFunction);
    }

    /**
     * @param observable
     * @return observable that:
     * <p>
     * 1- converts stream type from T to Wrapper<T> with
     * {@link RxSemaphoreTransformer#mapTToWrapperFunction}.
     * <p>
     * 2- Fetch any throwable before thrown and save it inside a wrapper<T>  with
     * {@link RxSemaphoreTransformer#mapThrowableToWrapperFunction}.
     * <p>
     * 3- Convert any event emitted by the main observable to observable will emit the same event
     * when the lifecycle become active with {@link RxSemaphoreTransformer#getLifecycleObservable}
     * => We used concat map instead of flat map because concat map preserve the order of items.
     * <p>
     * 4- Return back the stream type from Wrapper<T> to T which is the main type or throw the
     * saved throwable if found with {@link RxSemaphoreTransformer#mapWrapperToTFunction}
     */
    private Observable<T> applyLogic(Observable<T> observable) {
        return observable
                .map(mapTToWrapperFunction)
                .onErrorReturn(mapThrowableToWrapperFunction)
                .concatMap(RxSemaphoreTransformer.this::getLifecycleObservable)
                .map(mapWrapperToTFunction);
    }

    /**
     * Map t to Wrapper of t to be able to save t or throwable inside this wrapper and make the
     * stream of only one type (Wrapper<T>)
     */
    private Function<T, Wrapper<T>> mapTToWrapperFunction = t -> new Wrapper(t);

    /**
     * Map throwable to Wrapper of to store throwable inside this wrapper and make the stream of
     * only one type (Wrapper<T>)
     */
    private Function<Throwable, Wrapper<T>> mapThrowableToWrapperFunction = throwable -> new Wrapper(throwable);

    /**
     * @param tWrapper
     * @return observable that will emit once when lifecycle become active then make this active
     * signal to stream type (Wrapper<T>)
     */
    private Observable<Wrapper<T>> getLifecycleObservable(Wrapper<T> tWrapper) {
        return lifecycleBehaviorSubject
                .filter(fragmentEvent -> fragmentEvent == Signal.ACTIVE)
                .take(1)
                .map(integer -> tWrapper);
    }

    /**
     * Map the Wrapper<T> to the real stream type (T) or throw the saved exception that saved until
     * lifecycle emit active signal.
     */
    private Function<Wrapper<T>, T> mapWrapperToTFunction = wrapper -> {
        if (wrapper.throwable != null)
            throw (Exception) wrapper.throwable;
        return wrapper.t;
    };

    private class Wrapper<T> {
        T t;
        Throwable throwable;

        Wrapper(T t) {
            this.t = t;
        }

        Wrapper(Throwable throwable) {
            this.throwable = throwable;
        }

        Wrapper() {

        }
    }
}