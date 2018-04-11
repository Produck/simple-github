package online.produck.simplegithub.extensions

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import online.produck.simplegithub.rx.AutoClearedDisposable

operator fun AutoClearedDisposable.plusAssign(disposable: Disposable) = this.add(disposable)

operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    this.add(disposable)
}