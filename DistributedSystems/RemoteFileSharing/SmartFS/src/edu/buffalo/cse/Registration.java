package edu.buffalo.cse;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import org.apache.http.conn.util.InetAddressUtils;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Registration {
    
    static private LoginActivity loginActivity;
    public static LoginActivity getLoginActivity() {
        return loginActivity;
    }

    public static void setLoginActivity(LoginActivity loginActivity) {
        Registration.loginActivity = loginActivity;
    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(MainActivity mainActivity) {
        Registration.mainActivity = mainActivity;
    }

    static private MainActivity mainActivity;
    
    
    static void storeLoginActivityReference(LoginActivity localLoginActivity){
        
        loginActivity=localLoginActivity;      
    }
    
    static void storeMainActivityReference(MainActivity localMainActivity){
        
        mainActivity=localMainActivity;      
    }
    
    static void prepareServerListXmlString(){
        
        String rootHeaderXml ="<root>\n";
        String nodeElementIPXml = "<NodeElement type=\"Node\" ip=\"";
        String nameXml = "\" Name=\"";
        String portXml = "\" port=\"";
        String nodeElementTrailerXml = "\">\n</NodeElement>\n";
        String rootTrailerXml = "</root>";
        String outputXml;
        
        //Step 1 : Add the root element to the rootXml Header
        outputXml = rootHeaderXml;
        
        //Step 2 : Add the Node Elements
        for(Node n : LoginActivity.serverList){
            
            if(!n.IPAddress.equalsIgnoreCase(LoginActivity.myIP)){
            outputXml = outputXml + nodeElementIPXml + n.getIPAddress() + nameXml + n.getUserName() + portXml + n.getPortNumber() + nodeElementTrailerXml;
            }
        }
        
        //Step 3 : Appending the  root element closure
        outputXml = outputXml + rootTrailerXml; 
        
        Intent i = new Intent(LoginActivity.currentloginActivity, MainActivity.class);
        i.putExtra("RegisterAction", outputXml);
        LoginActivity.currentloginActivity.startActivity(i);
      
    }
    
    static void displayServerList(){
        
        //TextView localTextView = (TextView) findViewById(R.id.textView1);
        Log.e(Constants.TAG ,"\nAVAILABLE PEER LIST");
        //localTextView.append("AVAILABLE PEER LIST\n");
        for(Node n : LoginActivity.serverList){
        Log.e(Constants.TAG ,"IP Address:  " + n.getIPAddress() + "   Port Number: " + n.getPortNumber());
        //localTextView.append("IP Address:  " + n.getIPAddress() + "   Port Number: " + n.getPortNumber() + "\n");
        }              
    }
    
    static void displayConnectionList(){
        
        //TextView localTextView = (TextView) findViewById(R.id.textView1);
        Log.e(Constants.TAG ,"\nAVAILABLE PEER LIST");
        //localTextView.append("AVAILABLE PEER LIST\n");
        for(Node n : LoginActivity.connectionList){
        Log.e(Constants.TAG ,"IP Address:  " + n.getIPAddress() + "   Port Number: " + n.getPortNumber());
        //localTextView.append("IP Address:  " + n.getIPAddress() + "   Port Number: " + n.getPortNumber() + "\n");
        }              
    }
       
    static void createNewServerList(String messageRead){
        
        String messageReadArray[];
        Node n , newNode;
        int i;
        
        //Step 1 : Erase the current server List
        LoginActivity.serverList.clear();
        
        //Step 2 : Split the message by the delimiter
        messageReadArray = messageRead.split("\\|");
        
        //Step 3 : Add the nodes in the server List
        for(i=1;i<messageReadArray.length;i+=3){
            
            n = new Node();
            n.setUserName(messageReadArray[i]);
            n.setIPAddress(messageReadArray[i+1]);
            n.setPortNumber(messageReadArray[i+2]);
            LoginActivity.serverList.add(n);
        }  
        
        //Step 4 : If the conection List is empty add the server connection to it
        if(LoginActivity.connectionList.isEmpty()){
            
            //Step 5 : create a new node
            newNode = new Node();
            
            //Step 6 : Assign IP address , port no. and socket to it and add it to the server List
            newNode.setUserName(Constants.SERVERUSERNAME);
            newNode.setIPAddress(Constants.SERVERIP);
            newNode.setPortNumber(Constants.SERVER_PORT);
            newNode.setSocket(LoginActivity.serverSocket);
            LoginActivity.connectionList.add(newNode);
        }
    } 
            
    
    static void uploadFile(Socket socket, String fileName){
        
        RandomAccessFile rf;
        File file;
        String msg;
        long fileLength;
        long numberOfBytesRemaining;
        long bytesRead=0;
        byte dataBuffer[] ;
        int bytesReadInLastBlock;
        boolean firstBlock=true;
        String blockType;
        int storeProtocolHeaderLength;
        int storeProtocolDataSize;
        byte storeProtocolHeader[];
        byte headerAndDataBuffer[];
        int i,j;
        
        try{
        
        //Step : Create buffered stream
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        
        //Step 1 : Check if the file exists
        file = new File(Environment.getExternalStorageDirectory() + fileName);
        //String pth = file.getAbsolutePath();
        if(!file.isFile()){
            //msg = Constants.INVALIDFILE + Constants.PIPEDELIMITER + fileName  + Constants.PIPEDELIMITER;
            msg = Constants.INVALIDFILE + Constants.PIPEDELIMITER + fileName ;
            new ClientThread(socket,msg.getBytes());
            
        }
        else{
            
            //Step 2 : Open the file
            rf =new RandomAccessFile(Environment.getExternalStorageDirectory() + fileName,"r");
        
            //Step 3 : Get the length of the file
            fileLength=file.length();
            
            //Step 4: Initialize numberOfBytesRemaining and bytesRead
            bytesRead=0;
            numberOfBytesRemaining=fileLength;
            
            //Step 5 : Determine the header length
            storeProtocolHeaderLength = Constants.STOREPROTOCOL.length() + Constants.PIPEDELIMITER.length() + fileName.length() + Constants.PIPEDELIMITER.length() + Constants.FIRSTFILEBLOCK.length() + Constants.PIPEDELIMITER.length();     
            storeProtocolDataSize = Constants.BUFFER_SIZE - storeProtocolHeaderLength;

            
            //Step 6 : Read the file contents and send it
            while(numberOfBytesRemaining>0){
                if(storeProtocolDataSize < numberOfBytesRemaining){
                    
                    //Step 7 : Determine the size of data buffer
                    dataBuffer = new byte[storeProtocolDataSize];
            
                    //Step 8 : Set the position and read from the file
                    rf.seek(bytesRead);
                    rf.read(dataBuffer);
                
                    //Step 9 : Set numberOfBytesRemaining and bytesRead
                    bytesRead+=storeProtocolDataSize;
                    numberOfBytesRemaining-=storeProtocolDataSize;
                    
                    //Step 10 : Determine the block Type
                    if(firstBlock){
                        blockType=Constants.FIRSTFILEBLOCK;
                        firstBlock=false;
                    }
                    else{
                        blockType=Constants.INTERMEDIATEFILEBLOCK;
                    }
                
                    //Step 11 : Create a message and send it
                    msg = Constants.STOREPROTOCOL + Constants.PIPEDELIMITER + fileName + Constants.PIPEDELIMITER + blockType + Constants.PIPEDELIMITER ;
                    storeProtocolHeader=msg.getBytes();
                    
                    //Step 12 : Instantiate the  headerAndDataBuffer which consists of both data and header
                    headerAndDataBuffer = new byte[Constants.BUFFER_SIZE];
                    for(i=0;i<storeProtocolHeader.length;i++){
                        headerAndDataBuffer[i] = storeProtocolHeader[i];
                    }
                    for(j=0;j< dataBuffer.length ;j++,i++){
                        headerAndDataBuffer[i] = dataBuffer[j];
                    }
                    
                    //Step 13 : Send the message
                    out.write(headerAndDataBuffer);
                    out.flush();
                }
                else{
                    
                    //Step 14 : Determine the size of data buffer
                    dataBuffer = new byte[(int)numberOfBytesRemaining];
                                        
                    //Step 15 : Set the position and read from the file
                    rf.seek(bytesRead);
                    bytesReadInLastBlock=rf.read(dataBuffer);
                    
                    //Step 16 : Set numberOfBytesRemaining and bytesRead
                    bytesRead+=bytesReadInLastBlock;
                    numberOfBytesRemaining-=bytesReadInLastBlock;
                    
                    //Step 17 : Determine the block Type
                    if(firstBlock){
                        blockType=Constants.SINGLEFILEBLOCK;
                        firstBlock=false;
                    }
                    else{
                        blockType=Constants.LASTFILEBLOCK;
                    }
                    
                    //Step 18 : Create a message and send it
                    msg = Constants.STOREPROTOCOL + Constants.PIPEDELIMITER + fileName + Constants.PIPEDELIMITER + blockType + Constants.PIPEDELIMITER ;
                    storeProtocolHeader=msg.getBytes();
                    
                    //Step 19 : Read from the buffer which consists of both data and header
                    headerAndDataBuffer = new byte[storeProtocolHeaderLength + bytesReadInLastBlock];
                    for(i=0;i<storeProtocolHeader.length;i++){
                        headerAndDataBuffer[i] = storeProtocolHeader[i];
                    }
                    for(j=0;j< dataBuffer.length ;j++,i++){
                        headerAndDataBuffer[i] = dataBuffer[j];
                    }
                    
                    //Step 20 : send the message
                    out.write(headerAndDataBuffer);
                    out.flush();
            
                    }
            }
        }
        
        }
        catch(Exception e){
            Log.e(Constants.TAG,"Error in opening file \n");
        }
    }
    
    static void displayInvalidFile(){
        
    }
    
    static void storeFile(byte[] buffer, int numberOfBytesRead){
        
        RandomAccessFile rf;
        String messageReadArray[];
        long fileLength;
        String bufferString;
        int  bufferOffset;
        byte[] messageBuffer;
        
        //Step 1 : Get the buffer consisting of only bytes read
        messageBuffer = Arrays.copyOfRange(buffer,0,numberOfBytesRead);
        
        //Step 1 : Create a buffer string
        bufferString = new String(messageBuffer);
        
        //Step 2 : Split the buffer by | delimiter
        messageReadArray = bufferString.split("\\|");
        
        try{
        
            //Step 3 : Initialize the random access file
            rf = new RandomAccessFile(Environment.getExternalStorageDirectory() + "/SmartFSDownloads/" + messageReadArray[1],"rw");
        
            //Step 4 : count the buffer offset
            bufferOffset = messageReadArray[0].length() + Constants.PIPEDELIMITER.length() + messageReadArray[1].length() + Constants.PIPEDELIMITER.length() + messageReadArray[2].length() + Constants.PIPEDELIMITER.length(); 
        
            //Step 5: Check if we need to create a new file or write into an existing file. If it's a new file do the following
            if(messageReadArray[2].equals(Constants.FIRSTFILEBLOCK) || messageReadArray[2].equals(Constants.SINGLEFILEBLOCK)){
            
                rf.write(buffer,bufferOffset ,numberOfBytesRead -bufferOffset);
            }
            else{
            
                //Step 6  : Get the length of the file
                fileLength=rf.length();
            
                //Step 7 : Go to the end of the file
                rf.seek(fileLength);
                rf.write(buffer, bufferOffset, numberOfBytesRead -bufferOffset);                        
            }
            
            //Step 8 : Print the file has been written successfully if the last block was written
            if(messageReadArray[2].equals(Constants.SINGLEFILEBLOCK) || messageReadArray[2].equals(Constants.LASTFILEBLOCK)){
                
                Log.e(Constants.TAG,"File Downloaded Successfully \n");
                new DownloadCompletedDialog().show(mainActivity.getFragmentManager(), "DownloadCompletedDialog"); //Dialog Changes
            }
            
            //Step 9 : Close the Random Access file
            rf.close();
        }
        catch(Exception e){
                Log.e(Constants.TAG,"cannot write into the file");
        }        
    }
    
    static StringBuilder sb = new StringBuilder();
    
    private static String generateMetadataXml() throws IOException {
        sb.setLength(0);
       // sb.append("<NodeElement type=\"directory\" ip=\"TODO\" Name=\"TODO\">\n");
        traverseDirectoryStructure(new File(Environment.getExternalStorageDirectory() + "/SmartFSSharedFiles"));
        //sb.append("</NodeElement>\n");
        return sb.toString();
    }
    
    private static void traverseDirectoryStructure(File mf) {
        File fileList[] = mf.listFiles();
        if (null != fileList) {
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    sb.append("<NodeElement type=\"directory\" Name=\"" + fileList[i].getName() + "\">\n");
                    traverseDirectoryStructure(fileList[i]);
                    sb.append("</NodeElement>\n");
                } else {
                    sb.append("<NodeElement type=\"file\" Name=\"" + fileList[i].getName() + "\"></NodeElement>\n");
                }
            }
        }
    }
    
    static void decideTask(Socket socket,byte[] buffer,int numberOfBytesRead){
        
        String bufferString;
        String messageReadArray[];
        byte[] messageBuffer;
        
        
        //Step 1 : Get the buffer consisting of only bytes read
        messageBuffer = Arrays.copyOfRange(buffer,0,numberOfBytesRead);
        
        //Step 2 : Cast the buffer to a string to get the protocol
        bufferString=new String(messageBuffer);
        
        //Step 3 : Split the message by the delimiter
        messageReadArray = bufferString.split("\\|");
        
        //Step 4 : decide the task
        if(messageReadArray[0].equalsIgnoreCase("Connect")) {
            String data = "";
            try {
                data = "metadata\n" + generateMetadataXml();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new ClientThread(socket, data.getBytes());
        }        
        else if(messageReadArray[0].equalsIgnoreCase(Constants.INVALIDCREDIANTIALS)){
            
            loginActivity.displayInvalidCredentials();
        }
        else if(messageReadArray[0].equalsIgnoreCase(Constants.NEWSERVERLISTPROTOCOL)){
            
            createNewServerList(bufferString);
            prepareServerListXmlString();
            //displayServerList();
        }
        else if(messageReadArray[0].equalsIgnoreCase(Constants.DOWNLOADPROTOCOL)){
            
            uploadFile(socket,messageReadArray[1]);
        }
        else if(messageReadArray[0].equalsIgnoreCase(Constants.INVALIDFILE)){
            
            displayInvalidFile();
        }
        else if(messageReadArray[0].equalsIgnoreCase(Constants.STOREPROTOCOL)){
            
            storeFile(buffer,numberOfBytesRead);
        }
        else if(messageReadArray[0].substring(0, 8).equalsIgnoreCase("metadata")) {
            StringBuilder msb = new StringBuilder();
            String command = "";
            BufferedReader br = new BufferedReader(new StringReader(messageReadArray[0]));
            try {
                br.readLine();
                while(null != (command = br.readLine())) {
                    msb.append(command + "\n");
                }
                mainActivity.new ShowPeerMetadata(msb.toString()) ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        
    }
    
    static void exitAllConnections(){
        
      Iterator<Node> it;
      Node n ;
      it= LoginActivity.connectionList.iterator();
      while(it.hasNext()){    
          n =it.next();
          try{
              n.getSocket().close();
          }
          catch(Exception e){                 
              Log.e(Constants.TAG,e.toString());
          }
          it.remove();
      }
        
    }
    
    
    static void terminateConnection(Socket clientSocket){
        
        int i;
        for(i=0; i< LoginActivity.connectionList.size();i++){
         
            //Step 1 : If the socket to be terminated is the server connection....terminate all connections
            if(LoginActivity.connectionList.get(i).getSocket().equals(clientSocket) && i==0){
                
                exitAllConnections();
            }
            else if(LoginActivity.connectionList.get(i).getSocket().equals(clientSocket)){  
                
                //Step 2 : else just terminate the specified connection
                LoginActivity.connectionList.remove(i);
                try{
                clientSocket.close();
                break;
                }
                catch(Exception e){
                    Log.e(Constants.TAG,e.toString());
                }
            }
                                           
        }
        
    }
    
    
    static void processDownloadRequest(Socket peerSocket , String peerIP , String peerPort , String fileName){
        
        String downloadRequest;



        //Step 1 : Create the download request
        downloadRequest = Constants.DOWNLOADPROTOCOL + Constants.PIPEDELIMITER + fileName + Constants.PIPEDELIMITER;
        //downloadRequest = Constants.DOWNLOADPROTOCOL + Constants.PIPEDELIMITER + filePath ;  
        
        try{
        //Step 2: for testing purposes
        if(peerSocket==null){
            peerSocket = new Socket(InetAddress.getByName(Constants.SERVERIP), Integer.parseInt(Constants.SERVER_PORT));
        }
        }
        catch(Exception e){
            Log.e(Constants.TAG,"Error");
        }
        //Step 2 : Send the request to the peer
        new ClientThread(peerSocket,downloadRequest.getBytes());
        
    }
    
    static void SimulateNewServerList(){
        
        String message = Constants.NEWSERVERLISTPROTOCOL + Constants.PIPEDELIMITER + "abhishek" + Constants.PIPEDELIMITER  + "10.10.10.10" + Constants.PIPEDELIMITER + "4560" + Constants.PIPEDELIMITER + "sujay" + Constants.PIPEDELIMITER + "10.10.10.20" + Constants.PIPEDELIMITER + "4460" + Constants.PIPEDELIMITER;
        createNewServerList(message);
        prepareServerListXmlString();
        displayServerList();      
    }
    
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            
        } 
        return "";
    }

}
