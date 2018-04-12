package online.produck.simplegithub.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import online.produck.simplegithub.api.model.GithubRepo

@Dao
interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(repo: GithubRepo)

    @Query("SELECT * FROM repositories")
    fun getHistory() : Flowable<List<GithubRepo>>

    @Query("DELETE FROM repositories")
    fun clearAll()
}