package scan.lucas.com.docscan.DAL

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import scan.lucas.com.docscan.models.DocumentModel
import java.text.SimpleDateFormat
import java.util.*

class DocumentsDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    val dataFormat = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    @Throws(SQLiteConstraintException::class)
    fun insertUser(user: DocumentModel): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(DBContract.DocumentEntry.COLUMN_PAGINAS, user.paginas)
        values.put(DBContract.DocumentEntry.COLUMN_TAMANHO, user.tamanho)
        values.put(DBContract.DocumentEntry.COLUMN_DATA_MODIFICACAO, dataFormat.format(user.data_modificacao))
        values.put(DBContract.DocumentEntry.COLUMN_AWSKEY, user.awskey)
        values.put(DBContract.DocumentEntry.COLUMN_NAME, user.name)
        values.put(DBContract.DocumentEntry.COLUMN_DOCUMENT_ID, user.docid)

        // Insert the new row, returning the primary key value of the new row
        val newRowId = db.insert(DBContract.DocumentEntry.TABLE_NAME, null, values)

        return true
    }

    @Throws(SQLiteConstraintException::class)
    fun deleteUser(userid: String): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase
        // Define 'where' part of query.
        val selection = DBContract.DocumentEntry.COLUMN_DOCUMENT_ID + " LIKE ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(userid)
        // Issue SQL statement.
        db.delete(DBContract.DocumentEntry.TABLE_NAME, selection, selectionArgs)

        return true
    }

    fun readUser(userid: String): ArrayList<DocumentModel> {
        val users = ArrayList<DocumentModel>()
        val db = writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("select " +
                    DBContract.DocumentEntry.COLUMN_DOCUMENT_ID + "," +
                    DBContract.DocumentEntry.COLUMN_AWSKEY + "," +
                    DBContract.DocumentEntry.COLUMN_PAGINAS + "," +
                    DBContract.DocumentEntry.COLUMN_DATA_MODIFICACAO + "," +
                    DBContract.DocumentEntry.COLUMN_TAMANHO + "," +
                    DBContract.DocumentEntry.COLUMN_NAME +
                    " FROM " + DBContract.DocumentEntry.TABLE_NAME +
                    " WHERE " + DBContract.DocumentEntry.COLUMN_DOCUMENT_ID + "='" + userid + "'", null)
        } catch (e: SQLiteException) {
            // if table not yet present, create it
            db.execSQL(SQL_CREATE_ENTRIES)
            return ArrayList()
        }

        var docid: String
        var name: String
        var awskey: String
        var data: Date
        var tamanho: Int
        var paginas: Int
        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                docid = cursor.getString(cursor.getColumnIndex(DBContract.DocumentEntry.COLUMN_DOCUMENT_ID))
                name = cursor.getString(cursor.getColumnIndex(DBContract.DocumentEntry.COLUMN_NAME))
                awskey = cursor.getString(cursor.getColumnIndex(DBContract.DocumentEntry.COLUMN_AWSKEY))
                val dataStr = cursor.getString(cursor.getColumnIndex(DBContract.DocumentEntry.COLUMN_DATA_MODIFICACAO))

                data = dataFormat.parse(dataStr)
                tamanho = cursor.getInt(cursor.getColumnIndex(DBContract.DocumentEntry.COLUMN_TAMANHO))
                paginas = cursor.getInt(cursor.getColumnIndex(DBContract.DocumentEntry.COLUMN_PAGINAS))

                users.add(DocumentModel(docid, awskey, name, data, tamanho, paginas))
                cursor.moveToNext()
            }
        }
        return users
    }

    fun readAllDocuments(): ArrayList<DocumentModel> {
        val users = ArrayList<DocumentModel>()
        val db = writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("select * from " + DBContract.DocumentEntry.TABLE_NAME, null)
        } catch (e: SQLiteException) {
            db.execSQL(SQL_CREATE_ENTRIES)
            return ArrayList()
        }

        var docid: String
        var name: String
        var awskey: String
        var data: Date
        var tamanho: Int
        var paginas: Int
        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                docid = cursor.getString(cursor.getColumnIndex(DBContract.DocumentEntry.COLUMN_DOCUMENT_ID))
                name = cursor.getString(cursor.getColumnIndex(DBContract.DocumentEntry.COLUMN_NAME))
                awskey = cursor.getString(cursor.getColumnIndex(DBContract.DocumentEntry.COLUMN_AWSKEY))
                val dataStr = cursor.getString(cursor.getColumnIndex(DBContract.DocumentEntry.COLUMN_DATA_MODIFICACAO))
                val f = SimpleDateFormat("yyyy-MM-dd")
                data = f.parse(dataStr)
                tamanho = cursor.getInt(cursor.getColumnIndex(DBContract.DocumentEntry.COLUMN_TAMANHO))
                paginas = cursor.getInt(cursor.getColumnIndex(DBContract.DocumentEntry.COLUMN_PAGINAS))

                users.add(DocumentModel(docid, awskey, name, data, tamanho, paginas))
                cursor.moveToNext()
            }
        }
        return users
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        val DATABASE_VERSION = 1
        val DATABASE_NAME = "DocScan.db"

        private val SQL_CREATE_ENTRIES =
                "CREATE TABLE " + DBContract.DocumentEntry.TABLE_NAME + " (" +
                        DBContract.DocumentEntry.COLUMN_DOCUMENT_ID + " TEXT PRIMARY KEY," +
                        DBContract.DocumentEntry.COLUMN_AWSKEY + " TEXT," +
                        DBContract.DocumentEntry.COLUMN_PAGINAS + " INTEGER," +
                        DBContract.DocumentEntry.COLUMN_DATA_MODIFICACAO + " TEXT," +
                        DBContract.DocumentEntry.COLUMN_TAMANHO + " INTEGER," +
                        DBContract.DocumentEntry.COLUMN_NAME + " TEXT)"

        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBContract.DocumentEntry.TABLE_NAME
    }
}
