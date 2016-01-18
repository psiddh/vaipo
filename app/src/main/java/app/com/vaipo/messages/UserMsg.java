package app.com.vaipo.messages;

/**
 * Created by siddartha on 12/23/15.
 */
public class UserMsg implements IMessage {
    private String uniqueId;
    private boolean userAck;
    private boolean receiveAck;

    public UserMsg() {
        uniqueId = "";
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

    public String getId() {
        return uniqueId;
    }

    public boolean getAck() {
        return userAck;
    }
}
