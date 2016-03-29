package app.com.vaipo.messages;

/**
 * Created by siddartha on 12/23/15.
 */
public class UserMsg implements IMessage {
    private String uniqueId;
    private boolean userAck;
    private boolean receiveAck;
    private String callee;
    private String caller;

    public UserMsg() {
        uniqueId = caller = callee ="";
        userAck = receiveAck = false;
    }

    public UserMsg(String uniqueId, boolean userAck) {
        this.uniqueId = uniqueId;
        this.userAck = userAck;
    }

    public void setId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setAck(boolean userAck) {
        this.userAck = userAck;
    }

    public void setCallee(String number) {
        this.callee = number;
    }

    public void setCaller(String number) {
        this.caller = number;
    }

    public String getId() {
        return uniqueId;
    }

    public boolean getAck() {
        return userAck;
    }

    public String getCaller() {
        return caller;
    }

    public String getCallee() {
        return callee;
    }
}
