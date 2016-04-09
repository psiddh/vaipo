package app.com.vaipo.config;

public class OpenTokConfig {

    // *** Fill the following variables using your own Project info from the OpenTok dashboard  ***
    // ***                      https://dashboard.tokbox.com/projects                           ***
    // Replace with a generated Session ID
    public static  String SESSION_ID = "";
    // Replace with a generated token (from the dashboard or using an OpenTok server SDK)
    public static  String TOKEN =
            "";
    // Replace with your OpenTok API key
    public static  String API_KEY = "";

    // Subscribe to a stream published by this client. Set to false to subscribe
    // to other clients' streams only.
    public static final boolean SUBSCRIBE_TO_SELF = false;


}
