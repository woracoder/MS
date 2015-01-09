/**
 * 
 */

package edu.buffalo.cse.cse486586.groupmessenger;

/**
 * This class stores all the constants for this application.
 * 
 * @author srao2
 */
public interface ApplicationConstants {

    // Holds the name of the database.
    String DB_NAME = "groupMessengerTable.db";

    // Holds the version number of the database
    int DB_VERSION = 1;

    // Holds the name of the table to be created in the database.
    String DB_TABLE_NAME = "GroupMessenger";

    // Column name 1 of the table.
    String COLUMN_KEY = "key";

    // Column name 2 of the table.
    String COLUMN_VALUE = "value";

    // Create table script
    String DB_TABLE_CREATE = "create table " + DB_TABLE_NAME + "(" + COLUMN_KEY
            + " text not null, " + COLUMN_VALUE + " text not null);";

    // Get the name of the class
    String TAG = GroupMessengerActivity.class.getSimpleName();

    // Port for AVD 0
    String REMOTE_PORT0 = "11108";

    // Port for AVD 1
    String REMOTE_PORT1 = "11112";

    // Port for AVD 2
    String REMOTE_PORT2 = "11116";

    // Port for AVD 3
    String REMOTE_PORT3 = "11120";

    // Port for AVD 4
    String REMOTE_PORT4 = "11124";

    // Port for listening
    int SERVER_PORT = 10000;

    // Represents a blank string
    String BLANK_STRING = "";

    // Represents a new line string
    String NEWLINE_STRING = "\n";

    // Represents a tab string
    String TAB_STRING = "\t";
    
 // Represents a true value string
    String TRUE_STRING = "true";
}
