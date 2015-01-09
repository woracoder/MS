/**
 * 
 */

package edu.buffalo.cse.cse486586.groupmessenger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class is used to create and upgrade the table in the SQLite Database.
 * Reference: http://www.vogella.com/tutorials/AndroidSQLite/article.html
 * 
 * @author srao2
 */
public class GroupMessengerDbHelper extends SQLiteOpenHelper implements ApplicationConstants {

    /**
     * Constructor to initialize this helper class.
     * 
     * @param context
     */
    public GroupMessengerDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Overridden method of SQLiteHelper which is used to create the GroupMessenger table.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_TABLE_CREATE);
    }

    /**
     * Overridden method of SQLiteHelper which is used to upgrade the database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(GroupMessengerDbHelper.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion + ", which will destroy all old data.\n");
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_NAME);
        onCreate(db);
    }

}
