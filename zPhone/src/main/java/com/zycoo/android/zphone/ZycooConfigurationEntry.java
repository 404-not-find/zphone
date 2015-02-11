package com.zycoo.android.zphone;

public class ZycooConfigurationEntry {
    private static final String TAG = ZycooConfigurationEntry.class.getCanonicalName();
    // AMI
    public static final String NETWORK_AMI_ENABLE = "NETWORK_AMI_ENABLE." + TAG;
    public static final String NETWORK_AMI_HOST = "NETWORK_AMI_HOST." + TAG;
    public static final String NETWORK_AMI_PORT = "NETWORK_AMI_PORT." + TAG;
    public static final String NETWORK_AMI_USERNAME = "NETWORK_AMI_USERNAME." + TAG;
    public static final String NETWORK_AMI_SECRET = "NETWORK_AMI_SECRET." + TAG;
    //Default AMI
    public static final String DEFAULT_NETWORK_AMI_HOST = "192.168.1.251";
    public static final String DEFAULT_NETWORK_AMI_PORT = "5038";
    public static final String DEFAULT_NETWORK_AMI_USERNAME = "admin";
    public static final String DEFAULT_NETWORK_AMI_SECRET = "admin";
    // HTTP
    public static final String NETWORK_HTTP_HOST = "NETWORK_HTTP_HOST." + TAG;
    public static final String NETWORK_HTTP_PORT = "NETWORK_HTTP_PORT." + TAG;
    // Default HTTP
    public static final String DEFAULT_NETWORK_HTTP_HOST = "127.0.0.1";
    public static final String DEFAULT_NETWORK_HTTP_PORT = "5038";
    //Constant
    public static final int VOICE_MAIL_COUNT = 0;
    public static final int ONLINE_PEERS = 1;
    //broadcast
    public static final String ARG_PAGE = "arg_page";
    //Pattern
    //"tqc" <806> ====> tqc 806
    public static final String VOICE_CALLER_ID1 = "(?<=\"|<)([^\\s].*?)(?=\"|>)";
    //tqc <806> ====> tqc 806
    public static final String VOICE_CALLER_ID2 = "(?<=<|^)([^\\s].*?)(?= |>)";
    //Session Id
    public static final String SESSION_ID = "com.zycoo.android.sip.view.dialpad.session_id";
    //Task time
    public static final int AMI_LOGIN_TASK_DELOY = 1000;
    public static final int AMI_LOGIN_TASK_PERIOD = 6000;
    public static final int AMI_VOICE_MAIL_ACTION_TASK_DELOY = 1000;
    public static final int AMI_VOICE_MAIL_ACTION_TASK_PERIOD = 6000;
    public static final int AMI_ACTION_SLEEP_TIME = 1000;
    //AMI ACTION
    public static final String SUCCESS_RESPONSE = "SUCCESS";
    //DataBase Name
    public static final String DATA_BASE_NAME = "SoftPhone.db";



    //general
    public static final String GENERAL_KEYPAD_VIBRATION = "GENERAL_KEYPAD_VIBRATION." + TAG;
    public static final String GENERAL_KEYPAD_TONES = "GENERAL_KEYPAD_TONES." + TAG;
    public static final String GENERAL_PROXIMITY_SENSOR = "GENERAL_PROXIMITY_SENSOR." + TAG;
    public static final boolean DEFAULT_GENERAL_KEYPAD_VIBRATION = false;
    public static final boolean DEFAULT_GENERAL_KEYPAD_TONES = true;
    public static final boolean DEFAULT_GENERAL_PROXIMITY_SENSOR = true;

    //first login
    public static final boolean DEFAULT_FIRST_LOGIN = true;
    public static final String FIRST_LOGIN = "first_login" + TAG;
    //currentColor
    public static final String CURRENT_COLOR = "currentColor" + TAG;
}
