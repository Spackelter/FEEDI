package edu.kit.itiv.feedi_app;

public class Const {

    //Log tags
    public static final String LOG_TAG_BLE_MANAGER = "Ble manager";

    //Requests
    public static final int REQUEST_ENABLE_BLUETOOTH = 1;
    public static final int REQUEST_FINE_LOCATION_PERMISSION = 2;

    //Return codes
    public static final int CODE_OK = 0; //OK also stands for devices found
    public static final int CODE_NO_DEVICE_FOUND = 1;
    public static final int CODE_LACKS_OF_PERMISSION = -1;
    public static final int CODE_ALREADY_BLE_SCANNING = -2;

    //diverse constants
    public static final long MY_SCAN_PERIOD = 5000;

    //Intent names
    public static final String INTENT_ACTION_NOTIFY_BLE_SCAN_COMPLETE = "com.example.alain.NOTIFY_SCAN_COMPLETE";
    public static final String INTENT_ACTION_NOTIFY_BLE_MESSAGE_RECEIVED = "com.example.alain.NOTIFY_MESSAGE_RECEIVED";
    public static final String INTENT_ACTION_NOTIFY_BLE_DISCONNECTED = "com.example.alain.NOTIFY_DISCONNECTED";
    public static final String INTENT_ACTION_NOTIFY_BLE_CONNECTED = "com.example.alain.NOTIFY_CONNECTED";
    public static final String INTENT_ACTION_NOTIFY_NEW_GPS_POSITION = "com.example.alain.NOTIFY_NEW_GPS_POSITION";
    public static final String INTENT_ACTION_NOTIFY_GPS_POSITION_LOST = "com.example.alain.NOTIFY_GPS_POSITION_LOST";

    //time Constants
    public static final long MY_TIMEOUT = 11000;

    //Constants for the runninc activity manager
    public static final int NO_MODE = 0;
    public static final int MODE_POINT_TO_NORTH = 1;
    public static final int MODE_POINT_NAVIGATION_COMPASSVIEW = 2;
    public static final int MODE_POINT_NAVIGATION_MAPVIEW = 3;

    public Const(){

    }


}
