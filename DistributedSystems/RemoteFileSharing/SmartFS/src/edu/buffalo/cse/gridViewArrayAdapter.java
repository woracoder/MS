
package edu.buffalo.cse;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

public class gridViewArrayAdapter extends ArrayAdapter<ObjectItem> {

    Context mContext;

    int layoutResourceId;

    ObjectItem data[] = null;

    static final String REMOTE_PORT0 = "11108";

    static final String REMOTE_PORT1 = "11112";

    static final int SERVER_PORT = 10000;

    static final String TAG = MainActivity.class.getSimpleName();

    String myPort;

    AtomicBoolean receivedProgress;

    String metaDataString = "";

    String nodeName = "";

    View lastClickedView;

    Dialog mydialog;

    public gridViewArrayAdapter(Context context, int itemViewId, ObjectItem[] objects) {
        super(context, itemViewId, objects);
        this.layoutResourceId = itemViewId;
        this.mContext = context;
        this.data = objects;
        this.receivedProgress = new AtomicBoolean();

    }

    public gridViewArrayAdapter(Context context, int resource, int textViewResourceId,
            ObjectItem[] objects) {

        super(context, resource, textViewResourceId, objects);
        this.layoutResourceId = resource;
        this.mContext = context;
        this.data = objects;
        // TODO Auto-generated constructor stub

    }

    // public ArrayAdapterItem(Context context, int resource, int
    // ObjectItem[] objects) {
    // super(context, resource, objects);
    // // TODO Auto-generated constructor stub
    // }

    public View getView(int position, View convertView, ViewGroup parent) {


        
        if (convertView == null) {
            // inflate the layout
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }
        TextView item = (TextView)convertView.findViewById(R.id.directoryItem);
        ObjectItem itemdata = data[position];
        item.setText(itemdata.name);
        item.setTag(itemdata.Id);
        ImageView iconImg = (ImageView)convertView.findViewById(R.id.iconImg);
        
        if (itemdata.type.equals("file")) {
            iconImg.setImageResource(R.drawable.file_icon);
            Log.e("message", "changing icon to fileicon");
            
            iconImg.setOnClickListener(new View.OnClickListener() {
                
                //In this function call Abhis processDownloadRequest
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    //Waiting screen starts
                    DirectoryManager mgr = (DirectoryManager)((GridView)(v.getParent()).getParent())
                            .getTag();
                    
                    String fileName = ((TextView)((LinearLayout)(v.getParent())).getChildAt(1))
                            .getText().toString();
                    
                    
                    String downloadString = mgr.currentPath + "/"+fileName;
                    String myPort =String.valueOf( Constants.EMULATOR_SERVER_PORT);
                    String filePath;
                    String filePathArray[];
                    int filePathPrefixLength;
                    String downloadRequest;
                    
                    Log.e("Test3","Ip: "+mgr.currentNodeIp+" port:" + myPort + "fileName: "+ downloadString); 
                    filePathArray = downloadString.split("\\/");
                    filePathPrefixLength = filePathArray[1].length();
                    filePath=downloadString.substring(filePathPrefixLength + 1);
                    downloadRequest = Constants.DOWNLOADPROTOCOL + Constants.PIPEDELIMITER + filePath ; 
                    new ClientThread(mgr.currentNodeIp,myPort,downloadRequest.getBytes());
                    //Registration.processDownloadRequest(null, mgr.currentNodeIp, myPort, downloadString);
                    //After completion stop the waiting screen
                }
            });

        } 
        
