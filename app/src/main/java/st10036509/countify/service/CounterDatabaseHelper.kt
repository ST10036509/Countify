package st10036509.countify.service

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.ConnectivityManager
import androidx.core.content.ContentProviderCompat.requireContext
import st10036509.countify.model.CounterModel

class CounterDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "counter_database"
        private const val DATABASE_VERSION = 1

        private const val TABLE_COUNTERS = "counters"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_ID = "userId"
        private const val COLUMN_COUNTER_ID = "counterId"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_CHANGE_VALUE = "changeValue"
        private const val COLUMN_REPETITION = "repetition"
        private const val COLUMN_CREATED_TIMESTAMP = "createdTimestamp"
        private const val COLUMN_COUNT = "count"
        private const val COLUMN_SYNCED = "synced"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_COUNTERS (
                $COLUMN_ID TEXT,
                $COLUMN_USER_ID TEXT,
                $COLUMN_COUNTER_ID TEXT,
                $COLUMN_NAME TEXT,
                $COLUMN_CHANGE_VALUE INTEGER,
                $COLUMN_REPETITION TEXT,
                $COLUMN_CREATED_TIMESTAMP INTEGER,
                $COLUMN_COUNT INTEGER,
                $COLUMN_SYNCED INTEGER
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COUNTERS")
        onCreate(db)
    }

    // Helper function to convert Cursor to CounterModel
    private fun cursorToCounter(cursor: Cursor): CounterModel {
        return CounterModel(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
            counterId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTER_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
            changeValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHANGE_VALUE)),
            repetition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPETITION)),
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
            put(COLUMN_REPETITION, counter.repetition)
            put(COLUMN_CREATED_TIMESTAMP, counter.createdTimestamp)
            put(COLUMN_COUNT, counter.count)
            put(COLUMN_SYNCED, if (counter.synced) 1 else 0)
        }
        return db.update(TABLE_COUNTERS, values, "$COLUMN_ID = ?", arrayOf(counter.id.toString()))
    }

    // Delete a counter
    fun deleteCounter(counterId: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_COUNTERS, "$COLUMN_ID = ?", arrayOf(counterId.toString()))
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
