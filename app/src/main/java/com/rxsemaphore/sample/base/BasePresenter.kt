package com.rxsemaphore.sample.base

import androidx.annotation.CallSuper
import com.rxsemaphore.RxSemaphore
import com.rxsemaphore.RxSemaphoreTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BasePresenter {

    private val rxSemaphore = RxSemaphore()
    private val compositeDisposable =  CompositeDisposable()

    @CallSuper
    open fun start() {
        rxSemaphore.release()
    }

    @CallSuper
    open fun stop() {
        rxSemaphore.acquire()
    }

    @CallSuper
    open fun destroy() {
        if (!compositeDisposable.isDisposed)
            compositeDisposable.dispose()
    }

     protected fun <T> getRxSemaphoreTransformer(): RxSemaphoreTransformer<T> {
        return rxSemaphore.getRxLifecycleAwareTransformer()
    }

    protected fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }
}
