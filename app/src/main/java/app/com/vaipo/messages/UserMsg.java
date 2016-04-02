package app.com.vaipo.messages;

/**
 * Created by siddartha on 12/23/15.
 */
public class UserMsg implements IMessage {
    private String uniqueId;
    private boolean response;
    private String callee;
    private String caller;

    public UserMsg() {
        uniqueId = caller = callee ="";
        response = false;
    }

    public UserMsg(String uniqueId, boolean response) {
        this.uniqueId = uniqueId;
        this.response = response;
    }

    public void setId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setResponse(boolean response) {
        this.response = response;
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

    public boolean getResponse() {
        return response;
    }

    public String getCaller() {
        return caller;
    }

    public String getCallee() {
        return callee;
    }
}
