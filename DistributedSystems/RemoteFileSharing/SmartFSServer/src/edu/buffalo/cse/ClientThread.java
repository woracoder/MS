    
    package edu.buffalo.cse;
    import java.io.BufferedOutputStream;

import java.net.InetAddress;

import java.io.PrintWriter;
import java.net.Socket;

    public class ClientThread implements Runnable {
    
        Thread t;
        byte msg[];
        Socket clientSocket;
        String portNumber;

        ClientThread(Socket clientSocket,String portNumber ,byte[] msg) {
            t = new Thread(this);
            this.msg = msg;
            this.clientSocket=clientSocket;
            this.portNumber=portNumber;
            t.start();
        }

        public void run() {

            try {
             
                //For testing on emulator
                //clientSocket = new Socket(InetAddress.getByAddress(new byte[] {10, 0, 2, 2 }), Integer.parseInt(portNumber));
                
                // Step 1: Create an output stream and send the message
                BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());
                out.write(msg);
                out.flush();
                
            }  catch (Exception e) {
                System.out.println(e.toString() +  "\n");

            }
        }
    }
