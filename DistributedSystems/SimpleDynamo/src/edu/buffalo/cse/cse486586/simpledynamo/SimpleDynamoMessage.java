
package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.Serializable;

/**
 * Simple POJO class to represent a message
 * 
 * @author sudarshan
 */
public class SimpleDynamoMessage implements Serializable {

    private static final long serialVersionUID = -7930040908185435969L;

    public SimpleDynamoMessage(String msgTyp, String mOrigPort, String mKy, String mVal) {
        this.setMessageType(msgTyp);
        this.setmOriginPort(mOrigPort);
        this.setmKey(mKy);
        this.setmValue(mVal);
    }

    private String messageType;

    private String mOriginPort;

    private String mKey;

    private String mValue;

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

    public String getmKey() {
        return mKey;
    }

    public void setmKey(String mKey) {
        this.mKey = mKey;
    }

    public String getmValue() {
        return mValue;
    }

    public void setmValue(String mValue) {
        this.mValue = mValue;
    }

}
