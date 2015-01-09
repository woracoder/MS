/**
 * 
 */

package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.Serializable;

/**
 * @author srao2
 */
public class GroupMessage implements Serializable, ApplicationConstants {

    private static final long serialVersionUID = 1452135325603559671L;

    public GroupMessage(String message, String mPort, String mRemPort, String mSeq, String mIsSeq) {
        this.setMsg(message);
        this.setmPortNo(mPort);
        this.setmRemPortNo(mRemPort);
        this.setmSeqNo(Integer.parseInt(mSeq));
        if (mIsSeq.equalsIgnoreCase(TRUE_STRING)) {
            this.setmIsSeqMsg(true);
        } else {
            this.setmIsSeqMsg(false);
        }
    }

    private String msg;

    private String mPortNo;

    private String mRemPortNo;
    
    private int mSeqNo;

    private boolean mIsSeqMsg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getmPortNo() {
        return mPortNo;
    }

    public void setmPortNo(String mPortNo) {
        this.mPortNo = mPortNo;
    }

    public String getmRemPortNo() {
        return mRemPortNo;
    }

    public void setmRemPortNo(String mRemPortNo) {
        this.mRemPortNo = mRemPortNo;
    }

    public int getmSeqNo() {
        return mSeqNo;
    }

    public void setmSeqNo(int mSeqNo) {
        this.mSeqNo = mSeqNo;
    }

    public boolean ismIsSeqMsg() {
        return mIsSeqMsg;
    }

    public void setmIsSeqMsg(boolean mIsSeqMsg) {
        this.mIsSeqMsg = mIsSeqMsg;
    }

}
