package com.rxsemaphore.sample.base

import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    @CallSuper
    override fun onStart() {
        super.onStart()
        getPresenter().start()
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        getPresenter().stop()
    }

    abstract fun getPresenter(): BasePresenter
}