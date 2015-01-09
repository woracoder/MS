package edu.buffalo.cse;

import java.net.Socket;

public class Node {
    
    String IPAddress;
    String userName;
    String portNumber;
    Socket socket;
    
    public Socket getSocket() {
        return socket;
    }
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    public String getIPAddress() {
        return IPAddress;
    }
    public void setIPAddress(String iPAddress) {
        IPAddress = iPAddress;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPortNumber() {
        return portNumber;
    }
    public void setPortNumber(String portNumber) {
        this.portNumber = portNumber;
    }
    
    

}
