package edu.buffalo.cse.cse486586.groupmessenger;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * Reference: SimpleMessengerActivity.java
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity implements ApplicationConstants {

    static int sSequencer = 0;
    String myPort;
    List<GroupMessage> mgmBuffer = new ArrayList<GroupMessage>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        
        final EditText editText = (EditText) findViewById(R.id.editText1);
        
        findViewById(R.id.button4).setOnClickListener(new OnClickListener() {        
            @Override
            public void onClick(View v) {
               
                String msg = editText.getText().toString() + NEWLINE_STRING;
                editText.setText(BLANK_STRING);
               
                new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg, myPort, REMOTE_PORT0, "-1", "false");
                new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg, myPort, REMOTE_PORT1, "-1", "false");
                new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg, myPort, REMOTE_PORT2, "-1", "false");
                new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg, myPort, REMOTE_PORT3, "-1", "false");
                new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg, myPort, REMOTE_PORT4, "-1", "false");
            }
        });
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            
             try {
                while(null != serverSocket) {
                    Socket s = serverSocket.accept();
                    
                    GroupMessage mgm = null;
                    Object mObj = new ObjectInputStream(s.getInputStream()).readObject();
                    if(mObj instanceof GroupMessage) {
                        mgm = (GroupMessage) mObj;
                    }
                    
                    if(null != mgm) {
                        if(!mgm.ismIsSeqMsg()) {
                            mgmBuffer.add(mgm);
                            if(myPort.equalsIgnoreCase(REMOTE_PORT4)) {
                                new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mgm.getMsg(), mgm.getmPortNo(), REMOTE_PORT0, Integer.toString(sSequencer), "true");
                                new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mgm.getMsg(), mgm.getmPortNo(), REMOTE_PORT1, Integer.toString(sSequencer), "true");
                                new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mgm.getMsg(), mgm.getmPortNo(), REMOTE_PORT2, Integer.toString(sSequencer), "true");
                                new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mgm.getMsg(), mgm.getmPortNo(), REMOTE_PORT3, Integer.toString(sSequencer), "true");
                                new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mgm.getMsg(), mgm.getmPortNo(), REMOTE_PORT4, Integer.toString(sSequencer++), "true");
                            }
                        } else {
                            for(int i = 0; i < mgmBuffer.size(); i++) {
                                if(mgm.getmPortNo().equals(mgmBuffer.get(i).getmPortNo())) {
                                    persistMsgToContentProvider(mgm);
                                    mgmBuffer.remove(i);
                                    break;
                                }
                            }
                            publishProgress(mgm.getMsg());
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Can't accept connection to a ServerSocket from the Client");
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Error faced while deserializing object.\n");
            }
            return null;
        }

        void persistMsgToContentProvider(GroupMessage mgm){
            final ContentResolver mContentResolver = getContentResolver();
            final ContentValues mContentValues = new ContentValues();
            final Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger.provider");
            mContentValues.put(COLUMN_KEY, mgm.getmSeqNo() );
            mContentValues.put(COLUMN_VALUE, mgm.getMsg());
            mContentResolver.insert(mUri, mContentValues);
        }
        
        private Uri buildUri(String scheme, String authority) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }
        
        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView mTextView = (TextView) findViewById(R.id.textView1);
            mTextView.append(strReceived + "\t\n");

            return;
        }
    }
    
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(msgs[2]));
                
                if(null != msgs[0] && !msgs[0].trim().equalsIgnoreCase(BLANK_STRING)) {
                    new ObjectOutputStream(socket.getOutputStream())
                            .writeObject(new GroupMessage(msgs[0], msgs[1], msgs[2], msgs[3], msgs[4]));
                }
                
                socket.close();
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }
}
