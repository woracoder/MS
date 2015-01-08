
package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class acts as a simple dynamo provider and provides availability and
 * failure handling
 * 
 * @author sudarshan
 */
public class SimpleDynamoProvider extends ContentProvider implements ApplicationConstants {

    // Variable used as a helper object to access the SQLite database
    private SimpleDynamoDbHelper mDbHelper;

    // Variable used to store the local URI for the database
    private Uri mLocalUri;

    // Variable used to store emulator number of host
    private String portStr;

    // Variable used to store port number of host
    private String myPort;

    // TreeMap used store nodeId and node emulator number in sorted order
    private Map<String, String> mNodeIdNodePortMap;

    // Map used store node and node emulator number in sorted order
    private Map<String, String> mNodePortNodeIdMap;

    // HashMap used store nodeId and its two successor nodes
    private Map<String, String[]> mNodeSuccessorMap;

    // Variable used to store the results of query for a key
    private Map<String, String> globalCursorMap;

    // Variable used to keep count of inserts done
    int mInsertResponses;

    // Variable used to wait for query
    boolean isInsertDone;

    // Variable used for query guarded region
    AtomicBoolean isInsertGuarded;

    // Variable used to keep count of query results returned
    int mQueryResponses;

    // Variable used to wait for query
    boolean isQueryDone;

    // Variable used for query guarded region
    AtomicBoolean isQueryGuarded;

    // Variable to verify if insert response is for same key
    String mInsertKey;

    // Variable to verify if query response is for same key
    String mQueryKey;

    // Variable to determine if application is starting or recovering from crash
    boolean isStartMode;

    /**
     * Method called by the android framework when the application is started
     */
    @Override
    public boolean onCreate() {

        // Initialize start mode to true by default
        isStartMode = false;

        // If the database does not exist implies application is starting for
        // the first time
        if (!checkIfDbExists()) {
            isStartMode = true;
        }

        // Initialize insert response count to 0
        mInsertResponses = 0;

        // Initialize isInsertDone to false
        isInsertDone = false;

        // Initialize query response count to 0
        mQueryResponses = 0;

        // Initialize isQueryDone to false
        isQueryDone = false;

        // Initialize the atomic boolean for guarding insert block
        isInsertGuarded = new AtomicBoolean();

        // Initialize the atomic boolean for guarding insert block
        isQueryGuarded = new AtomicBoolean();

        // Initialize the SQLite database helper
        mDbHelper = new SimpleDynamoDbHelper(getContext());

        // Build the URI for operations on the SQLite database
        mLocalUri = buildUri(CONTENT, PROVIDER);

        // Initialize the node id vs node port treemap
        mNodeIdNodePortMap = new TreeMap<String, String>();

        // Initialize the node port vs node Id map
        mNodePortNodeIdMap = new HashMap<String, String>();

        // Initialize the global cursor map
        globalCursorMap = new HashMap<String, String>();

        // Initialize the emulator port number map
        mNodeSuccessorMap = new HashMap<String, String[]>();

        // Get the emulator and virtual router port
        TelephonyManager tel = (TelephonyManager)this.getContext().getSystemService(
                Context.TELEPHONY_SERVICE);
        portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);

        // Initialize my listening port
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        // Calculate the Node ID for all AVDs
        try {
            mNodeIdNodePortMap.put(genHash(EMU_AVD0), PORT_AVD0);
            mNodeIdNodePortMap.put(genHash(EMU_AVD1), PORT_AVD1);
            mNodeIdNodePortMap.put(genHash(EMU_AVD2), PORT_AVD2);
            mNodeIdNodePortMap.put(genHash(EMU_AVD3), PORT_AVD3);
            mNodeIdNodePortMap.put(genHash(EMU_AVD4), PORT_AVD4);

            mNodePortNodeIdMap.put(PORT_AVD0, genHash(EMU_AVD0));
            mNodePortNodeIdMap.put(PORT_AVD1, genHash(EMU_AVD1));
            mNodePortNodeIdMap.put(PORT_AVD2, genHash(EMU_AVD2));
            mNodePortNodeIdMap.put(PORT_AVD3, genHash(EMU_AVD3));
            mNodePortNodeIdMap.put(PORT_AVD4, genHash(EMU_AVD4));

        } catch (NoSuchAlgorithmException e1) {
            Log.e(TAG, "Cannot generate hash value.");
        }

