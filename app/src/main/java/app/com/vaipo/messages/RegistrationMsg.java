package app.com.vaipo.messages;

/**
 * Created by siddartha on 12/23/15.
 */
public class RegistrationMsg implements IMessage {
    private String uniqueId;
    private String number;

    public RegistrationMsg() {
        uniqueId = "";
        number = "";
    }

    public RegistrationMsg(String uniqueId, String number) {
        this.uniqueId = uniqueId;
        this.number = number;
    }

    public void setId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getId() {
        return uniqueId;
    }

    public String getNumber() {
        return number;
    }
}
