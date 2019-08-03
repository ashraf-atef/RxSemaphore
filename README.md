# Rx Semaphore

This library helps you to make the RX streams hold their emissions when you are inactive and emit them when you come back
active without making you dispose the stream and resubscribe to it again when you come back active.

## Usage

```kotlin
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

* In projects that applies MVP architecture pattern you should dispose all RX streams in fragments in onDestroyView
because if the you didn't dispose them, the presenter will tell the view to perform actions on view already destroyed
that will lead to null pointer exception. OnDestroyView will be called if we replaced fragment with another with adding
this transaction to the back stack. So when the user presses back we should check if the data loaded before and if not we should
execute the stream again by resubscribe to it. So with RX Semaphore we won't have to dispose our not completed streams
in onDestroyView and resubscribe to them from scratch in onCreateView, we will just push inactive signal in onStop that
will tell the streams to hold the emissions until we push the active signal in onStart that will make the streams emits
all held emissions.

* In projects that applies MVP architecture pattern we often dispose all streams in activities in onDestroy that lead to
applying actions on views of activities that are in the back stack which isn't visible to the user. So with RX Semaphore we
will just push inactive signal in onStop that will tell the streams to hold the emissions until we push the active
signal in onStart that will make the streams emit all held emissions.


## Conclusion

We made our RX streams act as [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) so we can control when our streams should emit or hold the emissions until we
push active signal and release the semaphore.

## Philosophy

* The logic of semaphore based on a transformer you add with compose operator which contains a behaviour subject(Semaphore)
that hold the value of the signal (active/inactive).

* With every item emitted by the original observable we concat map it to observable will emit the same item but after the
semaphore behaviour subject emit the active signal using concatMap operator which is like flat map but it preserve the
order of the emitted items.

* You maybe ask yourself what will we do If the original source throw a throwable. How can we hold it until the semaphore
emit the active signal and prevent the observer error consumer receive it immediately. We convert the throwble to a
wrapper contains the thrown throwable using onErrorReturn operator.

* Another thing we need to handle is to convert any item emitted by the source observable to the same
that contains the emitted value as generic (T). So our stream type will be Wrapper<T> which contains a T(emitted item)
and a Throwable.

* After the observable of the semaphore emit the active signal which mapped to the same Wrapper, we map it again to a T or
rethrow the thrown throwable if it's not null inside the wrapper.

```kotlin
SourceObservable
        /**
         * Map throwable to Wrapper of T that stores the throwable to make the stream of only one type (Wrapper<T>)
         */
        .map(mapTToWrapperFunction)
        /**
         * Map throwable to Wrapper of T that stores the throwable to make the stream of only one type (Wrapper<T>)
         */
        .onErrorReturn(mapThrowableToWrapperFunction)
        /**
         * Concat map the previous wrapper to Observable that will emit once when semaphore become active then
         * map this active signal to the stream type (Wrapper<T>)
         */
        .concatMap(RxSemaphoreTransformer.this::getRxSemaphoreObservable)
        /**
         * Map the Wrapper<T> to the real stream type (T) or throw the saved exception that saved until
         * semaphore emit active signal.
         */
        .map(mapWrapperToTFunction)
```

## Installation

[![](https://jitpack.io/v/ashraf-atef/RxSemaphore.svg)](https://jitpack.io/#ashraf-atef/RxSemaphore)

```gradle
implementation 'com.github.ashraf-atef:RxSemaphore:vX.X'
```

## License

### MIT License

Copyright (c) 2019 ashraf-atef

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
