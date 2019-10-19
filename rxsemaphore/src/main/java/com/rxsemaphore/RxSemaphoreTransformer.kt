package com.rxsemaphore


import io.reactivex.*
import io.reactivex.functions.Function
import io.reactivex.subjects.BehaviorSubject
import org.reactivestreams.Publisher

/**
 * @param <T> A transformer that make your RX stream doesn't emit or throw exception until the
 * semaphore behaviour subject emit active signal.
</T> */
class RxSemaphoreTransformer<T>(private val semaphoreBehaviorSubject: BehaviorSubject<Signal>) :
    FlowableTransformer<T, T>, ObservableTransformer<T, T>, CompletableTransformer,
    SingleTransformer<T, T>, MaybeTransformer<T, T> {

    /**
     * Map T to Wrapper of T to be able to save T or any throwable inside this wrapper and make the
     * stream of only one type (Wrapper<T>)
    </T> */
    private val mapTToWrapperFunction = Function<T, Wrapper<T>> { t -> Wrapper(t) }

    /**
     * Map throwable to Wrapper of T that stores the throwable to make the stream of only one type (Wrapper<T>)
    </T> */
    private val mapThrowableToWrapperFunction =
        Function<Throwable, Wrapper<T>> { throwable -> Wrapper(throwable = throwable) }

    /**
     * Map the Wrapper<T> to the real stream type (T) or throw the saved exception that saved until
     * semaphore emit active signal.
    </T> */
    private val mapWrapperToTFunction = Function<Wrapper<T>, T>{ wrapper ->
        if (wrapper.throwable != null)
            throw wrapper.throwable
        wrapper.t
    }

    /**
     * @param upstream
     * @return Do the same logic like [RxSemaphoreTransformer.applyLogic] but we
     * can't call this function because by converting the flowable to observable and passing it to
     * this function we will lose the back pressure strategy and can't return it back when we convert
     * the returned observable from apply logic function to flowable because there is no function that
     * get the back pressure strategy of original flowable.
     */
    override fun apply(upstream: Flowable<T>): Publisher<T> {
        return upstream
            .map<Wrapper<T>>(mapTToWrapperFunction)
            .onErrorReturn(mapThrowableToWrapperFunction)
            .concatMap { tWrapper ->
                getRxSemaphoreObservable(tWrapper)
                    .toFlowable(BackpressureStrategy.LATEST)
            }
            .map(mapWrapperToTFunction)
    }

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return applyLogic(upstream)
    }

    override fun apply(upstream: Completable): CompletableSource {
        return applyLogic(upstream.andThen(Observable.just("")) as Observable<T>)
            .ignoreElements()
    }

    override fun apply(upstream: Single<T>): SingleSource<T> {
        return applyLogic(upstream.toObservable())
            .elementAtOrError(0)
    }

    /**
     * @param upstream
     * @return Do the same logic like [RxSemaphoreTransformer.applyLogic] except
     * few things. We need to handle if the upstream (maybe) emit zero item then we need to make it
     * emit empty wrapper instead of complete the stream and after we receive the active signal
     * we will ignore any wrapper that contains empty T and Throwable and complete the stream.
     */
    override fun apply(upstream: Maybe<T>): MaybeSource<T> {
        return upstream
            .map<Wrapper<T>>(mapTToWrapperFunction)
            .switchIfEmpty(Maybe.just(Wrapper()))
            .onErrorReturn(mapThrowableToWrapperFunction)
            .concatMap { tWrapper -> getRxSemaphoreObservable(tWrapper).firstElement() }
            .filter { tWrapper -> tWrapper.t != null || tWrapper.throwable != null }
            .map(mapWrapperToTFunction)
    }

    /**
     * @param observable
     * @return observable that:
     *
     *
     * 1- converts stream type from T to Wrapper<T> with
     * [RxSemaphoreTransformer.mapTToWrapperFunction].
    </T> *
     *
     * 2- Fetch any throwable before thrown and save it inside a wrapper<T> with
     * [RxSemaphoreTransformer.mapThrowableToWrapperFunction].
    </T> *
     *
     * 3- Convert any item emitted by the main observable to observable that will emit the same event
     * when the semaphore become active with [RxSemaphoreTransformer.getRxSemaphoreObservable]
     * => We used concat map instead of flat map because concat map preserve the order of items.
     *
     *
     * 4- Return back the stream type from Wrapper<T> to T which is the main type or throw the
     * saved throwable if found with [RxSemaphoreTransformer.mapWrapperToTFunction]
    </T> */
    private fun applyLogic(observable: Observable<T>): Observable<T> {
        return observable
            .map<Wrapper<T>>(mapTToWrapperFunction)
            .onErrorReturn(mapThrowableToWrapperFunction)
            .concatMap { this@RxSemaphoreTransformer.getRxSemaphoreObservable(it) }
            .map(mapWrapperToTFunction)
    }

    /**
     * @param tWrapper
     * @return observable that will emit once when semaphore become active then map this active
     * signal to stream type (Wrapper<T>)
    </T> */
    private fun getRxSemaphoreObservable(tWrapper: Wrapper<T>): Observable<Wrapper<T>> {
        return semaphoreBehaviorSubject
            .filter { signal -> signal == Signal.ACTIVE }
            .take(1)
            .map { signal -> tWrapper }
    }

    private inner class Wrapper<T>(
        val t: T? = null,
        val throwable: Throwable? = null
    )
}