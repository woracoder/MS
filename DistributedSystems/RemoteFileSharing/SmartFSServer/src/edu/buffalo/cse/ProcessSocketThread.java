    
    package edu.buffalo.cse;
    import java.io.BufferedOutputStream;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

    public class ProcessSocketThread implements Runnable{
    
        Socket clientSocket;
        BufferedInputStream in;
        byte[] buffer = new byte[Constants.BUFFER_SIZE];
        int numberOfBytesRead;
        Thread t;
                
        ProcessSocketThread(Socket clientSocket){  
            t = new Thread(this);
            this.clientSocket=clientSocket;
            t.start();
        }
                
        void terminateConnection(Socket clientSocket){
            
            for(Node n : SmartFSServer.serverList){
                
                if(n.getSocket().equals(clientSocket)){
                    SmartFSServer.serverList.remove(n);
                    try{
                    clientSocket.close();
                    break;
                    }
                    catch(Exception e){
                        System.out.println(e.toString() + "\n");
                    }
                }
            }            
        }
        
        void displayServerList(){
            
            System.out.println("\nAVAILABLE PEER LIST");
            for(Node n : SmartFSServer.serverList){
                System.out.println("IP Address:  " + n.getIPAddress() + "   Port Number: " + n.getPortNumber() + "\n");
            }
                    
        }
           
        boolean authenticateUser(String user, String pass) {
            return SmartFSServer.userPassMap.containsKey(user) && SmartFSServer.userPassMap.get(user).equals(pass) ? true : false;
        } 
        
        void processRegisterRequest(Socket socket,String messageRead){
            
            String messageReadArray[];
            String serverListPacket= Constants.NEWSERVERLISTPROTOCOL + Constants.PIPEDELIMITER ;
            String invalidCredentials;
            Node newNode;
            boolean duplicateRequestFlag=false;
            
            //Step 1 : Split the message by the delimiter
            messageReadArray = messageRead.split("\\|");
            
            //Step 2 : Verify the username and Password
            //if(!authenticateUser(messageReadArray[1],messageReadArray[2])){
            if(false){
                //invalidCredentials=Constants.INVALIDCREDIANTIALS + Constants.PIPEDELIMITER ;
                invalidCredentials=Constants.INVALIDCREDIANTIALS ;
                new ClientThread(socket,messageReadArray[4],invalidCredentials.getBytes());
            }
            else{
                
                //Step 3 : Check if the user is already registered
                for(Node n : SmartFSServer.serverList){                
                    if(n.getIPAddress().equalsIgnoreCase(messageReadArray[3])){                
                        duplicateRequestFlag=true;
                    }                
                }
                        
                //Step 4 : Add the node in the list only if it is a new node
                if(!duplicateRequestFlag){
                
                    //Step 5 : create a new node
                    newNode = new Node();
            
                    //Step 6 : Assign IP address , port no. and socket to it and add it to the server List
                    newNode.setUserName(messageReadArray[1]);
                    newNode.setIPAddress(messageReadArray[3]);
                    newNode.setPortNumber(messageReadArray[4]);
                    newNode.setSocket(socket);
                    SmartFSServer.serverList.add(newNode);
                }
            
            
                //Step 7 :Create the serverList packet to be sent to each client
                for(Node n : SmartFSServer.serverList){
             
                    serverListPacket = serverListPacket + n.getUserName();
                    serverListPacket = serverListPacket + Constants.PIPEDELIMITER;
                    serverListPacket = serverListPacket + n.getIPAddress();
                    serverListPacket = serverListPacket + Constants.PIPEDELIMITER;
                    serverListPacket = serverListPacket + n.getPortNumber();
                    serverListPacket = serverListPacket + Constants.PIPEDELIMITER;          
                }

                //Step 8 : Send this server list to each registered client
                for(Node n : SmartFSServer.serverList){                
                    new ClientThread(n.getSocket(),n.getPortNumber(),serverListPacket.getBytes());            
                }    
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
            file = new File(fileName);
            String pth = file.getAbsolutePath();
            if(!file.isFile()){
                //msg = Constants.INVALIDFILE + Constants.PIPEDELIMITER + fileName  + Constants.PIPEDELIMITER;
                msg = Constants.INVALIDFILE + Constants.PIPEDELIMITER + fileName ;
                new ClientThread(socket,"",msg.getBytes());
                
            }
            else{
                
                //Step 2 : Open the file
                rf =new RandomAccessFile(fileName,"r");
            
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
                        //new ClientThread(socket,"",headerAndDataBuffer);
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
                        
                        //Step 20 : Send the message
                        //new ClientThread(socket,"",headerAndDataBuffer); 
                        out.write(headerAndDataBuffer);
                        out.flush();
                        
                        }
                }
            }
            
            }
            catch(Exception e){
                System.out.println("Error in opening file \n");
            }
        }
        
        
        void decideTask(Socket socket,byte[] buffer,int numberOfBytesRead){
            
            String bufferString;
            String messageReadArray[];
            byte messageBuffer[];
            
            //Step 1 : Get the buffer consisting of only bytes read
            messageBuffer = Arrays.copyOfRange(buffer,0,numberOfBytesRead);
            
            //Step 2 : Cast the buffer to a string to get the protocol
            bufferString=new String(messageBuffer);
            
            //Step 3 : Split the message by the delimiter
            messageReadArray = bufferString.split("\\|");
            System.out.println(bufferString);
            
            //Step 4 : decide the task
            if(messageReadArray[0].equalsIgnoreCase(Constants.REGISTERPROTOCOL)){
                
                processRegisterRequest(socket, bufferString);
                displayServerList();
            }
            else if(messageReadArray[0].equalsIgnoreCase(Constants.DOWNLOADPROTOCOL)){
                
                uploadFile(socket,messageReadArray[1]);
            }
            
        }

        
        public void run(){
            
            try{
                
                //Step 1 : Initialize the buffered Input Stream
                in =  new BufferedInputStream((clientSocket.getInputStream()));
    
                while(true){
                    
                    //Step 2 : Read from the buffered Input Stream
                    numberOfBytesRead = in.read(buffer);
            
                    if(numberOfBytesRead==-1){
                        
                        //Step 3 : If the socket has been closed from the remote peer
                        terminateConnection(clientSocket);
                        displayServerList();
                        break;
                        
                    }else{
                        
                        //Step 4 : Decide the task to be executed with the message
                        decideTask(clientSocket, buffer,numberOfBytesRead);
                    }
                }
                }
            catch(Exception e){
                System.out.println(e.toString() + "\n");
            }
            
        }
    }
    
    
    
