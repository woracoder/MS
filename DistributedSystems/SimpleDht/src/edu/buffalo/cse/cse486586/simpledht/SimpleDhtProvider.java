
package edu.buffalo.cse.cse486586.simpledht;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
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

/**
 * This class acts like the content provider for this application and supports
 * insert, query and delete operations over a simple DHT
 * 
 * @author sudarshan
 */
public class SimpleDhtProvider extends ContentProvider {

    // Variable used to store the results of query for a key
    private Map<String, String> globalCursorMap;

    // Variable used as a helper object to access the SQLite database
    private SimpleDhtDbHelper mDbHelper;

    // Variable used to store the local URI for the database
    private Uri mLocalUri;

    // Variable used to store number of rows inserted
    static int mInsertCount = 0;

    // Variable used to store number of rows deleted
    static int mDeleteCount = 0;

    // Variables used to wait on for query
    boolean isQueryStar, isQuery;

    // Variable used to store port number of host
    private String myPort;

    // Variable used to store port number of host predecessor
    private String mPredPort;

    // Variable used to store port number of host successor
    private String mSucPort;

    // Variable used to store node id of host
    private String mNodeId;

    // Variable used to store node id of host predecessor
    private String mPredNodeId;

    // Variable used to store node id of host successor
    private String mSucNodeId;

    /**
     * Method called by the android framework when the application is started
     */
    @Override
    public boolean onCreate() {

        // Initialize the global map which stores results of query
        globalCursorMap = new HashMap<String, String>();

        // Initialize flags for waiting after calling query on the DHT
        isQueryStar = isQuery = false;

        // Initialize the SQLite database helper
        mDbHelper = new SimpleDhtDbHelper(getContext());

        // Build the URI for operations on the SQLite database
        mLocalUri = buildUri(ApplicationConstants.CONTENT, ApplicationConstants.PROVIDER);

        // Get the emulator and virtual router port
        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(
                Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);

        // Initialize my listening port
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        // Initially set my predecessor and successor as me
        mPredPort = mSucPort = myPort;

        // Calculate the Node ID for this AVD
        try {
            mNodeId = genHash(portStr);
            // Initially set my predecessor and successor node id as me
            mPredNodeId = mSucNodeId = mNodeId;
        } catch (NoSuchAlgorithmException e1) {
            Log.e(ApplicationConstants.TAG, "Cannot generate hash value.");
        }

        // Listen on port 10000 for incoming requests
        try {
            ServerSocket mServerSocket = new ServerSocket(ApplicationConstants.SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mServerSocket);
        } catch (IOException e) {
            Log.e(ApplicationConstants.TAG, "Cannot create a ServerSocket");
        }

        // Check if this is AVD0 which handles all the joins
        // If it is not only then try to join the chord network
        if (null != myPort && !myPort.equalsIgnoreCase(ApplicationConstants.REMOTE_PORT0)) {

            sendMessage(ApplicationConstants.REMOTE_PORT0,
                    ApplicationConstants.MESSAGETYPE_JOIN, myPort,
                    ApplicationConstants.SPACE_STRING, ApplicationConstants.SPACE_STRING);
        }

        return false;
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
     * Method that generates the hash value for any given string
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
     * Method used to send the required message to the destination host
     * 
     * @param mDestination
     */
    private void sendMessage(String mDestination, String msgType, String mSrcPort, String mSucPort,
            String mPredPort) {

        Log.e(ApplicationConstants.TAG, " Message type and port " + msgType + " " + myPort);

        new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mDestination, msgType
                + ApplicationConstants.PIPE + mSrcPort + ApplicationConstants.PIPE + mSucPort
                + ApplicationConstants.PIPE + mPredPort);
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
                    Socket msoc = serverSocket.accept();

                    // Get the newly arrived message
                    BufferedReader mbr = new BufferedReader(
                            new InputStreamReader(msoc.getInputStream()));

                    // If not null split the messages and
                    String msg = null;
                    if (null == (msg = mbr.readLine())) {
                        Log.e(ApplicationConstants.TAG, "Null received from Buffered reader");
                        return null;
                    }

                    String[] msgs = msg.split(ApplicationConstants.REGEX_PIPE);

                    Log.e(ApplicationConstants.TAG, "Messages = " + msgs[0] + " " + msgs[1] + " "
                            + msgs[2]);

                    // Create a new message object
                    SimpleDhtMessage msdm = new SimpleDhtMessage(msgs[0], msgs[1], msgs[2], msgs[3]);

                    if (null != msdm) {

                        // If this is AVD0 and a join request or a join
                        // propagate message
                        if ((msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_JOIN)
                                && myPort.equalsIgnoreCase(ApplicationConstants.REMOTE_PORT0))
                                || msdm.getMessageType().equalsIgnoreCase(
                                        ApplicationConstants.MESSAGETYPE_JOINPROPAGATE)) {

                            // Method that handles all node join scenarios
                            handleNodeJoin(msdm);
                        }

                        // If it is a join response message then update the
                        // successor and predecessor values
                        else if (msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_JOINRESPONSE)) {

                            Log.e(ApplicationConstants.TAG, "Received join response " + myPort);

                            mSucPort = msdm.getmSucPort();
                            mSucNodeId = genHash(String.valueOf(Integer.parseInt(mSucPort) / 2));
                            mPredPort = msdm.getmPredPort();
                            mPredNodeId = genHash(String.valueOf(Integer.parseInt(mPredPort) / 2));

                        }

