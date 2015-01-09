package edu.buffalo.cse;

import android.util.Log;

import java.io.BufferedInputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.Arrays;

public class ProcessSocketThread implements Runnable{
    
    Thread t;
    Socket clientSocket;
    BufferedInputStream in;
    byte[] buffer = new byte[Constants.BUFFER_SIZE];
    int numberOfBytesRead;
            
    ProcessSocketThread(Socket clientSocket){  
        t = new Thread(this);
        this.clientSocket=clientSocket;
        t.start();
    }
    
    public void run(){
        
        try{
            
            //Step 1 : Initialize the buffered reader
            in =  new BufferedInputStream((clientSocket.getInputStream()));

          //  byte[] totaldata = new byte[10000];
          //  int totalsize = 0;
//            while(true){
//                
//                //Step 2 : Read from the buffered Reader
//                    numberOfBytesRead = in.read(buffer);
//                    if(numberOfBytesRead<=0 )
//                    {break
//                        ;}
//                    else
//                    {
//                      //Arrays.copyOfRange(buffer,0,numberOfBytesRead);
//                      System.arraycopy(buffer, 0, totaldata, totalsize, numberOfBytesRead);
//                      totalsize = totalsize + numberOfBytesRead;
//                  }
//            }
               
            
            while(true){
                
                //Step 2 : Read from the buffered Reader
                    numberOfBytesRead = in.read(buffer);
                            
                if(numberOfBytesRead==-1){
                    
                    //Step 3 : If the socket has been closed from the remote peer
                    Registration.terminateConnection(clientSocket);
                    Registration.displayConnectionList();
                    break;
                    
                }else{
                    
                    //Step 3 : Decide the task to be executed with the message
                    Registration.decideTask(clientSocket, buffer,numberOfBytesRead);
                }
            }
            }
        catch(Exception e){
            Log.e(Constants.TAG,e.toString());
        }
        
    }
}

