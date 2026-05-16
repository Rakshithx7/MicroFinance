package com.example.microfinance.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    /**
     * Migration 1 → 2
     * Changes:
     *  1. savings_entries: add weekNumber (INTEGER DEFAULT 0) and weekYear (INTEGER DEFAULT 0)
     *  2. loans: add durationMonths (INTEGER DEFAULT 0) and dueDateMillis (INTEGER DEFAULT 0)
     *  3. Create group_settings table
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 1. Extend savings_entries
            database.execSQL(
                "ALTER TABLE savings_entries ADD COLUMN weekNumber INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE savings_entries ADD COLUMN weekYear INTEGER NOT NULL DEFAULT 0"
            )

            // 2. Extend loans
            database.execSQL(
                "ALTER TABLE loans ADD COLUMN durationMonths INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE loans ADD COLUMN dueDateMillis INTEGER NOT NULL DEFAULT 0"
            )

            // 3. Create group_settings
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS group_settings (
                    id INTEGER NOT NULL PRIMARY KEY,
                    groupName TEXT NOT NULL,
                    weeklyContribution REAL NOT NULL,
                    groupStartMillis INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }
}
