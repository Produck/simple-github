package online.produck.simplegithub.api

import io.reactivex.Observable
import online.produck.simplegithub.api.model.GithubRepo
import online.produck.simplegithub.api.model.RepoSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApi {

    @GET("search/repositories")
    fun searchRepository(@Query("q") query: String): Observable<RepoSearchResponse>

    @GET("repos/{owner}/{name}")
    fun getRepository(@Path("owner") ownerLogin: String, @Path("name") repoName: String): Observable<GithubRepo>
}
