#Rx Semaphore
This library help you to make th RX streams hold their emissions when you inactive and emit them when you come back
active without making you dispose the stream and resubscribe to it again when you come back active.

## Usage

```kotlin
//Get your observable
//Create instance from Rx Semaphore
val rxSemaphore = RxSemaphore()

//Get your observable
Observable.just("Hello RX Semaphore")
    //Add Rx Semaphore transformer to your observable
    .compose(rxSemaphore.getRxLifecycleAwareTransformer())
    .subscribe { Log.d("Result", it)}

//To start receiving observable emissions When you become active just release the semaphore to push the active signal
rxSemaphore.release()

//To stop receiving observable emissions When you become inactive just acquire the semaphore to push inactive signal
rxSemaphore.acquire()
```

By applying the semaphore all RX streams emissions onNext, onComplete, onError and any callback related to the emissions
won't be called util you release the semaphore.

**Note any operator you apply before applying the semaphore won't respect it.**

```kotlin
Completable
    .complete()
    .doOnComplete { Log.d("RxSemaphore", "Won't respect the semaphore") }
    .compose(rxSemaphore.getRxLifecycleAwareTransformer<Any>())
    .doOnComplete { println("Will respect the semaphore") }
    .subscribe  { println("Will respect the semaphore") }
```

## Cases RX Semaphore will handle it easier.

* In projects that apply MVP architecture pattern you should dispose all RX streams in fragments in onDestroyView
because if the we didn't dispose them, the presenter will tell the view to perform actions on view already destroyed
that will lead to null pointer exception. OnDestroyView will be called if we replaced fragment with another with adding
this transaction to back stack, So when user press back we should check if the data loaded before and if not we should
execute the stream again by resubscribe to it. So with RX Semaphore we won't have to dispose our not completed streams
in onDestroyView and resubscribe to them from scratch in onCreate view, we will just push inactive signal in onStop that
will tell the streams to hold the emissions until we push the active signal in onStart that will make the streams emits
all held emissions.

* In projects that apply MVP architecture pattern if we often dispose all streams in activities in onDestroy that
often lead to applying actions to views of activities in back stack isn't visible to the user. So with RX Semaphore we
will just push inactive signal in onStop that will tell the streams to hold the emissions until we push the active
signal in onStart that will make the streams emit all held emissions.


## Conclusion

We made our RX streams act as [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)as we can control when our streams should emit or hold the emissions until we
push active signal and release the semaphore.