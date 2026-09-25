package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.database.daos.MatchHistoryDao
import com.example.data.database.daos.PlayerProfileDao
import com.example.data.database.daos.UserSettingsDao
import com.example.data.database.entities.MatchHistoryEntity
import com.example.data.database.entities.PlayerProfileEntity
import com.example.data.database.entities.UserSettingsEntity

@Database(
    entities = [
        PlayerProfileEntity::class,
        MatchHistoryEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ArmyXDatabase : RoomDatabase() {
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun matchHistoryDao(): MatchHistoryDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: ArmyXDatabase? = null

        fun getInstance(context: Context): ArmyXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArmyXDatabase::class.java,
                    "army_x_game_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