        else if (itemdata.type.equals("directory")) {
            Log.e("message", "trying to set directory on click");
            iconImg.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    DirectoryManager mgr = (DirectoryManager)((GridView)(v.getParent()).getParent())
                            .getTag();
                    String folderName = ((TextView)((LinearLayout)(v.getParent())).getChildAt(1))
                            .getText().toString();
                    try {
                        mgr.gotoNextLevel(folderName);
                    } catch (XPathExpressionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    ObjectItem ObjList[] = new ObjectItem[] {};
                    try {
                        ObjList = mgr.getCurrentLevelList();
                    } catch (XPathExpressionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    gridViewArrayAdapter gridSource = new gridViewArrayAdapter(v.getContext(),
                            R.layout.grid_item_view, ObjList);

                    ((GridView)(v.getParent()).getParent()).setAdapter(gridSource);

                }
            });

        } else if (itemdata.type.equals("Node")) {
            Log.e("message", "type: Node");
            iconImg.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    Log.e("message", "progress bar");
                    mydialog = new Dialog(v.getContext(), 0);
                    mydialog.addContentView(new ProgressBar(v.getContext()), new LayoutParams(40,
                            40));

                    mydialog.show();
//                    receivedProgress.set(false);

//                    String myPort = ((DirectoryManager)((GridView)(v.getParent()).getParent())
//                            .getTag()).myPort;
                    
                    String myPort =String.valueOf( Constants.EMULATOR_SERVER_PORT);

                    DirectoryManager mgr = (DirectoryManager)((GridView)(v.getParent()).getParent())
                            .getTag();

                    String folderName = ((TextView)((LinearLayout)(v.getParent())).getChildAt(1))
                            .getText().toString();

                    // Get the IP here & pass through client task ask Sujay
                    // String ipAdd
                    String nodeIp= null;
                    try {
                        nodeIp = mgr.getIP(folderName);
                    } catch (XPathExpressionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                                        
                    nodeName = folderName;
                    lastClickedView = v;

                    String s = "Connect";
                    Log.e("Message","Folder name:"+folderName + "ip:"+ nodeIp+" port:"+myPort);
                    metaDataString = "";
                    receivedProgress.set(true);
                    new ClientThread(nodeIp, myPort, s.getBytes());
                                        
                    //new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "IP", myPort, "Connect");
                      
                     while (receivedProgress.get())
                     {
                    
                     }

                     mydialog.dismiss();
//                     updateMetadata();
                     /******************************************************/
                     //DirectoryManager mgr = (DirectoryManager)((GridView)(lastClickedView.getParent()).getParent()).getTag();
                     Node n = null;
                     DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                     DocumentBuilder builder;
                     Document doc;
                     try {
                         
                         builder = docFactory.newDocumentBuilder();
                      /* Log.e("message", "Metadata string:" + metaDataString);*/
                         metaDataString = "<NodeElement type=\"Node\" ip=\""+nodeIp+"\" Name=\""
                                 + nodeName + "\">" + metaDataString + "</NodeElement>";
                         doc = builder.parse(new InputSource(new StringReader(metaDataString)));
                         n = doc.getDocumentElement();
                       //  Log.e("Message",n.toString());
                         mgr.updateFolder(n, nodeName);

                         if(!mgr.gotoNextLevel(nodeName)) 
                             Log.e("message","unable to go to directory:" + nodeName);

                         ObjectItem ObjList[] = new ObjectItem[] {};

                         
                         ObjList = mgr.getCurrentLevelList();

                     //    Log.e("message","new directory list size:"+String.valueOf(ObjList.length));
                         
                         gridViewArrayAdapter gridSource = new gridViewArrayAdapter(
                                 v.getContext(), R.layout.grid_item_view, ObjList);

                         ((GridView)(v.getParent()).getParent()).setAdapter(gridSource);

                     } catch (ParserConfigurationException e1) {
                         // TODO Auto-generated catch block
                         e1.printStackTrace();
                     } catch (SAXException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     } catch (IOException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     } catch (XPathExpressionException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }

                     
                     
                     /******************************************************/

                    // mydialog.dismiss();
                    // DirectoryManager mgr =
                    // (DirectoryManager)((GridView)(v.getParent()).getParent()).getTag();
                    // String folderName =
                    // ((TextView)((LinearLayout)(v.getParent())).getChildAt(1)).getText().toString();
                    // try {
                    // mgr.gotoNextLevel(folderName);
                    // } catch (XPathExpressionException e) {
                    // // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                    // ObjectItem ObjList[]= new ObjectItem[]{};
                    // try {
                    // ObjList = mgr.getCurrentLevelList();
                    // } catch (XPathExpressionException e) {
                    // // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                    // gridViewArrayAdapter gridSource = new
                    // gridViewArrayAdapter(v.getContext(),R.layout.grid_item_view,ObjList);
                    //
                    // ((GridView)(v.getParent()).getParent()).setAdapter(gridSource);

                }
            });

        } else {
            Log.e("message", "unknown type:" + itemdata.type);
        }
        return convertView;
    }

    void updateMetadata() {
       
        if(lastClickedView == null)
        {
            Log.e("Error", "Lastclickedview null");
        }
        else
        {
            LinearLayout ll = (LinearLayout)lastClickedView.getParent();
            if(ll == null)
            {
                Log.e("Error", "ll null");
            }
            else
            {
                GridView gv = (GridView)ll.getParent();
                if(gv == null)
                {
                    Log.e("Error", "gridview null");    
                }
                DirectoryManager dm = (DirectoryManager)gv.getTag();
                if(dm == null)
                {
                    Log.e("Error", "directory manager null");
                }
            }
       
        }
        
        DirectoryManager mgr = (DirectoryManager)((GridView)(lastClickedView.getParent()).getParent()).getTag();
        Node n = null;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        try {
            
            builder = docFactory.newDocumentBuilder();
            Log.e("message", "Metadata string:" + metaDataString);
            metaDataString = "<NodeElement type=\"directory\" ip=\"10.21.32.140\" Name=\""
                    + nodeName + "\">" + metaDataString + "</NodeElement>";
            doc = builder.parse(new InputSource(new StringReader(metaDataString)));
            n = doc.getDocumentElement();
            mgr.updateFolder(n, nodeName);

            mgr.gotoNextLevel(nodeName);

            ObjectItem ObjList[] = new ObjectItem[] {};

            ObjList = mgr.getCurrentLevelList();

            gridViewArrayAdapter gridSource = new gridViewArrayAdapter(
                    lastClickedView.getContext(), R.layout.grid_item_view, ObjList);

            ((GridView)(lastClickedView.getParent()).getParent()).setAdapter(gridSource);

        } catch (ParserConfigurationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // String folderName =
        // ((TextView)((LinearLayout)(v.getParent())).getChildAt(1)).getText().toString();

    }

    private class ClientTask extends AsyncTask<String, String, Void> {

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
                pw.println(msgs[2]);
                pw.close();
                socket.close();
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }

        // @Override
        // protected void onProgressUpdate(Void... values) {
        // // TODO Auto-generated method stub
        // super.onProgressUpdate(values);
        // metaDataString =values[0];
        //
        // }
        @Override
        protected void onProgressUpdate(String... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            metaDataString = values[0];
            mydialog.dismiss();
            Log.e(TAG, "Received new metadata.");
        }

    }

}