                        // If it is a join update successor message then update
                        // the successor values
                        else if (msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_JOINUPDATESUC)) {

                            Log.e(ApplicationConstants.TAG, "Received join update suc " + myPort);

                            mSucPort = msdm.getmSucPort();
                            mSucNodeId = genHash(String.valueOf(Integer.parseInt(mSucPort) / 2));

                        }

                        // If it is a join update predecessor message then
                        // update the predecessor values
                        else if (msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_JOINUPDATEPRED)) {

                            Log.e(ApplicationConstants.TAG, "Received join update pred " + myPort);

                            mPredPort = msdm.getmPredPort();
                            mPredNodeId = genHash(String.valueOf(Integer.parseInt(mPredPort) / 2));
                        }

                        // If it is a insert message then insert into local
                        // content provider
                        else if (msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_INSERT)) {

                            Log.e(ApplicationConstants.TAG, "Received message local insert "
                                    + myPort);

                            ContentValues mcv = new ContentValues();
                            mcv.put(ApplicationConstants.COLUMN_KEY, msdm.getmSucPort());
                            mcv.put(ApplicationConstants.COLUMN_VALUE, msdm.getmPredPort());
                            insertIntoLocalContentProvider(mLocalUri, mcv);
                        }

                        // If its a insert propagate message then call insert
                        // method again
                        else if (msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_INSERTPROPAGATE)) {

                            Log.e(ApplicationConstants.TAG, "Received message insert propagate "
                                    + myPort);

                            ContentValues mcv = new ContentValues();
                            mcv.put(ApplicationConstants.COLUMN_KEY, msdm.getmSucPort());
                            mcv.put(ApplicationConstants.COLUMN_VALUE, msdm.getmPredPort());
                            insert(mLocalUri, mcv);
                        }

                        // If it is a delete star message then do local delete
                        // Then check if this message needs to be propagated
                        // further
                        else if (msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_DELETESTAR)) {

                            Log.e(ApplicationConstants.TAG, "Received message delete * " + myPort);

                            // First delete the local content provider
                            deleteFromLocalContentProvider(ApplicationConstants.SYMBOL_AT);

                            // If originator of delete star is not my successor
                            // only then propagate message further
                            if (!mSucPort.equalsIgnoreCase(msdm.getmOriginPort())) {

                                // Propagate delete star message further
                                sendMessage(mSucPort, ApplicationConstants.MESSAGETYPE_DELETESTAR,
                                        msdm.getmOriginPort(), ApplicationConstants.SPACE_STRING,
                                        ApplicationConstants.SPACE_STRING);
                            }
                        }

                        // If it is a delete message then delete from local
                        // content provider
                        else if (msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_DELETE)) {

                            Log.e(ApplicationConstants.TAG, "Received message local delete "
                                    + myPort);

                            deleteFromLocalContentProvider(msdm.getmOriginPort());
                        }

                        // If its a delete propagate message then call delete
                        // method again
                        else if (msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_DELETEPROPAGATE)) {

                            Log.e(ApplicationConstants.TAG, "Received message delete propagate "
                                    + myPort);

                            delete(mLocalUri, msdm.getmOriginPort(), null);
                        }

                        // Check if its is query * message
                        else if (msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_QUERYSTAR)) {

                            Log.e(ApplicationConstants.TAG, "Received message query star " + myPort);

                            // Get results by querying local content provider
                            // Query my local content provider
                            Cursor mCursor = queryLocalContentProvider(mLocalUri, null,
                                    ApplicationConstants.SYMBOL_AT, null, null);

                            // Reply back to the originator with the results
                            sendMessage(msdm.getmOriginPort(),
                                    ApplicationConstants.MESSAGETYPE_QUERYRESULTS, myPort,
                                    ApplicationConstants.SPACE_STRING, cursorToString(mCursor));

                            // Check if next node is not originator
                            if (!mSucPort.equalsIgnoreCase(msdm.getmOriginPort())) {

                                // If not then send query * message to successor
                                sendMessage(mSucPort,
                                        ApplicationConstants.MESSAGETYPE_QUERYSTAR,
                                        msdm.getmOriginPort(), ApplicationConstants.SPACE_STRING,
                                        ApplicationConstants.SPACE_STRING);
                            }
                        }

                        // Check if it is a query results message
                        else if (msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_QUERYRESULTS)) {

                            Log.e(ApplicationConstants.TAG, "Received message query results "
                                    + myPort);

                            // Update the results in the global cursor map
                            globalCursorMap
                                    .putAll(cursorToMap(stringToCursor(msdm.getmPredPort())));

                            // If this is from my successor set the isQueryStar
                            // flag as true and stop waiting as all the results
                            // have arrived
                            if (msdm.getmOriginPort().equalsIgnoreCase(
                                    ApplicationConstants.SPACE_STRING)) {
                                isQuery = true;
                            } else if (msdm.getmOriginPort().equalsIgnoreCase(mPredPort)) {
                                isQueryStar = true;
                            }
                        }

                        // Check if it is a query message
                        else if (msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_QUERY)) {

                            Log.e(ApplicationConstants.TAG, "Received message query " + myPort);

                            Cursor mCursor = queryLocalContentProvider(mLocalUri, null,
                                    msdm.getmSucPort(), null, null);

                            // Reply back to the originator with the results
                            sendMessage(msdm.getmOriginPort(),
                                    ApplicationConstants.MESSAGETYPE_QUERYRESULTS,
                                    ApplicationConstants.SPACE_STRING,
                                    ApplicationConstants.SPACE_STRING, cursorToString(mCursor));
                        }

                        // Check if it is a query propagate message
                        else if (msdm.getMessageType().equalsIgnoreCase(
                                ApplicationConstants.MESSAGETYPE_QUERYPROPAGATE)) {

                            Log.e(ApplicationConstants.TAG, "Received message query propagate "
                                    + myPort);

                            Cursor mCursor;
                            // Call the query method and check
                            if (null != (mCursor = query(mLocalUri, null, msdm.getmSucPort(),
                                    new String[] {
                                        msdm.getmOriginPort()
                                    }, null))) {

                                // Send the query results to the originator
                                sendMessage(msdm.getmOriginPort(),
                                        ApplicationConstants.MESSAGETYPE_QUERYRESULTS,
                                        ApplicationConstants.SPACE_STRING,
                                        ApplicationConstants.SPACE_STRING, cursorToString(mCursor));
                            }
                        }

                    }

                    Log.e(ApplicationConstants.TAG, "Port = " + myPort + " Pred = " + mPredPort
                            + " Suc = " + mSucPort);

                }
            } catch (IOException e) {
                Log.e(ApplicationConstants.TAG,
                        "Cannot accept connection to a ServerSocket from the Client");
                Log.e(ApplicationConstants.TAG, e.getMessage(), e);
            } catch (NumberFormatException e) {
                Log.e(ApplicationConstants.TAG,
                        "Faced NumberFormatException while accepting connection from the Client");
            } catch (NoSuchAlgorithmException e) {
                Log.e(ApplicationConstants.TAG,
                        "Could not generate hash value in ServerTask.");
            }

            return null;
        }

        /**
         * Method that handles the various node join scenarios
         * 
         * @param msdm
         * @throws NoSuchAlgorithmException
         * @throws NumberFormatException
         */
        private void handleNodeJoin(SimpleDhtMessage msdm) throws NoSuchAlgorithmException,
                NumberFormatException {

            Log.e(ApplicationConstants.TAG, "Received join message " + myPort);

            // Get the node id of the originator of the request
            String mOrigNodeId = genHash(String.valueOf(Integer.parseInt(msdm
                    .getmOriginPort()) / 2));

            /**
             * Case 1: When AVD0s successor, predecessor and node ID are the
             * same. This implies AVD0 is the only host currently in the chord
             * network. Set the successor and predecessor as the arrived node
             * and send it AVD0s node id
             */
            if (myPort.equalsIgnoreCase(mSucPort) && myPort.equalsIgnoreCase(mPredPort)
                    && mNodeId.equalsIgnoreCase(mPredNodeId)
                    && mNodeId.equalsIgnoreCase(mSucNodeId)) {

                mSucPort = mPredPort = msdm.getmOriginPort();
                mSucNodeId = mPredNodeId = mOrigNodeId;

                // Send response to original join requester
                sendMessage(msdm.getmOriginPort(), ApplicationConstants.MESSAGETYPE_JOINRESPONSE,
                        ApplicationConstants.SPACE_STRING, myPort, myPort);
            }

            // Case 2: When hash value >= mine
            else if (mOrigNodeId.compareTo(mNodeId) >= 0) {

                // If hash >= mine and <= my successor or
                // if my hash >= my successor
                if (mOrigNodeId.compareTo(mSucNodeId) <= 0
                        || mNodeId.compareTo(mSucNodeId) >= 0) {

                    // New node will join in between
                    sendMessage(msdm.getmOriginPort(),
                            ApplicationConstants.MESSAGETYPE_JOINRESPONSE,
                            ApplicationConstants.SPACE_STRING, mSucPort, myPort);

                    // Send update info to previous successor
                    sendMessage(mSucPort, ApplicationConstants.MESSAGETYPE_JOINUPDATEPRED,
                            ApplicationConstants.SPACE_STRING, ApplicationConstants.SPACE_STRING,
                            msdm.getmOriginPort());

                    // Update values on local now
                    mSucPort = msdm.getmOriginPort();
                    mSucNodeId = mOrigNodeId;

                    Log.e(ApplicationConstants.TAG, "Port = " + myPort + " Pred = " + mPredPort
                            + " Suc = " + mSucPort + " PredId = " + mPredNodeId + " SucId = "
                            + mSucNodeId);
                }

                // Else if hash > my successor then propagate further
                else if (mOrigNodeId.compareTo(mSucNodeId) > 0) {

                    // Send details to successor node and ask it
                    // to check if it can place this node
                    sendMessage(mSucPort, ApplicationConstants.MESSAGETYPE_JOINPROPAGATE,
                            msdm.getmOriginPort(), ApplicationConstants.SPACE_STRING,
                            ApplicationConstants.SPACE_STRING);
                }
            }

            // Case 3: when hash value is less than AVD0
            else if (mOrigNodeId.compareTo(mNodeId) < 0) {

                // If hash < mine and >= my predecessor or
                // if my hash is <= my predecessor
                if (mOrigNodeId.compareTo(mPredNodeId) >= 0 || mNodeId.compareTo(mPredNodeId) <= 0) {

                    // New node will join in between
                    sendMessage(msdm.getmOriginPort(),
                            ApplicationConstants.MESSAGETYPE_JOINRESPONSE,
                            ApplicationConstants.SPACE_STRING, myPort, mPredPort);

                    // Send update info to previous predecessor
                    sendMessage(mPredPort, ApplicationConstants.MESSAGETYPE_JOINUPDATESUC,
                            ApplicationConstants.SPACE_STRING, msdm.getmOriginPort(),
                            ApplicationConstants.SPACE_STRING);

                    // Update values on local now
                    mPredPort = msdm.getmOriginPort();
                    mPredNodeId = mOrigNodeId;

                }

                // Else if hash < predecessor propagate further
                else if (mOrigNodeId.compareTo(mPredNodeId) < 0) {

                    // Send details to predecessor node and ask
                    // it to check if it can place this node
                    sendMessage(mPredPort, ApplicationConstants.MESSAGETYPE_JOINPROPAGATE,
                            msdm.getmOriginPort(), ApplicationConstants.SPACE_STRING,
                            ApplicationConstants.SPACE_STRING);
                }
            }
        }
    }

    /**
     * Client task that is responsible for sending messages to different hosts
     * 
     * @author sudarshan
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                Socket socket = new Socket(InetAddress.getByAddress(new byte[] {
                        10, 0, 2, 2
                }), Integer.parseInt(msgs[0]));

                PrintWriter mpw = new PrintWriter(socket.getOutputStream(), true);

                mpw.println(msgs[1]);

                mpw.close();

                socket.close();

            } catch (UnknownHostException e) {
                Log.e(ApplicationConstants.TAG, "ClientTask UnknownHostException");
            } catch (IOException ex) {
                Log.e(ApplicationConstants.TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }

    /**
     * @author sudarshan
     */
    private class SendMessageThread implements Runnable {

        Thread mTh;

        String msg;

        SendMessageThread(String msg) {
            mTh = new Thread(this);
            this.msg = msg;
            mTh.start();
        }

        public void run() {
            String msgArray[];
            try {

                msgArray = msg.split(ApplicationConstants.TILDE);

                Socket socket = new Socket(InetAddress.getByAddress(new byte[] {
                        10, 0, 2, 2
                }), Integer.parseInt(msgArray[0]));

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(msgArray[1]);
                socket.close();
            } catch (UnknownHostException e) {
                Log.e(ApplicationConstants.TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(ApplicationConstants.TAG, "ClientTask socket IOException");
            } catch (Exception e) {
                Log.e(ApplicationConstants.TAG, "Emulator 5554 is not available. Cannot join \n");

            }
        }
    }

    /**
     * Method used at a global level to determine where to insert content values
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        String mKey = values.get(ApplicationConstants.COLUMN_KEY).toString();

        String mValue = values.get(ApplicationConstants.COLUMN_VALUE).toString();

        String mKeyHash = null;
        try {
            mKeyHash = genHash(mKey);
        } catch (NoSuchAlgorithmException e) {
            Log.e(ApplicationConstants.TAG, "Cannot generate hash value.");
        }

        // Check if this is the only node
        // If yes then local insert
        if (myPort.equalsIgnoreCase(mSucPort) && myPort.equalsIgnoreCase(mPredPort)
                && mNodeId.equalsIgnoreCase(mSucNodeId)
                && mNodeId.equalsIgnoreCase(mPredNodeId)) {
            insertIntoLocalContentProvider(uri, values);
        }
        // If key hash >= mine
        else if (mKeyHash.compareTo(mNodeId) >= 0) {

            // If keyHash >= mine and <= my successor or
            // If keyHash >= mine and mine >= my successor
            if (mKeyHash.compareTo(mSucNodeId) <= 0 || mNodeId.compareTo(mSucNodeId) >= 0) {

                // Insert at successor by sending insert message
                new SendMessageThread(mSucPort + ApplicationConstants.TILDE
                        + ApplicationConstants.MESSAGETYPE_INSERT
                        + ApplicationConstants.PIPE
                        + ApplicationConstants.SPACE_STRING + ApplicationConstants.PIPE + mKey
                        + ApplicationConstants.PIPE + mValue);
            }

            // If keyHash >= mine and > successor
            else if (mKeyHash.compareTo(mSucNodeId) > 0) {

                // Send insert propagate message to successor
                new SendMessageThread(mSucPort + ApplicationConstants.TILDE
                        + ApplicationConstants.MESSAGETYPE_INSERTPROPAGATE
                        + ApplicationConstants.PIPE
                        + ApplicationConstants.SPACE_STRING + ApplicationConstants.PIPE + mKey
                        + ApplicationConstants.PIPE + mValue);
            }
        }

        // If mKeyHash < me
        else if (mKeyHash.compareTo(mNodeId) < 0) {

            // If mKeyHash < me and mKeyHash >= my predecessor or
            // If my < my predecessor
            if (mKeyHash.compareTo(mPredNodeId) >= 0 || mNodeId.compareTo(mPredNodeId) <= 0) {
                // Insert in local provider
                insertIntoLocalContentProvider(uri, values);
            }

            // If mKeyHash < predecessor
            else if (mKeyHash.compareTo(mPredNodeId) < 0) {

                // Send insert propagate message to predecessor
                new SendMessageThread(mPredPort + ApplicationConstants.TILDE
                        + ApplicationConstants.MESSAGETYPE_INSERTPROPAGATE
                        + ApplicationConstants.PIPE
                        + ApplicationConstants.SPACE_STRING + ApplicationConstants.PIPE + mKey
                        + ApplicationConstants.PIPE + mValue);
            }
        }

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

        String mKey = values.get(ApplicationConstants.COLUMN_KEY).toString();

        // Check if local content provider already has record with this key
        // If yes then update the record else insert
        Cursor mQueryResult = queryLocalContentProvider(uri, null, mKey, null, null);
        if (mQueryResult.getCount() > 0) {
            update(uri, values, ApplicationConstants.COLUMN_KEY, new String[] {
                    mKey
            });
        } else {
            mDb.insert(ApplicationConstants.DB_TABLE_NAME, null, values);
        }

        Log.e(ApplicationConstants.TAG,
                "Rows inserted at host " + myPort + " = " + String.valueOf(++mInsertCount));

        getContext().getContentResolver().notifyChange(uri, null);
        Log.v("insert", values.toString());
        return uri;

    }

    /**
     * The update method to update values that already exist in the content
     * provider
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();
        int mRowsUpdated = mDb.update(ApplicationConstants.DB_TABLE_NAME, values,
                ApplicationConstants.COLUMN_KEY + "=?", selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return mRowsUpdated;
    }

    /**
     * Method used at a global level to recognize from where to delete based on
     * key
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Delete only at local content provider if @ provided
        if (selection.equalsIgnoreCase(ApplicationConstants.SYMBOL_AT)) {
            deleteFromLocalContentProvider(selection);
        }

        // Delete at all the content providers if * provided
        else if (selection.equalsIgnoreCase(ApplicationConstants.SYMBOL_STAR)) {

            // Send a delete all message to all the other nodes
            // Basically send the message with originator port and
            // stop passing the message when the successor is the originator

            // If my successor is me then it means I am alone
            if (myPort.equalsIgnoreCase(mSucPort)) {
                deleteFromLocalContentProvider(ApplicationConstants.SYMBOL_AT);
            }

            // Else delete at local and propagate further
            else {

                // Delete my local content provider
                deleteFromLocalContentProvider(ApplicationConstants.SYMBOL_AT);

                // Propagate delete star message to successor
                sendMessage(mSucPort, ApplicationConstants.MESSAGETYPE_DELETESTAR, myPort,
                        ApplicationConstants.SPACE_STRING, ApplicationConstants.SPACE_STRING);
            }
        }

        // Normal deletion of an entry in the DHT
        else {

            String mKeyHash = null;
            try {
                mKeyHash = genHash(selection);
            } catch (NoSuchAlgorithmException e) {
                Log.e(ApplicationConstants.TAG, "Cannot generate hash value.");
            }

            // Check if this is the only node
            // If yes then local delete
            if (myPort.equalsIgnoreCase(mSucPort) && myPort.equalsIgnoreCase(mPredPort)
                    && mNodeId.equalsIgnoreCase(mSucNodeId)
                    && mNodeId.equalsIgnoreCase(mPredNodeId)) {
                deleteFromLocalContentProvider(selection);
            }

            // If key hash >= mine
            else if (mKeyHash.compareTo(mNodeId) >= 0) {

                // If keyHash >= mine and <= my successor or
                // If keyHash >= mine and mine >= my successor
                if (mKeyHash.compareTo(mSucNodeId) <= 0 || mNodeId.compareTo(mSucNodeId) >= 0) {

                    sendMessage(mSucPort, ApplicationConstants.MESSAGETYPE_DELETE, selection,
                            ApplicationConstants.SPACE_STRING, ApplicationConstants.SPACE_STRING);
                }

                // If keyHash >= mine and >= successor
                else if (mKeyHash.compareTo(mSucNodeId) > 0) {

                    // Send delete propagate message to successor
                    sendMessage(mSucPort, ApplicationConstants.MESSAGETYPE_DELETEPROPAGATE,
                            selection, ApplicationConstants.SPACE_STRING,
                            ApplicationConstants.SPACE_STRING);
                }
            }

            // If mKeyHash < me
            else if (mKeyHash.compareTo(mNodeId) < 0) {

                // If mKeyHash < me and mKeyHash >= my predecessor or
                // If my < my predecessor
                if (mKeyHash.compareTo(mPredNodeId) >= 0 || mNodeId.compareTo(mPredNodeId) <= 0) {
                    // Delete in local provider
                    deleteFromLocalContentProvider(selection);
                }

                // If mKeyHash < predecessor
                else if (mKeyHash.compareTo(mPredNodeId) < 0) {

                    // Send delete propagate message to predecessor
                    sendMessage(mPredPort, ApplicationConstants.MESSAGETYPE_DELETEPROPAGATE,
                            selection, ApplicationConstants.SPACE_STRING,
                            ApplicationConstants.SPACE_STRING);
                }
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
        if (selection.equals(ApplicationConstants.SYMBOL_AT)) {
            rowsDeleted = mDb.delete(ApplicationConstants.DB_TABLE_NAME,
                    ApplicationConstants.NUMBER_ONE, null);
        } else {
            rowsDeleted = mDb.delete(ApplicationConstants.DB_TABLE_NAME,
                    ApplicationConstants.COLUMN_KEY + "=?", new String[] {
                        selection
                    });
        }

        getContext().getContentResolver().notifyChange(mLocalUri, null);

        mDeleteCount += rowsDeleted;

        Log.e(ApplicationConstants.TAG,
                "Rows deleted at host " + myPort + " = " + String.valueOf(mDeleteCount));

        return rowsDeleted;
    }

    /**
     * The global query method which determines where a particular request
     * should go
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        Cursor mCursor = null;

        // If symbol is @ return all rows from local content provider
        if (selection.equalsIgnoreCase(ApplicationConstants.SYMBOL_AT)) {
            mCursor = queryLocalContentProvider(uri, null, ApplicationConstants.SYMBOL_AT, null,
                    null);
            return mCursor;
        }

        // Else if symbol is * we need to send request to the entire ring and
        // wait for everybody to respond
        else if (selection.equalsIgnoreCase(ApplicationConstants.SYMBOL_STAR)) {

            // Send a query all message to all the other nodes
            // Basically send the message with originator port and
            // stop passing the message when the successor is the originator

            // If my successor is me then it means I am alone
            // Return all rows from local content provider
            if (myPort.equalsIgnoreCase(mSucPort)) {
                mCursor = queryLocalContentProvider(uri, null, ApplicationConstants.SYMBOL_AT,
                        null, null);
                return mCursor;
            }

            // Else send query * message to the ring
            else {

                // Query my local content provider and put results in map
                globalCursorMap.putAll(cursorToMap(queryLocalContentProvider(uri, null,
                        ApplicationConstants.SYMBOL_AT, null, null)));

                // Propagate query star message to successor
                new SendMessageThread(mSucPort + ApplicationConstants.TILDE
                        + ApplicationConstants.MESSAGETYPE_QUERYSTAR + ApplicationConstants.PIPE
                        + myPort + ApplicationConstants.PIPE + ApplicationConstants.SPACE_STRING
                        + ApplicationConstants.PIPE + ApplicationConstants.SPACE_STRING);

                // Wait till the server task gets the query response from the
                // predecessor
                while (!isQueryStar) {

                }
                isQueryStar = false;
                // Return the cursor after converting from global cursor map
                mCursor = hashMapToCursor(globalCursorMap);
                globalCursorMap.clear();
                return mCursor;
            }
        }
        // Else a normal query where only 1 key needs to be searched in DHT
        else {

            String mKeyHash = null;
            try {
                mKeyHash = genHash(selection);
            } catch (NoSuchAlgorithmException e) {
                Log.e(ApplicationConstants.TAG, "Cannot generate hash value.");
            }

            // Check if this is the only node
            // If yes then local query
            if (myPort.equalsIgnoreCase(mSucPort) && myPort.equalsIgnoreCase(mPredPort)
                    && mNodeId.equalsIgnoreCase(mSucNodeId)
                    && mNodeId.equalsIgnoreCase(mPredNodeId)) {
                mCursor = queryLocalContentProvider(uri, null, selection, null, null);
                return mCursor;
            }

            // If key hash >= mine
            else if (mKeyHash.compareTo(mNodeId) >= 0) {

                // If keyHash >= mine and <= my successor or
                // If keyHash >= mine and mine >= my successor
                if (mKeyHash.compareTo(mSucNodeId) <= 0 || mNodeId.compareTo(mSucNodeId) >= 0) {

                    // Send query message to successor
                    if (null != selectionArgs && null != selectionArgs[0]
                            && !selectionArgs[0].trim().equalsIgnoreCase(
                                    ApplicationConstants.BLANK_STRING)) {
                        new SendMessageThread(mSucPort + ApplicationConstants.TILDE
                                + ApplicationConstants.MESSAGETYPE_QUERY
                                + ApplicationConstants.PIPE
                                + selectionArgs[0] + ApplicationConstants.PIPE + selection
                                + ApplicationConstants.PIPE + ApplicationConstants.SPACE_STRING);
                    } else {
                        new SendMessageThread(mSucPort + ApplicationConstants.TILDE
                                + ApplicationConstants.MESSAGETYPE_QUERY
                                + ApplicationConstants.PIPE
                                + myPort + ApplicationConstants.PIPE + selection
                                + ApplicationConstants.PIPE + ApplicationConstants.SPACE_STRING);
                    }

                    // If I am sending a query request to my next node and
                    // if I am not the request originator
                    // Then I do not need to wait
                    if (null == selectionArgs || null == selectionArgs[0]
                            || selectionArgs[0].trim().equalsIgnoreCase(
                                    ApplicationConstants.BLANK_STRING)) {
                        while (!isQuery) {

                        }
                        isQuery = false;
                        // Return cursor after converting from global cursor map
                        mCursor = hashMapToCursor(globalCursorMap);
                        globalCursorMap.clear();
                        return mCursor;
                    } else {
                        return null;
                    }

                }

                // If keyHash >= mine and >= successor
                else if (mKeyHash.compareTo(mSucNodeId) > 0) {

                    // Send query propagate message to successor
                    if (null != selectionArgs && null != selectionArgs[0]
                            && !selectionArgs[0].trim().equalsIgnoreCase(
                                    ApplicationConstants.BLANK_STRING)) {
                        new SendMessageThread(mSucPort + ApplicationConstants.TILDE
                                + ApplicationConstants.MESSAGETYPE_QUERYPROPAGATE
                                + ApplicationConstants.PIPE
                                + selectionArgs[0] + ApplicationConstants.PIPE + selection
                                + ApplicationConstants.PIPE + ApplicationConstants.SPACE_STRING);
                    } else {
                        new SendMessageThread(mSucPort + ApplicationConstants.TILDE
                                + ApplicationConstants.MESSAGETYPE_QUERYPROPAGATE
                                + ApplicationConstants.PIPE
                                + myPort + ApplicationConstants.PIPE + selection
                                + ApplicationConstants.PIPE + ApplicationConstants.SPACE_STRING);
                    }

                    // If I am sending a query request to my next node and
                    // if I am not the request originator
                    // Then I do not need to wait
                    if (null == selectionArgs || null == selectionArgs[0]
                            || selectionArgs[0].trim().equalsIgnoreCase(
                                    ApplicationConstants.BLANK_STRING)) {
                        while (!isQuery) {

                        }
                        isQuery = false;
                        // Return cursor after converting from global cursor map
                        mCursor = hashMapToCursor(globalCursorMap);
                        globalCursorMap.clear();
                        return mCursor;
                    } else {
                        return null;
                    }
                }
            }

            // If mKeyHash < me
            else if (mKeyHash.compareTo(mNodeId) < 0) {

                // If mKeyHash < me and mKeyHash >= my predecessor or
                // If my < my predecessor
                if (mKeyHash.compareTo(mPredNodeId) >= 0 || mNodeId.compareTo(mPredNodeId) <= 0) {
                    // Query in local provider
                    mCursor = queryLocalContentProvider(uri, null, selection, null, null);
                    return mCursor;
                }

                // If mKeyHash < predecessor
                else if (mKeyHash.compareTo(mPredNodeId) < 0) {

                    // Send query propagate message to predecessor
                    if (null != selectionArgs && null != selectionArgs[0]
                            && !selectionArgs[0].trim().equalsIgnoreCase(
                                    ApplicationConstants.BLANK_STRING)) {
                        new SendMessageThread(mPredPort + ApplicationConstants.TILDE
                                + ApplicationConstants.MESSAGETYPE_QUERYPROPAGATE
                                + ApplicationConstants.PIPE
                                + selectionArgs[0] + ApplicationConstants.PIPE + selection
                                + ApplicationConstants.PIPE + ApplicationConstants.SPACE_STRING);
                    } else {
                        new SendMessageThread(mPredPort + ApplicationConstants.TILDE
                                + ApplicationConstants.MESSAGETYPE_QUERYPROPAGATE
                                + ApplicationConstants.PIPE
                                + myPort + ApplicationConstants.PIPE + selection
                                + ApplicationConstants.PIPE + ApplicationConstants.SPACE_STRING);
                    }

                    // If I am sending a query request to my next node and
                    // if I am not the request originator
                    // Then I do not need to wait
                    if (null == selectionArgs || null == selectionArgs[0]
                            || selectionArgs[0].trim().equalsIgnoreCase(
                                    ApplicationConstants.BLANK_STRING)) {
                        while (!isQuery) {

                        }
                        // Return cursor after converting from global cursor map
                        mCursor = hashMapToCursor(globalCursorMap);
                        globalCursorMap.clear();
                        isQuery = false;
                        return mCursor;
                    } else {
                        return null;
                    }
                }
            }
        }
        return mCursor;
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
            String[] selectionArgs,
            String sortOrder) {

        SQLiteQueryBuilder mQueryBuilder = new SQLiteQueryBuilder();
        mQueryBuilder.setTables(ApplicationConstants.DB_TABLE_NAME);
        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

        Cursor mCursor = null;

        // If @ then retrieve all the rows of the local content provider
        if (selection.equalsIgnoreCase(ApplicationConstants.SYMBOL_AT)) {
            mCursor = mQueryBuilder.query(mDb, projection, null, null
                    , null, null, sortOrder);
        }

        // Select based on the provided key
        else {
            mCursor = mQueryBuilder.query(mDb, projection, ApplicationConstants.COLUMN_KEY
                    + "=?", new String[] {
                    selection
            }, null, null, sortOrder);
        }

        mCursor.setNotificationUri(getContext().getContentResolver(), uri);
        Log.v("query", selection);
        return mCursor;
    }

    /**
     * Standard function to convert hash map to cursor
     * 
     * @param cursorMap
     * @return
     */
    public MatrixCursor hashMapToCursor(Map<String, String> cursorMap) {

        MatrixCursor mCursor = new MatrixCursor(new String[] {
                ApplicationConstants.COLUMN_KEY, ApplicationConstants.COLUMN_VALUE
        });
        Iterator<Map.Entry<String, String>> mIter = cursorMap.entrySet().iterator();
        while (mIter.hasNext()) {
            Map.Entry<String, String> mEntry = mIter.next();
            mCursor.addRow(new String[] {
                    (String) mEntry.getKey(), (String) mEntry.getValue()
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
            mCursorMap.put(
                    cursor.getString(cursor.getColumnIndex(ApplicationConstants.COLUMN_KEY)),
                    cursor.getString(cursor.getColumnIndex(ApplicationConstants.COLUMN_VALUE)));
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
                msb.append(mCursor.getString(0) + ApplicationConstants.TILDE + mCursor.getString(1)
                        + ApplicationConstants.HASH);
            } while (mCursor.moveToNext());
        }

        if (msb.length() > 0) {
            msb.setLength(msb.length() - 1);
        }

        if (msb.toString().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
            return ApplicationConstants.SPACE_STRING;
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
                ApplicationConstants.COLUMN_KEY, ApplicationConstants.COLUMN_VALUE
        });

        if (null != mStr && !mStr.trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)
                && mStr.trim().length() > 0) {

            String[] msgs = mStr.split(ApplicationConstants.HASH);
            for (int i = 0; i < msgs.length; i++) {
                mCursor.addRow(new String[] {
                        msgs[i].split(ApplicationConstants.TILDE)[0],
                        msgs[i].split(ApplicationConstants.TILDE)[1]
                });
            }
        }

        return mCursor;
    }

    @Override
    public String getType(Uri uri) {

        return null;
    }

}
