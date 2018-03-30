package online.produck.simplegithub.api;

import online.produck.simplegithub.api.model.GithubRepo;
import online.produck.simplegithub.api.model.RepoSearchResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GithubApi {

    @GET("search/repositories")
    Call<RepoSearchResponse> searchRepository(@Query("q") String query);

    @GET("repos/{owner}/{name}")
    Call<GithubRepo> getRepository(@Path("owner") String ownerLogin, @Path("name") String repoName);
}
