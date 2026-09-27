package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.FieldActivity

@Database(entities = [FieldActivity::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fieldActivityDao(): FieldActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "field_activities", "activityType", "TEXT NOT NULL DEFAULT 'Implantação Tacha'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "field_activities", "photoDuringPath", "TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "field_activities", "studType", "TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "field_activities", "plateType", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "field_activities", "plateCode", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "field_activities", "plateText", "TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "field_activities", "lane", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "field_activities", "legendDescription", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "field_activities", "eixo", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "field_activities", "cadence", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "field_activities", "observations", "TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_field_activities_timestamp` ON `field_activities` (`timestamp`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_field_activities_isSent` ON `field_activities` (`isSent`)")
            }
        }

        private fun addColumnIfNotExists(
            database: SupportSQLiteDatabase,
            tableName: String,
            columnName: String,
            columnDef: String
        ) {
            database.query("PRAGMA table_info($tableName)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                var exists = false
                while (cursor.moveToNext()) {
                    if (nameIndex >= 0 && cursor.getString(nameIndex) == columnName) {
                        exists = true
                        break
                    }
                }
                if (!exists) {
                    database.execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $columnDef")
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "field_activity_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

