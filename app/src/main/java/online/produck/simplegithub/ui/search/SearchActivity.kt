package online.produck.simplegithub.ui.search

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_search.*

import online.produck.simplegithub.R
import online.produck.simplegithub.api.GithubApi
import online.produck.simplegithub.api.GithubApiProvider
import online.produck.simplegithub.api.model.GithubRepo
import online.produck.simplegithub.api.model.RepoSearchResponse
import online.produck.simplegithub.plusAssign
import online.produck.simplegithub.ui.repository.RepositoryActivity
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity(), SearchAdapter.ItemClickListener {

    internal lateinit var menuSearch: MenuItem

    internal lateinit var searchView: SearchView

    internal lateinit var adapter: SearchAdapter

    internal lateinit var api: GithubApi

    internal val disposable = CompositeDisposable()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)
        menuSearch = menu.findItem(R.id.menu_activity_search_query)

        searchView = menuSearch.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                updateTile(query)
                hideSoftKeyboard()
                collapseSearchView()
                searchRepository(query)

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        adapter = SearchAdapter()
        adapter.setItemClickListener(this)
        rvActivitySearchList.layoutManager = LinearLayoutManager(this)
        rvActivitySearchList.adapter = adapter

        api = GithubApiProvider.provideGithubApi(this)
    }

    private fun searchRepository(query: String) {
        clearResults()
        hideError()
        showProgress()

        disposable += (api.searchRepository(query)
                .flatMap {
                    if (0 == it.totalCount) {
                        Observable.error(IllegalStateException("No search result"))
                    } else {
                        Observable.just(it.items)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    clearResults()
                    hideError()
                    showProgress()
                }
                .doOnTerminate {
                    hideProgress()
                }
                .subscribe({ items ->
                    with (adapter) {
                        setItems(items)
                        notifyDataSetChanged()
                    }
                }) {
                    showError(it.message)
                })
    }

    private fun showProgress() {
        pbActivitySearch.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        pbActivitySearch.visibility = View.GONE
    }

    private fun showError(message: String?) {
        tvActivitySearchMessage.text = message ?: "Unexpected Error"
        tvActivitySearchMessage.visibility = View.VISIBLE
    }

    private fun hideError() {
        tvActivitySearchMessage.text = ""
        tvActivitySearchMessage.visibility = View.GONE
    }

    private fun clearResults() {
        adapter.clearItems()
        adapter.notifyDataSetChanged()
    }

    private fun collapseSearchView() {
        menuSearch.collapseActionView()
    }

    private fun hideSoftKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(searchView.windowToken, 0)
    }

    private fun updateTile(query: String) {
        val actionBar = supportActionBar
        if (null != actionBar) {
            actionBar.subtitle = query
        }
    }

    override fun onItemClick(repository: GithubRepo) {
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name
        )
    }

    override fun onStop() {
        super.onStop()

        disposable.clear()
    }
}
