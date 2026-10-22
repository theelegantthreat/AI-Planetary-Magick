package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [JournalEntryEntity::class, PlannedActivityEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MagickDatabase : RoomDatabase() {
    abstract fun magickDao(): MagickDao

    companion object {
        @Volatile
        private var INSTANCE: MagickDatabase? = null

        fun getDatabase(context: Context): MagickDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MagickDatabase::class.java,
                    "planetary_magick.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
