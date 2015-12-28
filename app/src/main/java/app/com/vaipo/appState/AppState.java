package app.com.vaipo.appState;

import android.app.Application;
import app.com.vaipo.uuid.UUIDFactory;


public class AppState extends Application {

    private static final String TAG = "AppState";
    private UUIDFactory mUUIDFactory = null;
    private String number = "";

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
}
