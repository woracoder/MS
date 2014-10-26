
package edu.buffalo.cse.cse486586.simpledht;

/**
 * Simple POJO class to represent a message
 * 
 * @author sudarshan
 */
public class SimpleDhtMessage {

    public SimpleDhtMessage(String msgTyp, String mOrigPort, String mSucPrt, String mPredPrt) {
        this.setMessageType(msgTyp);
        this.setmOriginPort(mOrigPort);
        this.setmSucPort(mSucPrt);
        this.setmPredPort(mPredPrt);
    }

    private String messageType;

    private String mOriginPort;

    private String mSucPort;

    private String mPredPort;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getmOriginPort() {
        return mOriginPort;
    }

    public void setmOriginPort(String mOriginPort) {
        this.mOriginPort = mOriginPort;
    }

    public String getmSucPort() {
        return mSucPort;
    }

    public void setmSucPort(String mSucPort) {
        this.mSucPort = mSucPort;
    }

    public String getmPredPort() {
        return mPredPort;
    }

    public void setmPredPort(String mPredPort) {
        this.mPredPort = mPredPort;
    }

}
