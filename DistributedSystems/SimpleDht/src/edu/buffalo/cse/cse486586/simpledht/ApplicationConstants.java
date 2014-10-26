/**
 * 
 */

package edu.buffalo.cse.cse486586.simpledht;

/**
 * This class stores all the constants for this application.
 * 
 * @author srao2
 */
public class ApplicationConstants {

    // Holds the name of the database.
    static String DB_NAME = "simpleDhtTable.db";

    // Holds the version number of the database
    static int DB_VERSION = 1;

    // Holds the name of the table to be created in the database.
    static String DB_TABLE_NAME = "SimpleDht";

    // Column name 1 of the table.
    static String COLUMN_KEY = "key";

    // Column name 2 of the table.
    static String COLUMN_VALUE = "value";

    // Holds the first part of the content provider string
    static String CONTENT = "content";

    // Holds the second part of the content provider identifier string
    static String PROVIDER = "edu.buffalo.cse.cse486586.simpledht.provider";

    // Create table script
    static String DB_TABLE_CREATE = "create table " + DB_TABLE_NAME + "(" + COLUMN_KEY
            + " text not null, " + COLUMN_VALUE + " text not null);";

    // Get the name of the class
    static String TAG = SimpleDhtActivity.class.getSimpleName();

    // Port for AVD 0
    static String REMOTE_PORT0 = "11108";

    // Port for listening
    static int SERVER_PORT = 10000;

    // Represents a blank string
    static String BLANK_STRING = "";

    // Represents a space string
    static String SPACE_STRING = " ";

    // Represents message type of join
    static String MESSAGETYPE_JOIN = "join";

    // Represents message type of join propagate
    static String MESSAGETYPE_JOINPROPAGATE = "joinpropagate";

    // Represents message type of join response
    static String MESSAGETYPE_JOINRESPONSE = "joinresponse";

    // Represents message type of join update for predecessor
    static String MESSAGETYPE_JOINUPDATEPRED = "joinupdatepred";

    // Represents message type of join update for successor
    static String MESSAGETYPE_JOINUPDATESUC = "joinupdatesuc";

    // Represents message type of insert for successor or predecessor
    static String MESSAGETYPE_INSERT = "insert";

    // Represents message type of insert for successor or predecessor
    static String MESSAGETYPE_INSERTPROPAGATE = "insertpropagate";

    // Represents message type of delete for all nodes
    static String MESSAGETYPE_DELETESTAR = "deletestar";

    // Represents message type of delete
    static String MESSAGETYPE_DELETE = "delete";

    // Represents message type of insert for successor or predecessor
    static String MESSAGETYPE_DELETEPROPAGATE = "deletepropagate";

    // Represents message type of delete for all nodes
    static String MESSAGETYPE_QUERYSTAR = "querystar";

    // Represents message type of delete
    static String MESSAGETYPE_QUERY = "query";

    // Represents message type of insert for successor or predecessor
    static String MESSAGETYPE_QUERYPROPAGATE = "querypropagate";

    // Represents message type of query results
    static String MESSAGETYPE_QUERYRESULTS = "queryresults";

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
