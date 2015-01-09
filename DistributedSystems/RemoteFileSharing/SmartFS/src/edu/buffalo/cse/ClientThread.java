package edu.buffalo.cse;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientThread implements Runnable {

    Thread t;
    byte[] msg;
    Socket socket;
    String port="";
    String ipAddress;
    boolean processThreadFlag=false;

    ClientThread(Socket socket, byte[] msg) {
        t = new Thread(this);
        this.msg = msg;
        this.socket=socket;
        t.start();
    }
    
    ClientThread(String ipAddress , String port, byte[] msg) {
        t = new Thread(this);
        this.msg = msg;
        this.port=port;
        this.ipAddress=ipAddress;
        socket=null;
        processThreadFlag=true;
        t.start();

    }

    public void run() {

        try {
            
            //Step 1: Obtain the socket if socket is null
            if(socket==null){
            socket = new Socket(InetAddress.getByName(ipAddress), Integer.parseInt(port));
            }
                 
            // Step 2: Create an output stream and send the message
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            Log.e(Constants.TAG,new String(msg));
            out.write(msg);  
            out.flush();
            
            //Step 3 : Start the processThreadFlag if processThreadFlag=true and wait for any incoming data in it
            if(processThreadFlag){
            new ProcessSocketThread(socket);
            }
            
        } catch (UnknownHostException e) {
            Log.e(Constants.TAG, "ClientTask UnknownHostException");
        } catch (IOException e) {
            Log.e(Constants.TAG, "ClientTask socket IOException");
            Log.e(Constants.TAG, e.toString());
        } catch (Exception e) {
            Log.e(Constants.TAG, "Emulator 5554 is not available. Cannot join \n");

        }
    }
}

