package app.com.vaipo.messages;

/**
 * Created by siddartha on 12/23/15.
 */
public class DialMsg implements IMessage {
    private String id;
    private String callee;
    private String caller;
    private int state;
    private String sessionId;


    public static final int IDLE = 0;
    public static final int DIALING = 1;
    public static final int INCOMING = 2;
    public static final int ACTIVE = 3;
    public static final int END = 4;


    public DialMsg() {
        id = "";
        callee = "";
        caller = "";
        state = IDLE;
        sessionId = "-1";
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCallee(String number) {
        this.callee = number;
    }

    public void setCaller(String number) {
        this.caller = number;
    }


    public String getId() {
        return id;
    }

    public String getCallee() {
        return callee;
    }

    public String getCaller() {
        return caller;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

}
