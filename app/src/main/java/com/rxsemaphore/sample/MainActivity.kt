package com.rxsemaphore.sample

import android.os.Bundle
import android.widget.Toast
import com.rxsemaphore.sample.base.BaseActivity
import com.rxsemaphore.sample.base.BasePresenter

class MainActivity : BaseActivity(), MainView {

    lateinit var mainPresenter : MainPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainPresenter = MainPresenter(this)
        mainPresenter.getMessage()
    }

    override fun getPresenter(): BasePresenter = mainPresenter

    override fun showToast(message: String) = Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
}
