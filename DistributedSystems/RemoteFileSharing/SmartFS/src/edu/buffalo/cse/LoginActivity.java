
package edu.buffalo.cse;

import java.net.InetAddress;
import java.net.Socket;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.content.Context;
import android.telephony.TelephonyManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import android.util.Log;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
    

    // Values for email and password at the time of the login attempt.
    private String mUsername;
    private String mPassword;

    // UI References.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;
    
    static String  myIP = "10.0.2.2";
    static String myPort ;
    EditText editText1;
    ContentResolver cr;
    static Uri mUri;
    static ContentValues cv;
    ArrayList<String> msgList;
    ServerSocket emulatorServerSocket;
    String msg;
    static String portStr;
    static ArrayList<Node> serverList;
    static ArrayList<Node> connectionList;
    static Socket serverSocket;
    static LoginActivity currentloginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Step 1 : Set the view
        setContentView(R.layout.activity_login); 
        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        currentloginActivity=this;
        
        //Step 2 : Get myIP and my port
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        myIP = Registration.getIPAddress(true);
        
        //Step 3: Instantiate serverList and ConnectionList
        serverList     =  new ArrayList<Node>();
        connectionList =  new ArrayList<Node>();
        
        //Step 4 : Create the server socket
        try{        
            emulatorServerSocket = new ServerSocket(Constants.EMULATOR_SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, emulatorServerSocket);
        }
        catch(IOException e){

        }
                
        //Step 5: Event method on editing 
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        //Step 6 : Set other view and event on clicking sign in button
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
                Log.e(LoginActivity.class.getSimpleName(),"fghi");
            }
        });
        
        
        //Step 7 : Testing code begins
        //Registration.SimulateNewServerList();
        //Registration.setLoginActivity(this);
        //new DownloadClientThreadForTesting();                      
       /* try{
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.txt");
        f.createNewFile();
        PrintWriter pw =new PrintWriter(f);
        pw.write("Hello this is my test file.  \n");
        pw.close();
        }
        catch(Exception e){
            Log.e(Constants.TAG,"file not created");
        }
        
        Registration.uploadFile(new Socket(),Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.txt");*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
       
        // Step 1: Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Step 2:Store values at the time of the login attempt.
        mUsername = mUsernameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Step 3:Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Step 4:Check for a valid username
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } 
        
        //Step 5 : Check if there is an error else invoke client thread
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            //Step 6 : Create a register message and send it to the server
            msg = Constants.REGISTERPROTOCOL + Constants.PIPEDELIMITER + mUsername + Constants.PIPEDELIMITER +  mPassword + Constants.PIPEDELIMITER  + myIP + Constants.PIPEDELIMITER + myPort ;
                        
            try {                
                // Step 7 : create a socket                
                new ClientThread(Constants.SERVERIP,Constants.SERVER_PORT,msg.getBytes());
            }
            catch(Exception e){
                Log.e(Constants.TAG, e.toString());
            }                                                
            
            //Step 9 : Show message that login is in progress
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);            
            Log.e(LoginActivity.class.getSimpleName(),"Login Message sent to the server \n");
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    
                       
    public void displayInvalidCredentials() {         
        showProgress(false);
        mPasswordView.setError(getString(R.string.error_incorrect_password));
        mPasswordView.requestFocus();   
    }
        

    private class DownloadClientThreadForTesting implements Runnable {

        Thread t;        
        Socket socket;

        DownloadClientThreadForTesting() {
            t = new Thread(this);
            t.start();
        }
        
        
        public void run() {

            try {                
                socket = new Socket(InetAddress.getByName(Constants.SERVERIP), Integer.parseInt(Constants.SERVER_PORT));
                Registration.processDownloadRequest(socket ,"","","deepika.jpg"); 
                new ProcessSocketThread(socket);
            }  catch (Exception e) {
                Log.e(Constants.TAG, "Emulator 5554 is not available. Cannot join \n");

            }
        }
    }
       
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {

            ServerSocket emulatorServerSocket = sockets[0];
            Socket clientSocket;

            try{
                while(true)
                {
                    //Step 1 : Accept the connection
                    clientSocket =emulatorServerSocket.accept();
                    
                    //Step 2 : process the client socket
                    new ProcessSocketThread(clientSocket);
                                                    
                }
        }
        catch(Exception e){
            Log.e(Constants.TAG, "Eception Occured" + e.toString());

        }
        return null;
    }
                
}

    
    
    
}
