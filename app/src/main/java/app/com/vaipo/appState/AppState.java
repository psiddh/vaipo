package app.com.vaipo.appState;

import android.app.Application;
import app.com.vaipo.uuid.UUIDFactory;


public class AppState extends Application {

    private static final String TAG = "AppState";
    private UUIDFactory mUUIDFactory = null;
    private String number = "";

    private String caller = "";
    private String callee = "";

    @Override
    public void onCreate() {
        super.onCreate();
        mUUIDFactory = new UUIDFactory(this);
    }

    public String getID() {
        if (mUUIDFactory == null) {
            mUUIDFactory = new UUIDFactory(this);
        }
        return mUUIDFactory.getUuid().toString();
    }

    public String setNumber(String number) {
       return this.number = number;
    }

    public String getNumber() {
        return this.number;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCaller() {
        return caller;
    }

    public void setCallee(String callee) {
        this.callee = callee;
    }

    public String getCallee() {
        return callee;
    }

}
