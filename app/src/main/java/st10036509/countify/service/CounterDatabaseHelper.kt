package st10036509.countify.service

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.getSystemService
import st10036509.countify.model.CounterModel

class CounterDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "counter_database"
        private const val DATABASE_VERSION = 2

        private const val TABLE_COUNTERS = "counters"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_ID = "userId"
        private const val COLUMN_COUNTER_ID = "counterId"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_CHANGE_VALUE = "changeValue"
        private const val COLUMN_START_VALUE = "startValue"
        private const val COLUMN_REPETITION = "repetition"
        private const val COLUMN_CREATED_TIMESTAMP = "createdTimestamp"
        private const val COLUMN_COUNT = "count"
        private const val COLUMN_SYNCED = "synced"
    }

    override fun onCreate(db: SQLiteDatabase) {

        val createTable = """
            CREATE TABLE $TABLE_COUNTERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID TEXT,
                $COLUMN_COUNTER_ID TEXT,
                $COLUMN_NAME TEXT,
                $COLUMN_CHANGE_VALUE INTEGER,
                $COLUMN_REPETITION TEXT,
                $COLUMN_START_VALUE INTEGER,
                $COLUMN_CREATED_TIMESTAMP INTEGER,
                $COLUMN_COUNT INTEGER,
                $COLUMN_SYNCED BOOLEAN
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COUNTERS")
        onCreate(db)
    }

    fun getCountersByUserId(userId: String): List<CounterModel> {
        val counters = mutableListOf<CounterModel>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_COUNTERS WHERE $COLUMN_USER_ID = ?"

        val cursor: Cursor = db.rawQuery(query, arrayOf(userId))

        if (cursor.moveToFirst()) {
            do {
                val counter = CounterModel(
                    counterId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    changeValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHANGE_VALUE)),
                    count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COUNT)),
                    startValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_START_VALUE)),
                    createdTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_TIMESTAMP)),
                    repetition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPETITION)),
                    userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)) == 1
                )
                counters.add(counter)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return counters
    }

    // Helper function to convert Cursor to CounterModel
    private fun cursorToCounter(cursor: Cursor): CounterModel {
        return CounterModel(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
            counterId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTER_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
            changeValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHANGE_VALUE)),
            repetition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPETITION)),
            startValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_START_VALUE)),
            createdTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_TIMESTAMP)),
            count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COUNT)),
            synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)) == 1
        )
    }

    // Insert a counter
    fun insertCounter(counter: CounterModel): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, counter.userId)
            put(COLUMN_COUNTER_ID, counter.counterId)
            put(COLUMN_NAME, counter.name)
            put(COLUMN_CHANGE_VALUE, counter.changeValue)
            put(COLUMN_START_VALUE, counter.startValue)
            put(COLUMN_REPETITION, counter.repetition)
            put(COLUMN_CREATED_TIMESTAMP, counter.createdTimestamp)
            put(COLUMN_COUNT, counter.count)
            put(COLUMN_SYNCED, if (counter.synced) 1 else 0)
        }
        return db.insert(TABLE_COUNTERS, null, values)
    }

    // Update a counter
    fun updateCounter(counter: CounterModel): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, counter.userId)
            put(COLUMN_COUNTER_ID, counter.counterId)
            put(COLUMN_NAME, counter.name)
            put(COLUMN_CHANGE_VALUE, counter.changeValue)
            put(COLUMN_START_VALUE, counter.startValue)
            put(COLUMN_REPETITION, counter.repetition)
            put(COLUMN_CREATED_TIMESTAMP, counter.createdTimestamp)
            put(COLUMN_COUNT, counter.count)
            put(COLUMN_SYNCED, if (counter.synced) 1 else 0)
        }
        val rowsAffected = db.update(TABLE_COUNTERS, values, "$COLUMN_ID = ?", arrayOf(counter.id.toString()))
        db.close()  // Close the database after update
        return rowsAffected
    }


    // Delete a counter
    fun deleteCounter(counterId: String?): Int {
        val db = writableDatabase
        return db.delete(TABLE_COUNTERS, "$COLUMN_COUNTER_ID = ?", arrayOf(counterId))
    }


    // Get all unsynced counters
    fun getUnsyncedCounters(): List<CounterModel> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COUNTERS,
            null,
            "$COLUMN_SYNCED = 0",
            null,
            null,
            null,
            null
        )
        val counters = mutableListOf<CounterModel>()
        with(cursor) {
            while (moveToNext()) {
                counters.add(cursorToCounter(this))
            }
            close()
        }
        return counters
    }

    // Clear all counters
    fun clearCounters() {
        val db = writableDatabase
        db.delete(TABLE_COUNTERS, null, null)
    }


}
