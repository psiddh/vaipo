package app.com.vaipo.messages;

/**
 * Created by siddartha on 12/23/15.
 */
public class AuthenticateMsg implements IMessage {
    public String id;
    public String number;
    public String code;

    public AuthenticateMsg() {
        id = "";
        number = "";
        code = "";
    }

    public AuthenticateMsg(String uniqueId, String number, String code) {
        this.id = uniqueId;
        this.number = number;
        this.code = code;
    }

    public void setId(String uniqueId) {
        this.id = uniqueId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getCode() { return code; }
}
