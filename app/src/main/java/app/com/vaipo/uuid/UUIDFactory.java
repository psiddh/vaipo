package app.com.vaipo.uuid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class UUIDFactory {
    private static final String TAG = "UUIDFactory";
    private static final String MAGIC_DEVICE_ID = "9774d56d682e549c";
    private static final String EMPTY_STRING = "";

    protected static final String PREFS_FILE = "device_id";
    protected static final String PREFS_DEVICE_ID = "device_id";

    protected static UUID uuid;

    public UUIDFactory(Context context) {
        if( uuid == null ) {
            synchronized (UUIDFactory.class) {
                if( uuid == null) {
                    final SharedPreferences prefs = context.getSharedPreferences( PREFS_FILE, 0);
                    final String id = prefs.getString(PREFS_DEVICE_ID, null );
                    if (id != null) {
                        // Use the ids previously computed and stored in the prefs file
                        uuid = UUID.fromString(id);
                    } else {
                        // Order of preference for a picking up uuid
                        // 1st : Android_ID
                        // 2nd : Serial Code
                        // 3rd : Much less reliable random UUID
                        final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        try {
                            if (androidId != null && !EMPTY_STRING.equals(androidId) && !MAGIC_DEVICE_ID.equals(androidId)) {
                                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                            } else {
                                // device has no valid android_id, try using the Android serial code
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                                    uuid = UUID.fromString(Build.SERIAL);
                                } else {
                                    uuid = UUID.randomUUID();
                                }
                            }
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }

                        // Write the value out to the prefs file
                        prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString() ).apply();
                    }
                }
            }
        }
    }

    public UUID getUuid() {
        return uuid;
    }
}