package com.rxsemaphore.sample

import com.rxsemaphore.sample.base.BasePresenter
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class MainPresenter(val mainView: MainView): BasePresenter() {

    fun getMessage() {
        addDisposable(
            Observable.timer(5, TimeUnit.SECONDS)
                //Add Rx Semaphore transformer to your observable
                .compose(getRxSemaphoreTransformer())
            .subscribe{mainView.showToast("Hello Rx Semaphore")})
    }
}
