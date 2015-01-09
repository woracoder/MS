    
package edu.buffalo.cse;

import java.util.HashMap;

import java.util.Map;
import org.apache.http.conn.util.InetAddressUtils;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.Socket;

public class SmartFSServer {
    
    static ArrayList<Node> serverList;
    static ArrayList<Node> connectionList;
    static Map<String, String> userPassMap;
    
    
    
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
        
    public static void acceptConnections(){
        
        ServerSocket serverSocket; 
        Socket clientSocket;

        try{
            
            //Step 1 : create a server socket
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
                       
            while(true)
            {
                //Step 2 : Accept the connection
                clientSocket =serverSocket.accept();
                
                //Step 2 : process the client socket
                new ProcessSocketThread(clientSocket);                                             
            }
        }
        catch(Exception e){
        System.out.println("Exception Occured " + e.toString() + "\n");
        }
    }
    
    
    
    
    public static void main(String args[]){
        
        String myIP;
        
        //Step 1 : Initialize serverList and ConnectionList
        serverList = new ArrayList<Node>();
        connectionList = new ArrayList<Node>();
        userPassMap= new HashMap<String, String>();
        
        //Step 2 : Get IP Address
        myIP=getIPAddress(true);
        System.out.println("Server IP Address is :" +  myIP);
        
        //Step 3 : Insert data into the map
        userPassMap.put("sujay", "sujay");
        userPassMap.put("kapoor", "kapoor");
        userPassMap.put("sudarshan", "sudarshan");
        userPassMap.put("sam", "sam");
        userPassMap.put("sharad", "sharad");
        userPassMap.put("kanjo", "kanjo");
        
        //Step 4 : Invoke accept connections
        acceptConnections();
    }
}
