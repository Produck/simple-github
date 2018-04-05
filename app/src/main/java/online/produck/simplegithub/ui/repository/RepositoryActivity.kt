package online.produck.simplegithub.ui.repository

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_repository.*
import online.produck.simplegithub.R
import online.produck.simplegithub.api.GithubApi
import online.produck.simplegithub.api.GithubApiProvider
import online.produck.simplegithub.api.model.GithubRepo
import online.produck.simplegithub.plusAssign
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RepositoryActivity : AppCompatActivity() {

    internal lateinit var api: GithubApi

    internal val disposable = CompositeDisposable()

    internal val dateFormatIntResponse = SimpleDateFormat(
            "yyyy-MM-dd 'T' HH:mm:ssX", Locale.getDefault()
    )

    internal val dateFormatToShow = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository)

        api = GithubApiProvider.provideGithubApi(this)

        val login = intent.getStringExtra(KEY_USER_LOGIN)
                ?: throw IllegalArgumentException("No login info exists in extras")
        val repo = intent.getStringExtra(KEY_REPO_NAME)
                ?: throw IllegalArgumentException("No repo info exists in extras")

        showRepositoryInfo(login, repo)
    }

    private fun showRepositoryInfo(login: String, repoName: String) {
        showProgress()

        disposable += (api.getRepository(login, repoName)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showProgress() }
                .doOnError { hideProgress(false) }
                .doOnComplete { hideProgress(true) }
                .subscribe ({ repo ->
                    Glide.with(this@RepositoryActivity)
                            .load(repo.owner.avatarUrl)
                            .into(ivActivityRepositoryProfile)
                    tvActivityRepositoryName.text = repo.fullName
                    tvActivityRepositoryStars.text = resources
                            .getQuantityString(R.plurals.star, repo.stars, repo.stars)
                    if (null == repo.description)
                        tvActivityRepositoryDescription.setText(R.string.no_description_provided)
                    else
                        tvActivityRepositoryDescription.text = repo.description
                    if (null == repo.language)
                        tvActivityRepositoryLanguage.setText(R.string.no_language_specified)
                    else
                        tvActivityRepositoryLanguage.text = repo.language

                    try {
                        val lastUpdate = dateFormatIntResponse.parse(repo.updatedAt)
                        tvActivityRepositoryLastUpdate.text = dateFormatToShow.format(lastUpdate)
                    } catch (e: ParseException) {
                        tvActivityRepositoryLastUpdate.text = getString(R.string.unknown)
                    }
                }) {
                    showError(it.message)
                }
        )
    }

    private fun showProgress() {
        pbActivityRepository.visibility = View.VISIBLE
        llActivityRepositoryContent.visibility = View.GONE
    }

    private fun hideProgress(isSucceed: Boolean) {
        pbActivityRepository.visibility = View.GONE
        llActivityRepositoryContent.visibility = if (isSucceed) View.VISIBLE else View.GONE
    }

    private fun showError(message: String?) {
        with(tvActivityRepositoryMessage) {
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()

        disposable.clear()
    }

    companion object {

        val KEY_USER_LOGIN = "user_login"

        val KEY_REPO_NAME = "repo_name"
    }
}
