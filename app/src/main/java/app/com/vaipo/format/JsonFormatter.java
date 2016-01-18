package app.com.vaipo.format;

/**
 * Created by siddartha on 12/23/15.
 */

import android.util.JsonWriter;
import android.util.Log;

import java.io.IOException;
import java.io.StringWriter;

import app.com.vaipo.messages.DialMsg;
import app.com.vaipo.messages.IMessage;
import app.com.vaipo.messages.RegistrationMsg;
import app.com.vaipo.messages.UserMsg;


public class JsonFormatter {

    private static final String TAG = "JsonFormatter:";
    private StringWriter mOutStream = null;
    private JsonWriter mJSONWriter = null;
    private boolean mIsInitialized = false;


    public JsonFormatter() {
    }

    public void initialize() {
        mOutStream = new StringWriter();
        mJSONWriter = new JsonWriter(mOutStream);
        mJSONWriter.setIndent("  ");
        mIsInitialized = true;
    }

    public void destroy() {
        try {
            mJSONWriter.flush();
            mJSONWriter.close();
            mOutStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        mIsInitialized = false;
    }

    public String get(IMessage msg) {
        if (!mIsInitialized) {
            throw new InstantiationError("Call 'initialize' before calling format(). Either " +
                    this.getClass().getName() + "'s destroy() is called (or) it has not been initialized");
        }
        try {
            if (msg instanceof RegistrationMsg) {
                formatRegistrationMsg(msg);
            } else if (msg instanceof DialMsg) {
                formatDialMsg(msg);
            } else if (msg instanceof UserMsg) {
                formatUserMsg(msg);
            } else {
                throw new InstantiationError(" Unknown Class " + msg.getClass() + ", Unable to format!") ;
            }
        } catch (OutOfMemoryError error) {
            Log.d(TAG, "DBG: Fatal Exception Encountered!");
            error.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "DBG: IOException while attempting to format msg of type : - " + msg.getClass());
        }

        finally {
            return finalizeAndGet();
        }

    }

    private void formatRegistrationMsg (IMessage baseMsg) throws IOException {
        RegistrationMsg msg = (RegistrationMsg) baseMsg;
        mJSONWriter.beginObject();
        mJSONWriter.name("id").value(msg.getId());
        mJSONWriter.name("number").value(msg.getNumber());
        mJSONWriter.endObject();
    }

    private void formatDialMsg (IMessage baseMsg) throws IOException {
        DialMsg msg = (DialMsg) baseMsg;
        mJSONWriter.beginObject();
        mJSONWriter.name("id").value(msg.getId());
        mJSONWriter.name("callee").value(msg.getCallee());
        mJSONWriter.name("caller").value(msg.getCaller());
        mJSONWriter.name("state").value(msg.getState());
        mJSONWriter.name("userAck").value(false);
        mJSONWriter.name("receiveAck").value(false);
        mJSONWriter.endObject();
    }

    private void formatUserMsg (IMessage baseMsg) throws IOException {
        UserMsg msg = (UserMsg) baseMsg;
        mJSONWriter.beginObject();
        mJSONWriter.name("id").value(msg.getId());
        mJSONWriter.name("userAck").value(msg.getAck());
        mJSONWriter.name("receiveAck").value(false);
        mJSONWriter.endObject();
    }

    private String finalizeAndGet() {
        String msg = "";
        try {
            mJSONWriter.flush();
            msg = mOutStream.toString();
            mJSONWriter.close();
            mOutStream.close();
            mIsInitialized = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return msg;
    }

}

