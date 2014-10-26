/**
 * 
 */

package edu.buffalo.cse.cse486586.simpledht;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class is used to create and upgrade the table in the SQLite Database.
 * 
 * @author srao2
 */
public class SimpleDhtDbHelper extends SQLiteOpenHelper {

    /**
     * Constructor to initialize this helper class.
     * 
     * @param context
     */
    public SimpleDhtDbHelper(Context context) {
        super(context, ApplicationConstants.DB_NAME, null, ApplicationConstants.DB_VERSION);
    }

    /**
     * Overridden method of SQLiteHelper which is used to create the
     * SimpleDht table.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ApplicationConstants.DB_TABLE_CREATE);
    }

    /**
     * Overridden method of SQLiteHelper which is used to upgrade the database
     * version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SimpleDhtDbHelper.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion + ", which will destroy all old data.\n");
        db.execSQL("DROP TABLE IF EXISTS " + ApplicationConstants.DB_TABLE_NAME);
        onCreate(db);
    }

}