        // Populate the emulator port number map
        mNodeSuccessorMap.put(PORT_AVD0, new String[] {
                PORT_AVD2, PORT_AVD3
        });
        mNodeSuccessorMap.put(PORT_AVD1, new String[] {
                PORT_AVD0, PORT_AVD2
        });
        mNodeSuccessorMap.put(PORT_AVD2, new String[] {
                PORT_AVD3, PORT_AVD4
        });
        mNodeSuccessorMap.put(PORT_AVD3, new String[] {
                PORT_AVD4, PORT_AVD1
        });
        mNodeSuccessorMap.put(PORT_AVD4, new String[] {
                PORT_AVD1, PORT_AVD0
        });

        // Listen on port 10000 for incoming requests
        try {
            ServerSocket mServerSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mServerSocket);
        } catch (IOException e) {
            Log.e(TAG, "Cannot create a ServerSocket");
        }

        // If not in start mode
        if (!isStartMode) {

            // Log.e(TAG, "Node " + myPort + " recovering.");

            // Clear your local content provider
            // deleteFromLocalContentProvider(SYMBOL_AT);

            // Query * to get all data from all hosts
            Cursor mCs = query(mLocalUri, null, SYMBOL_STAR, null, null);

            // Now filter the data from the cursor and insert it into local
            // content provider
            filterAndInsertIntoLocalContentProvider(mCs);
        } else {
            // Log.e(TAG, "Node " + myPort + " starting.");
        }

        return false;
    }

    /**
     * Server task class that continuously listens for connections from other
     * systems
     * 
     * @author sudarshan
     */
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {

            ServerSocket serverSocket = sockets[0];

            try {

                // Continuously listen for any messages from other hosts
                while (null != serverSocket) {

                    // Block till a new request arrives
                    Socket mSoc = serverSocket.accept();

                    // Get the newly arrived message
                    BufferedReader mbr = new BufferedReader(new InputStreamReader(
                            mSoc.getInputStream()));

                    // If not null split the messages
                    String msg = null;
                    if (null == (msg = mbr.readLine())) {
                        Log.e(TAG, "Null received from Buffered reader");
                        return null;
                    }

                    String[] msgs = msg.split(REGEX_PIPE);

                    // Create a new message object
                    SimpleDynamoMessage mSdm = new SimpleDynamoMessage(msgs[0], msgs[1], msgs[2],
                            msgs[3]);

                    if (null != mSdm) {

                        // If it is a delete star message
                        if (mSdm.getMessageType().equalsIgnoreCase(MESSAGETYPE_DELETESTAR)) {

                            // Delete everything from the local content provider
                            deleteFromLocalContentProvider(SYMBOL_AT);
                        }

                        // If it is a normal delete message
                        else if (mSdm.getMessageType().equalsIgnoreCase(MESSAGETYPE_DELETE)) {

                            // Delete from local content provider
                            deleteFromLocalContentProvider(mSdm.getmKey());
                        }

                        // If message type is insert
                        else if (mSdm.getMessageType().equalsIgnoreCase(MESSAGETYPE_INSERT)) {

                            // Log.e(TAG, "Received insert for " +
                            // mSdm.getmKey() + " on " + myPort
                            // + " from " + mSdm.getmOriginPort());

                            // Insert in local content provider
                            localInsert(mSdm.getmKey(), mSdm.getmValue());

                            // Send feedback message to originator
                            sendMessage(mSdm.getmOriginPort(), MESSAGETYPE_INSERTSUCCESS, myPort,
                                    mSdm.getmKey(), SPACE_STRING);

                        }

                        // If message type is insert success
                        else if (mSdm.getMessageType().equalsIgnoreCase(MESSAGETYPE_INSERTSUCCESS)) {

                            // Log.e(TAG,
                            // "Received insert success on " + myPort + " from "
                            // + mSdm.getmOriginPort() + " for key " +
                            // mSdm.getmKey());

                            // Check if response is for same key else ignore
                            if (mSdm.getmKey().equals(mInsertKey)) {
                                // Check if main thread waiting for response
                                if (isInsertGuarded.get()) {
                                    // If responses < 2 && insert incomplete as
                                    // we get at least 2 responses
                                    if (mInsertResponses < 2 && !isInsertDone) {
                                        // Increment insert response count
                                        mInsertResponses++;
                                        // If 2 responses then toggle flag
                                        if (mInsertResponses == 2) {
                                            isInsertDone = true;
                                        }
                                    }
                                }
                            }

                        }

                        // If message type is query
                        else if (mSdm.getMessageType().equalsIgnoreCase(MESSAGETYPE_QUERY)) {

                            // Log.e(TAG, "Received query for " + mSdm.getmKey()
                            // + " on " + myPort
                            // + " from " + mSdm.getmOriginPort());

                            // Query my local content provider
                            Cursor mCursor = queryLocalContentProvider(mLocalUri, null,
                                    mSdm.getmKey(), null, null);

                            // Reply back to the originator with the results
                            sendMessage(mSdm.getmOriginPort(), MESSAGETYPE_QUERYRESULT, myPort,
                                    mSdm.getmKey(), cursorToString(mCursor));
                        }

                        // If message type is query *
                        else if (mSdm.getMessageType().equalsIgnoreCase(MESSAGETYPE_QUERYSTAR)) {

                            // Log.e(TAG,
                            // "Received query * on " + myPort + " from "
                            // + mSdm.getmOriginPort());

                            // Query my local content provider
                            Cursor mCursor = queryLocalContentProvider(mLocalUri, null, SYMBOL_AT,
                                    null, null);

                            // Reply back to the originator with the results
                            sendMessage(mSdm.getmOriginPort(), MESSAGETYPE_QUERYSTARRESULTS,
                                    myPort, mSdm.getmKey(), cursorToString(mCursor));
                        }

                        // If message type is query star results
                        else if (mSdm.getMessageType().equalsIgnoreCase(
                                MESSAGETYPE_QUERYSTARRESULTS)) {

                            // Log.e(TAG,
                            // "Received query * results on " + myPort +
                            // " from "
                            // + mSdm.getmOriginPort());

                            // Check if responses < 3 && query incomplete
                            // As we will get at least 3 responses
                            if (isQueryGuarded.get()) {

                                if (mQueryResponses < 3 && !isQueryDone) {

                                    // Update results in global cursor map
                                    globalCursorMap.putAll(cursorToMap(stringToCursor(mSdm
                                            .getmValue())));

                                    // Increment response count
                                    mQueryResponses++;

                                    // If 3 responses then toggle flag
                                    if (mQueryResponses == 3) {
                                        isQueryDone = true;
                                    }
                                }
                            }

                        }

                        // If message type is query result
                        else if (mSdm.getMessageType().equalsIgnoreCase(MESSAGETYPE_QUERYRESULT)) {

                            // Log.e(TAG,
                            // "Received query results on " + myPort + " from "
                            // + mSdm.getmOriginPort() + " for key " +
                            // mSdm.getmKey());

                            // If the result is not for same key ignore
                            if (mSdm.getmKey().equalsIgnoreCase(mQueryKey)) {

                                // If returned cursor has non blank value
                                if (checkIfCursorHasValueForKey(stringToCursor(mSdm.getmValue()))) {

                                    // If main thread waiting for response
                                    if (isQueryGuarded.get()) {
                                        // If query not done send results
                                        if (!isQueryDone) {
                                            // Update results in globalcursormap
                                            globalCursorMap.putAll(cursorToMap(stringToCursor(mSdm
                                                    .getmValue())));
                                            isQueryDone = true;
                                        }
                                    }
                                }
                            }
                        }

                    }

                } // infinite while ends

            } catch (IOException e) {
                Log.e(TAG, "Cannot accept connection to a ServerSocket from the Client");
            } catch (NumberFormatException e) {
                Log.e(TAG, "Faced NumberFormatException while accepting connection from the Client");
            }

            return null;

        }// do in background ends
    }// server task class ends

    /**
     * The global query method which determines where a particular request
     * should go
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        Cursor mCursor = null;

        // If symbol is @ or * return all rows from local content provider and
        // others after filtering if only @
        if (selection.equalsIgnoreCase(SYMBOL_AT) || selection.equalsIgnoreCase(SYMBOL_STAR)) {

            // Guard the next part of critical section code as there are 3
            // global shared variables present in them namely; global cursor
            // map, isQueryDone and mQueryResponses
            while (!isQueryGuarded.compareAndSet(false, true)) {

            }

            globalCursorMap.clear();
            mQueryResponses = 0;
            isQueryDone = false;

            // Query my local content provider and put results in map
            globalCursorMap.putAll(cursorToMap(queryLocalContentProvider(uri, null, SYMBOL_AT,
                    null, null)));

            // Propagate query star message to everyone else
            for (Map.Entry<String, String[]> e : mNodeSuccessorMap.entrySet()) {
                if (!e.getKey().equalsIgnoreCase(myPort)) {
                    sendMessage(e.getKey(), MESSAGETYPE_QUERYSTAR, myPort, selection, SPACE_STRING);
                }
            }

            // Wait till the server task gets the query response
            while (!isQueryDone) {
                // try {
                // Thread.sleep(200);
                // } catch (InterruptedException e1) {
                // Log.e(TAG, "Query *@ sleep interruppted");
                // }
            }

            // Return the cursor after converting from global cursor map
            mCursor = hashMapToCursor(globalCursorMap);

            if (selection.equalsIgnoreCase(SYMBOL_AT)) {
                // Filter cursor on the values required for this host
                mCursor = filterCursorForHost(mCursor);
            }

            // Set the flag for the guarded region to false so that the next
            // thread can enter
            isQueryGuarded.set(false);

            return mCursor;

        }

        // Else a normal query where only 1 key needs to be searched in DHT
        else {

            String mKeyHash = null;
            try {
                mKeyHash = genHash(selection);
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "Cannot generate hash value.");
            }

            // Iterate over the node id set
            boolean mIsFound = false;
            String mPortToSend = BLANK_STRING;

            for (Map.Entry<String, String> e : mNodeIdNodePortMap.entrySet()) {
                // If key hash < id Hash we need to send to that node
                if (mKeyHash.compareTo(e.getKey()) <= 0) {
                    mIsFound = true;
                    mPortToSend = e.getValue();
                    break;
                }
            }

            // If no query destination found means hash was bigger than all
            // Thus we need to query from 1st node i.e. AVD4 & its successors
            if (!mIsFound) {
                mPortToSend = PORT_AVD4;
            }

            // Get the successor ports of the destination port
            String[] mSucPorts = mNodeSuccessorMap.get(mPortToSend);

            // Create a map of nodes where we need to insert with key as port
            Map<String, Integer> multicastMap = new HashMap<String, Integer>();
            multicastMap.put(mPortToSend, 1);
            multicastMap.put(mSucPorts[0], 2);
            multicastMap.put(mSucPorts[1], 3);

            // Guard this critical region as it has two global variables that
            // are shared by two threads
            while (!isQueryGuarded.compareAndSet(false, true)) {

            }

            // Check if the map has the local port
            if (multicastMap.containsKey(myPort)) {

                // If yes then query at local
                mCursor = queryLocalContentProvider(uri, null, selection, null, null);

                // If mCursor has proper value return
                if (checkIfCursorHasValueForKey(mCursor)) {
                    // Set false to allow next thread to enter
                    isQueryGuarded.set(false);
                    return mCursor;
                }

                // Else remove from map and send query to remote hosts
                else {
                    multicastMap.remove(myPort);
                }
            }

            // Else send query message to all remote hosts
            globalCursorMap.clear();
            isQueryDone = false;
            mQueryKey = selection;

            for (Map.Entry<String, Integer> e : multicastMap.entrySet()) {
                sendMessage(e.getKey(), MESSAGETYPE_QUERY, myPort, selection, SPACE_STRING);
            }

            // Wait for response from only one replica and proceed
            while (!isQueryDone) {

            }

            // Return cursor after converting from global cursor map
            mCursor = hashMapToCursor(globalCursorMap);

            // Set false to allow next thread to enter
            isQueryGuarded.set(false);

            return mCursor;
        }
    }

    /**
     * Method used to query the local content provider
     * 
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    private Cursor queryLocalContentProvider(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder mQueryBuilder = new SQLiteQueryBuilder();
        mQueryBuilder.setTables(DB_TABLE_NAME);
        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

        Cursor mCursor = null;

        // If @ then retrieve all the rows of the local content provider
        if (selection.equalsIgnoreCase(SYMBOL_AT)) {
            mCursor = mQueryBuilder.query(mDb, projection, null, null, null, null, sortOrder);
        }

        // Select based on the provided key
        else {
            mCursor = mQueryBuilder.query(mDb, projection, COLUMN_KEY + "=?", new String[] {
                selection
            }, null, null, sortOrder);
        }

        mCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Log.e(TAG, "Queried " + selection + " on " + myPort);

        return mCursor;
    }

    /**
     * Method used at a global level to determine where to insert content values
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        String mKey = values.get(COLUMN_KEY).toString();

        String mValue = values.get(COLUMN_VALUE).toString();

        String mKeyHash = null;
        try {
            mKeyHash = genHash(mKey);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Cannot generate hash value.");
        }

        // Iterate over the node id set
        boolean mIsFound = false;
        String mPortToSend = BLANK_STRING;

        for (Map.Entry<String, String> e : mNodeIdNodePortMap.entrySet()) {
            // If key hash < id Hash we need to send to that node
            if (mKeyHash.compareTo(e.getKey()) <= 0) {
                mIsFound = true;
                mPortToSend = e.getValue();
                break;
            }
        }

        // If no insert destination found means hash was bigger than all
        // Thus we need to insert from 1st node i.e. AVD4 & its successors
        if (!mIsFound) {
            mPortToSend = PORT_AVD4;
        }

        // Get the successor ports of the destination port
        String[] mSucPorts = mNodeSuccessorMap.get(mPortToSend);

        // Create a map of nodes where we need to insert with key as port
        Map<String, Integer> multicastMap = new HashMap<String, Integer>();
        multicastMap.put(mPortToSend, 1);
        multicastMap.put(mSucPorts[0], 2);
        multicastMap.put(mSucPorts[1], 3);

        // Guard this critical region as it has two global variables that
        // are shared by two threads
        while (!isInsertGuarded.compareAndSet(false, true)) {

        }

        mInsertKey = mKey;
        mInsertResponses = 0;
        isInsertDone = false;

        // Check if the map has the local port
        if (multicastMap.containsKey(myPort)) {

            // If yes then insert at local and increment response count
            insertIntoLocalContentProvider(uri, values);
            mInsertResponses++;

            // Remove that entry from the map
            multicastMap.remove(myPort);
        }

        // Now iterate over the map and send to remaining hosts
        for (Map.Entry<String, Integer> e : multicastMap.entrySet()) {
            sendMessage(e.getKey(), MESSAGETYPE_INSERT, myPort, mKey, mValue);
        }

        while (!isInsertDone) {

        }

        isInsertGuarded.set(false);

        return null;
    }

    /**
     * Method used to insert content values into local content provider
     * 
     * @param uri
     * @param values
     * @return
     */
    private Uri insertIntoLocalContentProvider(Uri uri, ContentValues values) {

        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

        String mKey = values.get(COLUMN_KEY).toString();

        // String mVal = values.get(COLUMN_VALUE).toString();

        // Check if local content provider already has record with this key
        // If yes then update the record else insert
        Cursor mQueryResult = queryLocalContentProvider(uri, null, mKey, null, null);
        if (mQueryResult.getCount() > 0) {
            update(uri, values, COLUMN_KEY, new String[] {
                mKey
            });
        } else {
            mDb.insert(DB_TABLE_NAME, null, values);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Log.e(TAG, "Inserted key " + mKey + " value " + mVal + " on host " +
        // myPort);

        return uri;

    }

    /**
     * Method used at a global level to recognize where to delete based on key
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Delete only at local content provider if @ provided
        if (selection.equalsIgnoreCase(SYMBOL_AT)) {
            deleteFromLocalContentProvider(SYMBOL_AT);
        }

        // Delete at all the content providers if * provided
        else if (selection.equalsIgnoreCase(SYMBOL_STAR)) {

            // Delete my local content provider
            deleteFromLocalContentProvider(SYMBOL_AT);

            // Send a delete all message to all the other nodes except self
            for (Map.Entry<String, String[]> e : mNodeSuccessorMap.entrySet()) {
                if (!e.getKey().equalsIgnoreCase(myPort)) {
                    sendMessage(e.getKey(), MESSAGETYPE_DELETESTAR, myPort, SYMBOL_AT, SPACE_STRING);
                }
            }

        }

        // Normal deletion of an entry in the DHT
        else {

            String mKeyHash = null;
            try {
                mKeyHash = genHash(selection);
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "Cannot generate hash value.");
            }

            // Iterate over the node id set
            boolean mIsFound = false;
            String mPortToSend = BLANK_STRING;

            for (Map.Entry<String, String> e : mNodeIdNodePortMap.entrySet()) {
                // If key hash < id Hash we need to send to that node
                if (mKeyHash.compareTo(e.getKey()) <= 0) {
                    mIsFound = true;
                    mPortToSend = e.getValue();
                    break;
                }
            }

            // If no delete destination found means hash was bigger than all
            // Thus we need to delete from 1st node i.e. AVD4 & its successors
            if (!mIsFound) {
                mPortToSend = PORT_AVD4;
            }

            // Get the successor ports of the destination port
            String[] mSucPorts = mNodeSuccessorMap.get(mPortToSend);

            // Create a map of nodes where we need to delete with key as port
            Map<String, Integer> multicastMap = new HashMap<String, Integer>();
            multicastMap.put(mPortToSend, 1);
            multicastMap.put(mSucPorts[0], 2);
            multicastMap.put(mSucPorts[1], 3);

            // Check if the map has the local port
            if (multicastMap.containsKey(myPort)) {

                // If yes then delete at local
                deleteFromLocalContentProvider(selection);

                // Remove that entry from the map
                multicastMap.remove(myPort);
            }

            // Now iterate over the map and send to remaining hosts
            for (Map.Entry<String, Integer> e : multicastMap.entrySet()) {
                sendMessage(e.getKey(), MESSAGETYPE_DELETE, myPort, selection, SPACE_STRING);
            }
        }

        return 0;
    }

    /**
     * Method used to delete from local content provider
     * 
     * @param selection
     * @return
     */
    private int deleteFromLocalContentProvider(String selection) {

        int rowsDeleted = 0;

        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

        // If symbol is @ then delete all records
        // Else delete a specific record
        if (selection.equals(SYMBOL_AT)) {
            rowsDeleted = mDb.delete(DB_TABLE_NAME, NUMBER_ONE, null);
        } else {
            rowsDeleted = mDb.delete(DB_TABLE_NAME, COLUMN_KEY + "=?", new String[] {
                selection
            });
        }

        getContext().getContentResolver().notifyChange(mLocalUri, null);

        return rowsDeleted;
    }

    /**
     * This thread class is used to send messages to the remote hosts
     * 
     * @author sudarshan
     */
    private class SendMessageThread implements Runnable {

        Thread mTh;

        String msg;

        String mDestination;

        SendMessageThread(String mDest, String mType, String mSrc, String mKy, String mVal) {
            mTh = new Thread(this);
            this.mDestination = mDest;
            msg = mType + PIPE + mSrc + PIPE + mKy + PIPE + mVal;
            mTh.start();
        }

        public void run() {

            try {

                Socket mSock = new Socket(InetAddress.getByAddress(new byte[] {
                        10, 0, 2, 2
                }), Integer.parseInt(mDestination));

                PrintWriter mpw = new PrintWriter(mSock.getOutputStream(), true);

                mpw.println(msg);

                mpw.close();

                mSock.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException.");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException.");
            } catch (Exception e) {
                Log.e(TAG, "Emulator is not available.");

            }
        }
    }

    /**
     * Method used to send the required message to the destination host
     * 
     * @param mDestination
     */
    private void sendMessage(String mDestination, String msgType, String mSrcPort, String mKy,
            String mVal) {

        // Log.e(TAG, "Sending " + msgType + " to " + mDestination + " from " +
        // myPort + " with key "
        // + mKy + " and value " + mVal);

        new SendMessageThread(mDestination, msgType, mSrcPort, mKy, mVal);
    }

    /**
     * The update method to update values that already exist in the content
     * provider
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();
        int mRowsUpdated = mDb.update(DB_TABLE_NAME, values, COLUMN_KEY + "=?", selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return mRowsUpdated;

    }

    /**
     * Method used to build the URI
     * 
     * @param scheme
     * @param authority
     * @return
     */
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    /**
     * Method used to generate the SHA-1 hash for a key string
     * 
     * @param input
     * @return
     * @throws NoSuchAlgorithmException
     */
    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        String mHash = formatter.toString();
        formatter.close();
        return mHash;
    }

    /**
     * Standard function to convert hash map to cursor
     * 
     * @param cursorMap
     * @return
     */
    public MatrixCursor hashMapToCursor(Map<String, String> cursorMap) {

        MatrixCursor mCursor = new MatrixCursor(new String[] {
                COLUMN_KEY, COLUMN_VALUE
        });
        Iterator<Map.Entry<String, String>> mIter = cursorMap.entrySet().iterator();
        while (mIter.hasNext()) {
            Map.Entry<String, String> mEntry = mIter.next();
            mCursor.addRow(new String[] {
                    (String)mEntry.getKey(), (String)mEntry.getValue()
            });
        }

        return mCursor;
    }

    /**
     * Standard function to convert cursor to hash map
     * 
     * @param cursor
     * @return
     */
    public Map<String, String> cursorToMap(Cursor cursor) {

        Map<String, String> mCursorMap = new HashMap<String, String>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            mCursorMap.put(cursor.getString(cursor.getColumnIndex(COLUMN_KEY)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_VALUE)));
            cursor.moveToNext();
        }

        return mCursorMap;
    }

    /**
     * Method used to convert cursor to string
     * 
     * @return
     */
    private String cursorToString(Cursor mCursor) {

        StringBuilder msb = new StringBuilder();

        if (mCursor.moveToFirst()) {
            do {
                msb.append(mCursor.getString(0) + TILDE + mCursor.getString(1) + HASH);
            } while (mCursor.moveToNext());
        }

        if (msb.length() > 0) {
            msb.setLength(msb.length() - 1);
        }

        if (msb.toString().equalsIgnoreCase(BLANK_STRING)) {
            return SPACE_STRING;
        } else {
            return msb.toString();
        }
    }

    /**
     * Method used to convert cursor to string
     * 
     * @return
     */
    private Cursor stringToCursor(String mStr) {

        MatrixCursor mCursor = new MatrixCursor(new String[] {
                COLUMN_KEY, COLUMN_VALUE
        });

        if (null != mStr && !mStr.trim().equalsIgnoreCase(BLANK_STRING) && mStr.trim().length() > 0) {

            String[] msgs = mStr.split(HASH);
            for (int i = 0; i < msgs.length; i++) {
                mCursor.addRow(new String[] {
                        msgs[i].split(TILDE)[0], msgs[i].split(TILDE)[1]
                });
            }
        }

        return mCursor;
    }

    /**
     * Method that check if query result has value or not
     * 
     * @return
     */
    private boolean checkIfCursorHasValueForKey(Cursor mCur) {

        // Check if cursor is not null and
        if (mCur.moveToFirst()) {
            // Check if for a key the cursor has a non blank value
            if (null != mCur.getString(1)
                    && !mCur.getString(1).trim().equalsIgnoreCase(BLANK_STRING)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This function removes unwanted data not pertaining to this host from the
     * cursor
     */
    private Cursor filterCursorForHost(Cursor mFilterCur) {

        MatrixCursor mCsr = new MatrixCursor(new String[] {
                COLUMN_KEY, COLUMN_VALUE
        });

        String mfck = null, mGenHsh = null;

        // If cursor is not null
        if (mFilterCur.moveToFirst()) {
            do {
                mfck = mFilterCur.getString(0);
                try {
                    mGenHsh = genHash(mfck);
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, "Cannot generate hash value.");
                }
                if (myPort.equalsIgnoreCase(PORT_AVD0)) {
                    if (mGenHsh.compareTo(mNodePortNodeIdMap.get(PORT_AVD0)) <= 0
                            || mGenHsh.compareTo(mNodePortNodeIdMap.get(PORT_AVD3)) > 0) {
                        mCsr.addRow(new String[] {
                                mfck, mFilterCur.getString(1)
                        });
                    }
                } else if (myPort.equalsIgnoreCase(PORT_AVD1)) {
                    if (mGenHsh.compareTo(mNodePortNodeIdMap.get(PORT_AVD1)) <= 0
                            || mGenHsh.compareTo(mNodePortNodeIdMap.get(PORT_AVD2)) > 0) {
                        mCsr.addRow(new String[] {
                                mfck, mFilterCur.getString(1)
                        });
                    }
                } else if (myPort.equalsIgnoreCase(PORT_AVD2)) {
                    if (mGenHsh.compareTo(mNodePortNodeIdMap.get(PORT_AVD2)) <= 0
                            && mGenHsh.compareTo(mNodePortNodeIdMap.get(PORT_AVD4)) > 0) {
                        mCsr.addRow(new String[] {
                                mfck, mFilterCur.getString(1)
                        });
                    }
                } else if (myPort.equalsIgnoreCase(PORT_AVD3)) {
                    if (mGenHsh.compareTo(mNodePortNodeIdMap.get(PORT_AVD3)) <= 0
                            && mGenHsh.compareTo(mNodePortNodeIdMap.get(PORT_AVD1)) > 0) {
                        mCsr.addRow(new String[] {
                                mfck, mFilterCur.getString(1)
                        });
                    }
                } else if (myPort.equalsIgnoreCase(PORT_AVD4)) {
                    if (mGenHsh.compareTo(mNodePortNodeIdMap.get(PORT_AVD4)) <= 0
                            || mGenHsh.compareTo(mNodePortNodeIdMap.get(PORT_AVD0)) > 0) {
                        mCsr.addRow(new String[] {
                                mfck, mFilterCur.getString(1)
                        });
                    }
                }
            } while (mFilterCur.moveToNext());
        }

        return mCsr;

    }

    /**
     * This function is used to insert the filtered data into the local content
     * provider after recovery
     * 
     * @param mRecCur
     */
    private void filterAndInsertIntoLocalContentProvider(Cursor mRecCur) {

        String mCurKey = null, mGnHash = null;

        // If cursor is not null
        if (mRecCur.moveToFirst()) {
            do {
                mCurKey = mRecCur.getString(0);
                try {
                    mGnHash = genHash(mCurKey);
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, "Cannot generate hash value.");
                }
                if (myPort.equalsIgnoreCase(PORT_AVD0)) {
                    if (mGnHash.compareTo(mNodePortNodeIdMap.get(PORT_AVD0)) <= 0
                            || mGnHash.compareTo(mNodePortNodeIdMap.get(PORT_AVD3)) > 0) {
                        localInsert(mCurKey, mRecCur.getString(1));
                    }
                } else if (myPort.equalsIgnoreCase(PORT_AVD1)) {
                    if (mGnHash.compareTo(mNodePortNodeIdMap.get(PORT_AVD1)) <= 0
                            || mGnHash.compareTo(mNodePortNodeIdMap.get(PORT_AVD2)) > 0) {
                        localInsert(mCurKey, mRecCur.getString(1));
                    }
                } else if (myPort.equalsIgnoreCase(PORT_AVD2)) {
                    if (mGnHash.compareTo(mNodePortNodeIdMap.get(PORT_AVD2)) <= 0
                            && mGnHash.compareTo(mNodePortNodeIdMap.get(PORT_AVD4)) > 0) {
                        localInsert(mCurKey, mRecCur.getString(1));
                    }
                } else if (myPort.equalsIgnoreCase(PORT_AVD3)) {
                    if (mGnHash.compareTo(mNodePortNodeIdMap.get(PORT_AVD3)) <= 0
                            && mGnHash.compareTo(mNodePortNodeIdMap.get(PORT_AVD1)) > 0) {
                        localInsert(mCurKey, mRecCur.getString(1));
                    }
                } else if (myPort.equalsIgnoreCase(PORT_AVD4)) {
                    if (mGnHash.compareTo(mNodePortNodeIdMap.get(PORT_AVD4)) <= 0
                            || mGnHash.compareTo(mNodePortNodeIdMap.get(PORT_AVD0)) > 0) {
                        localInsert(mCurKey, mRecCur.getString(1));
                    }
                }

            } while (mRecCur.moveToNext());
        }

    }

    /**
     * Utility function to call insert into local content provider
     * 
     * @param mCrKy
     * @param mCrVl
     */
    private void localInsert(String mCrKy, String mCrVl) {
        // Insert in local content provider
        ContentValues mCv = new ContentValues();
        mCv.put(COLUMN_KEY, mCrKy);
        mCv.put(COLUMN_VALUE, mCrVl);
        insertIntoLocalContentProvider(mLocalUri, mCv);
    }

    /**
     * This function checks if the database already exists or not
     */
    private boolean checkIfDbExists() {
        SQLiteDatabase mCheckDb = null;
        try {
            mCheckDb = SQLiteDatabase.openDatabase(getContext().getDatabasePath(DB_NAME).getPath(),
                    null, SQLiteDatabase.OPEN_READONLY);
            mCheckDb.close();
        } catch (SQLiteException e) {

        }
        return mCheckDb != null ? true : false;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

}
