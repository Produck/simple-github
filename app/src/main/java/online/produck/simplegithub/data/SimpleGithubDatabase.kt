package online.produck.simplegithub.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import online.produck.simplegithub.api.model.GithubRepo

@Database(entities = arrayOf(GithubRepo::class), version = 1)
abstract class SimpleGithubDatabase : RoomDatabase() {
    abstract  fun searchHistoryDao() : SearchHistoryDao
}