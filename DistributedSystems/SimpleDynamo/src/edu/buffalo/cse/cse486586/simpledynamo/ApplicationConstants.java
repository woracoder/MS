/**
 * 
 */

package edu.buffalo.cse.cse486586.simpledynamo;

/**
 * This class stores all the constants for this application.
 * 
 * @author srao2
 */
public interface ApplicationConstants {

    // Holds the name of the database.
    static String DB_NAME = "simpleDynamoTable.db";

    // Holds the version number of the database
    static int DB_VERSION = 1;

    // Holds the name of the table to be created in the database.
    static String DB_TABLE_NAME = "SimpleDynamo";

    // Column name 1 of the table.
    static String COLUMN_KEY = "key";

    // Column name 2 of the table.
    static String COLUMN_VALUE = "value";

    // Holds the first part of the content provider string
    static String CONTENT = "content";

    // Holds the second part of the content provider identifier string
    static String PROVIDER = "edu.buffalo.cse.cse486586.simpledynamo.provider";

    // Create table script
    static String DB_TABLE_CREATE = "create table " + DB_TABLE_NAME + "(" + COLUMN_KEY
            + " text not null, " + COLUMN_VALUE + " text not null);";

    // Get the name of the class
    static String TAG = SimpleDynamoActivity.class.getSimpleName();

    // Emulator number for AVD 0
    static String EMU_AVD0 = "5554";

    // Emulator number for AVD 1
    static String EMU_AVD1 = "5556";

    // Emulator number for AVD 2
    static String EMU_AVD2 = "5558";

    // Emulator number for AVD 3
    static String EMU_AVD3 = "5560";

    // Emulator number for AVD 4
    static String EMU_AVD4 = "5562";

    // Port number for AVD 0
    static String PORT_AVD0 = "11108";

    // Port number for AVD 1
    static String PORT_AVD1 = "11112";

    // Port number for AVD 2
    static String PORT_AVD2 = "11116";

    // Port number for AVD 3
    static String PORT_AVD3 = "11120";

    // Port number for AVD 4
    static String PORT_AVD4 = "11124";

    // Port for listening
    static int SERVER_PORT = 10000;

    // Represents a blank string
    static String BLANK_STRING = "";

    // Represents a space string
    static String SPACE_STRING = " ";

    // Represents message type of insert for a host
    static String MESSAGETYPE_INSERT = "insert";

    // Represents message type of insert success for a remote host
    static String MESSAGETYPE_INSERTSUCCESS = "insertsuccess";

    // Represents message type of delete for all nodes
    static String MESSAGETYPE_DELETESTAR = "deletestar";

    // Represents message type of delete
    static String MESSAGETYPE_DELETE = "delete";

    // Represents message type of delete for all nodes
    static String MESSAGETYPE_QUERYSTAR = "querystar";

    // Represents message type of delete
    static String MESSAGETYPE_QUERY = "query";

    // Represents message type of query results
    static String MESSAGETYPE_QUERYRESULT = "queryresult";

    // Represents message type of query results
    static String MESSAGETYPE_QUERYSTARRESULTS = "querystarresults";

    // Represents the @ symbol
    static String SYMBOL_AT = "@";

    // Represents the * symbol
    static String SYMBOL_STAR = "*";

    // Represents the 1 symbol
    static String NUMBER_ONE = "1";

    // Represents the ~ symbol
    static String TILDE = "~";

    // Represents the | symbol
    static String PIPE = "|";

    // Represents the # symbol
    static String HASH = "#";

    // Represents the | symbol to be used in regex
    static String REGEX_PIPE = "\\|";

}
