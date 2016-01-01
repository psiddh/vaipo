package app.com.vaipo.appState.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by siddartha on 12/30/15.
 */
public class Utils {

    public static String END_VAIPO_CALL = "end-vaipo-call";

    public static void endVaipoCall(Context context) {
        Intent intent = new Intent(END_VAIPO_CALL);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
