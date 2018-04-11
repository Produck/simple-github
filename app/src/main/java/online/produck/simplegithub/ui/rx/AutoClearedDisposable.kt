package online.produck.simplegithub.ui.rx

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.v7.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class AutoClearedDisposable(
        private val lifecycleOwner: AppCompatActivity, // 생명주기를 참조할 액티비티
        private val alwaysClearOnStop: Boolean = true, // onStop() 콜백 메소드가 호출되었을 때, 디스포저블 객체를 해제할지를 결정
        private val compositeDisposable: CompositeDisposable = CompositeDisposable()
) : LifecycleObserver {
    fun add(disposable: Disposable) {
        // LifecycleOwner.lifecycle 을 사용하여 참조하고 있는 컴포넌트의 Lifecycle 객체에 접근
        // Lifecycle.currentState 를 사용하여 상태 접오인 Lifecycle.State 에 접근합니다.
        // Lifecycle.State.isAtLast() 함수를 사용하여 현재 상태가 특정 상태의 이후 상태인지 여부를 반별한다.
        // 코틀린 표준 라이브러리에서 제공하는 check() 함수로 isAtLeast() 반환 값이 참인지 확인하다
        // 참이 아니면 IllegalStateException 발생

        check(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED))

        compositeDisposable.add(disposable)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun cleanUp() {
        // onStop() 콜백 함수가 호출되었을 때 무조건 디스포저블을 해제하지 않는 경우
        // 액티비티가 종료되지 않는 시점에만 디스포저블을 해제하지 않도록 합니다.
        if (!alwaysClearOnStop && !lifecycleOwner.isFinishing) {
            return
        }

        compositeDisposable.clear()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun detachSelf() {
        compositeDisposable.clear()

        lifecycleOwner.lifecycle.removeObserver(this)
    }
}