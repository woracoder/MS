
package edu.buffalo.cse;

import android.support.v4.app.FragmentActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import javax.xml.xpath.XPathExpressionException;

public class MainActivity extends FragmentActivity {
    
    static final String TAG = MainActivity.class.getSimpleName();

    static final String REMOTE_PORT0 = "11108";

    static final String REMOTE_PORT1 = "11112";

    static final int SERVER_PORT = 10000;

    static final String BLANK_STRING = "";

    String myPort;

    //StringBuilder sb = new StringBuilder();

    public gridViewArrayAdapter gridSource;

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Intent i = getIntent();
        String s = i.getStringExtra("RegisterAction");
        
        /* ***************************************************************************************/
        
        TelephonyManager tel = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        
        Registration.storeMainActivityReference(this);
     
        
        if (isExternalStorageWritable()) {

            // Create folders SmartFsSharedFiles, SmartFsDownloads if not existing
            if (!new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SmartFSSharedFiles").mkdir()) {
                if (new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SmartFSSharedFiles").exists()) {
                    Log.e(TAG, "Directory SmartFSSharedFiles not created as it already exists");
                } else {
                    Log.e(TAG, "Directory SmartFSSharedFiles not created due to internal problem");
                }
            }

            if (!new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SmartFSDownloads").mkdir()) {
                if (new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SmartFSDownloads").exists()) {
                    Log.e(TAG, "Directory SmartFSDownloads not created as it already exists");
                } else {
                    Log.e(TAG, "Directory SmartFSDownloads not created due to internal problem");
                }
            }
            
        } else {
            Log.e(TAG, "SD card is not mounted.");
        }
        
        GridView gv =(GridView)findViewById(R.id.vw_grid);
        
//        ObjectItem ObjList[]= new ObjectItem[6];// = {new ObjectItem("fileabc", 1,"file"),new ObjectItem("folder2.1", 2,"directory")}; 
//     
//        for(int k = 0; k < 2;k++)
//        {
//            ObjList[k*3]=new ObjectItem("Folder_"+String.valueOf(k*3),k*3,"directory");
//            ObjList[k*3+1]=new ObjectItem("Folder_"+String.valueOf(k*3+1),k*3+1,"directory");
//            ObjList[k*3+2]=new ObjectItem("Folder_"+String.valueOf(k*3+2),k*3+2,"file");
//        }
        AssetManager assetMgr = getAssets();
        InputStream strm=null;
        // strm = assetMgr.open( "Local.xml" );
        strm = new ByteArrayInputStream(s.getBytes());
        DirectoryManager mgr = new DirectoryManager(strm, myPort);
        
        ObjectItem ObjList[] = null;
        try {
            ObjList = mgr.getCurrentLevelList();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        
        Button backbtn = (Button)findViewById(R.id.btn_back);
        
        backbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                GridView gv =(GridView)findViewById(R.id.vw_grid);
                if(((DirectoryManager)gv.getTag()).gotoPreviousLevel())
                {
                    try {
                        gridSource = new gridViewArrayAdapter(arg0.getContext(),R.layout.grid_item_view,((DirectoryManager)gv.getTag()).getCurrentLevelList());
                        gv.setAdapter(gridSource);
                    } catch (XPathExpressionException e) {
                        e.printStackTrace();
                    }
                }
            }});
        
        
        gridSource = new gridViewArrayAdapter(this,R.layout.grid_item_view,ObjList);
        gv.setTag(mgr);
        gv.setAdapter(gridSource);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public class ShowPeerMetadata implements Runnable{
        
        Thread t;
        String msb;
        ShowPeerMetadata(String msb){
            
            t= new Thread(this);
            this.msb=msb;
            t.start();
        }
        
        public void run(){
            
      //      Log.e("message","setting metadata to :"+ msb);
            gridSource.metaDataString = msb;
            //gridSource.metaDataString = strings[0];
            gridSource.receivedProgress.set(false);
//            gridSource.mydialog.dismiss();
//            gridSource.updateMetadata();
                               
        }
    }

    /*private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            try {
                while (null != serverSocket) {
                    Socket s = serverSocket.accept();
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String command = "";
                    Log.e(TAG,"step1");
                    if ((command = br.readLine()).equalsIgnoreCase("connect")) {
                        Log.e(TAG,"step2");
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "IP", myPort, "metadata\n" + generateMetadataXml());
                        Log.e(TAG, "served the request");
                    } else if (command.equalsIgnoreCase("metadata")) {
                        Log.e(TAG,"step3");
                     //   FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/metaData.txt"));
                        StringBuilder msb = new StringBuilder();
                        while(null != (command = br.readLine())) {
                            msb.append(command + "\n");
                        }
                       // fos.write(msb.toString().getBytes());
                        //fos.close();
                        
                        //Log.e(TAG,msb.toString());
                        
                        publishProgress(msb.toString());
                        Log.e(TAG,"received metadata");
                        
                    }
                    Log.e(TAG,"step4");
                    br.close();
                    
                }
            } catch (IOException e) {
                Log.e(TAG, "Can't accept connection to a ServerSocket from the Client");
            }
            return null;
        }
        
        @Override
        protected void onProgressUpdate(String... strings) {
            
             * The following code displays what is received in doInBackground().
             
            Log.e(TAG, "Inside onProgressUpdate");
            gridSource.metaDataString = strings[0];
            gridSource.receivedProgress.set(false);
                        
            return;
        }
        
    }*/
    
    

    /*private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String remotePort = REMOTE_PORT0;
                if (msgs[1].equals(REMOTE_PORT0))
                    remotePort = REMOTE_PORT1;

                Socket socket = new Socket(InetAddress.getByAddress(new byte[] {
                        10, 0, 2, 2
                }), Integer.parseInt(remotePort));

                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                pw.println(msgs[2].getBytes());
                pw.close();

                socket.close();
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }*/

    /*private String generateMetadataXml() throws IOException {
        sb.append("<NodeElement type=\"directory\" ip=\"TODO\" Name=\"TODO\">\n");
        traverseDirectoryStructure(new File(Environment.getExternalStorageDirectory() + "/SmartFSSharedFiles"));
        traverseDirectoryStructure(new File(Environment.getExternalStorageDirectory() + "/"));
        sb.append("</NodeElement>\n");
        return sb.toString();
    }

    private void traverseDirectoryStructure(File mf) {
        File fileList[] = mf.listFiles();
        if (null != fileList) {
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    sb.append("<NodeElement type=\"folder\" Name=\"" + fileList[i].getName() + "\">\n");
                    traverseDirectoryStructure(fileList[i]);
                    sb.append("</NodeElement>\n");
                } else {
                    sb.append("<NodeElement type=\"file\" Name=\"" + fileList[i].getName() + "\"></NodeElement>\n");
                }
            }
        }
    }*/
    
}
