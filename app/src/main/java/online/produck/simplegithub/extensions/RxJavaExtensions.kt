package online.produck.simplegithub.extensions

import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import online.produck.simplegithub.rx.AutoClearedDisposable

operator fun AutoClearedDisposable.plusAssign(disposable: Disposable) = this.add(disposable)

operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    this.add(disposable)
}

fun runOnIoScheduler(func: () -> Unit): Disposable
    = Completable.fromCallable(func)
        .subscribeOn(Schedulers.io())
        .subscribe()