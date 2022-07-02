package com.example.drinkwaternow

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

//creating the database logic, extending the SQLiteOpenHelper base class
class WaterDatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "WaterDatabase"

        private val TABLE_DAILY_INTAKES = "DailyIntakes"
        private val TABLE_INTAKES = "Intakes"

        private val KEY_ID = "_id"
        private val KEY_DATETIME = "datetime"
        private val KEY_VOLUME = "volume"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        //creating table with fields
        val CREATE_DAILY_INTAKES_TABLE = ("CREATE TABLE " + TABLE_DAILY_INTAKES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATETIME + " TEXT,"
                + KEY_VOLUME + " TEXT" + ")")
        db?.execSQL(CREATE_DAILY_INTAKES_TABLE)

        val CREATE_INTAKES_TABLE = ("CREATE TABLE " + TABLE_INTAKES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATETIME + " TEXT,"
                + KEY_VOLUME + " TEXT" + ")")
        db?.execSQL(CREATE_INTAKES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_DAILY_INTAKES")
        onCreate(db)
    }

    /**
     * Function to insert data
     */
    fun addIntake(intake: WaterModelClass): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_DATETIME, intake.DateTime) // EmpModelClass Name
        contentValues.put(KEY_VOLUME, intake.Volume) // EmpModelClass Email

        // Inserting employee details using insert query.
        val success = db.insert(TABLE_DAILY_INTAKES, null, contentValues)
        //2nd argument is String containing nullColumnHack

        db.close() // Closing database connection
        return success
    }

    fun addSumIntake(intake: WaterModelClass): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_DATETIME, intake.DateTime) // EmpModelClass Name
        contentValues.put(KEY_VOLUME, intake.Volume) // EmpModelClass Email
        println("DB add sum:" + intake.DateTime + " " + intake.Volume)

        // Inserting employee details using insert query.
        val success = db.insert(TABLE_INTAKES, null, contentValues)
        //2nd argument is String containing nullColumnHack

        db.close() // Closing database connection
        return success
    }

    /**
     * Method to read the records from database in form of ArrayList
     */
    fun viewIntake(): ArrayList<WaterModelClass> {

        val intakeList: ArrayList<WaterModelClass> = ArrayList<WaterModelClass>()

        // Query to select all the records from the table.
        val selectQuery = "SELECT  * FROM $TABLE_DAILY_INTAKES"

        val db = this.readableDatabase
        // Cursor is used to read the record one by one. Add them to data model class.
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var dateTime: Long
        var volume: Int

        //TODO разобраться с getColumnIndex
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                dateTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DATETIME))
                volume = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOLUME))

                val intake = WaterModelClass(id = id, DateTime = dateTime, Volume = volume)
                intakeList.add(intake)

            } while (cursor.moveToNext())
        }
        return intakeList
    }

    fun viewIntakesByDay(): ArrayList<WaterModelClass> {

        val intakeList: ArrayList<WaterModelClass> = ArrayList<WaterModelClass>()

        // Query to select all the records from the table.
        val selectQuery = "SELECT  * FROM $TABLE_INTAKES"

        val db = this.readableDatabase
        // Cursor is used to read the record one by one. Add them to data model class.
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var dateTime: Long
        var volume: Int

        //TODO разобраться с getColumnIndex
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                dateTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DATETIME))
                volume = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOLUME))

                val intake = WaterModelClass(id = id, DateTime = dateTime, Volume = volume)
                intakeList.add(intake)

            } while (cursor.moveToNext())
        }
        return intakeList
    }

    /**
     * Method to update the record
     */
    fun updateIntake(intake: WaterModelClass): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_DATETIME, intake.DateTime) // EmpModelClass Name
        contentValues.put(KEY_VOLUME, intake.Volume) // EmpModelClass Email

        // Updating Row
        val success = db.update(TABLE_DAILY_INTAKES, contentValues, KEY_ID + "=" + intake.id, null)
        //2nd argument is String containing nullColumnHack

        // Closing database connection
        db.close()
        return success
    }

    /**
     * Method to delete the record
     */
    fun deleteIntake(intake: WaterModelClass): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, intake.id) // EmpModelClass id
        // Deleting Row
        val success = db.delete(TABLE_DAILY_INTAKES, KEY_ID + "=" + intake.id, null)
        //2nd argument is String containing nullColumnHack

        // Closing database connection
        db.close()
        return success
    }
}