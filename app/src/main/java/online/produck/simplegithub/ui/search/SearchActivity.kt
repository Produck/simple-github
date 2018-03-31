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

import online.produck.simplegithub.R
import online.produck.simplegithub.api.GithubApi
import online.produck.simplegithub.api.GithubApiProvider
import online.produck.simplegithub.api.model.GithubRepo
import online.produck.simplegithub.api.model.RepoSearchResponse
import online.produck.simplegithub.ui.repository.RepositoryActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity(), SearchAdapter.ItemClickListener {

    internal lateinit var rvList: RecyclerView

    internal lateinit var progress: ProgressBar

    internal lateinit var tvMessage: TextView

    internal lateinit var menuSearch: MenuItem

    internal lateinit var searchView: SearchView

    internal lateinit var adapter: SearchAdapter

    internal lateinit var api: GithubApi

    internal lateinit var searchCall: Call<RepoSearchResponse>

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

        rvList = findViewById(R.id.rvActivitySearchList)
        progress = findViewById(R.id.pbActivitySearch)
        tvMessage = findViewById(R.id.tvActivitySearchMessage)

        adapter = SearchAdapter()
        adapter.setItemClickListener(this)
        rvList.layoutManager = LinearLayoutManager(this)
        rvList.adapter = adapter

        api = GithubApiProvider.provideGithubApi(this)
    }

    private fun searchRepository(query: String) {
        clearResults()
        hideError()
        showProgress()

        searchCall = api.searchRepository(query)

        searchCall.enqueue(object : Callback<RepoSearchResponse> {
            override fun onResponse(call: Call<RepoSearchResponse>, response: Response<RepoSearchResponse>) {
                hideProgress()

                val searchResult = response.body()
                if (response.isSuccessful && null != searchResult) {
                    adapter.setItems(searchResult.items)
                    adapter.notifyDataSetChanged()

                    if (0 == searchResult.totalCount) {
                        showError(getString(R.string.no_search_result))
                    }
                } else {
                    showError("Not successful: " + response.message())
                }
            }

            override fun onFailure(call: Call<RepoSearchResponse>, t: Throwable) {
                hideProgress()
                showError(t.message)
            }
        })
    }

    private fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress.visibility = View.GONE
    }

    private fun showError(message: String?) {
        tvMessage.text = message ?: "Unexpected Error"
        tvMessage.visibility = View.VISIBLE
    }

    private fun hideError() {
        tvMessage.text = ""
        tvMessage.visibility = View.GONE
    }

    private fun clearResults() {
        adapter.clearItems()
        adapter.notifyDataSetChanged()
    }

    private fun collapseSearchView() {
        menuSearch.collapseActionView()
    }

    private fun hideSoftKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(searchView.windowToken, 0)
    }

    private fun updateTile(query: String) {
        val actionBar = supportActionBar
        if (null != actionBar) {
            actionBar.subtitle = query
        }
    }

    override fun onItemClick(repository: GithubRepo) {
        val intent = Intent(this, RepositoryActivity::class.java)
        intent.putExtra(RepositoryActivity.KEY_USER_LOGIN, repository.owner.login)
        intent.putExtra(RepositoryActivity.KEY_REPO_NAME, repository.name)
        startActivity(intent)
    }
}
