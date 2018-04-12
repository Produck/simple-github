package online.produck.simplegithub.ui.main

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sign_in.*

import online.produck.simplegithub.R
import online.produck.simplegithub.api.model.GithubRepo
import online.produck.simplegithub.data.provideSearchHistoryDao
import online.produck.simplegithub.extensions.plusAssign
import online.produck.simplegithub.extensions.runOnIoScheduler
import online.produck.simplegithub.rx.AutoClearedDisposable
import online.produck.simplegithub.ui.repository.RepositoryActivity
import online.produck.simplegithub.ui.search.SearchActivity
import online.produck.simplegithub.ui.search.SearchAdapter
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity(), SearchAdapter.ItemClickListener {

    internal val adapter by lazy { SearchAdapter().apply { setItemClickListener(this@MainActivity) } }

    internal val searchHistoryDao by lazy { provideSearchHistoryDao(this) }

    internal val disposable = AutoClearedDisposable(this)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (R.id.menu_activity_main_clear_all == item?.itemId) {
            clearAll()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycle += disposable
        lifecycle += object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun fetch() {
                fetchSearchHistory()
            }
        }

        btnActivityMainSearch.setOnClickListener {
            startActivity<SearchActivity>()
        }

        with (rvActivityMainList) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun fetchSearchHistory() : Disposable
            = searchHistoryDao.getHistory()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                items ->
                with(adapter) {
                    setItems(items)
                    notifyDataSetChanged()
                }

                if (items.isEmpty()) {
                    showMessage(getString(R.string.no_recent_repositories))
                } else {
                    hideMessage()
                }
            }) {
                showMessage(it.message)
            }

    private fun  clearAll() {
        disposable += runOnIoScheduler { searchHistoryDao.clearAll() }
    }

    private fun showMessage(message: String?) {
        with(tvActivityMainMessage) {
            text = message ?: "Unexpected Error"
            visibility = View.VISIBLE
        }
    }

    private fun hideMessage() {
        with(tvActivityMainMessage) {
            text = ""
            visibility = View.GONE
        }
    }

    override fun onItemClick(repository: GithubRepo) {
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name
        )
    }
}
