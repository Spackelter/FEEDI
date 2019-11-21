package edu.kit.itiv.feedi_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class PointNavigationMenuActivity extends AppCompatActivity {


    //for controlling the active mode
    private runningActivityManager myRunningActivityManager;

    //ble manager
    private BleManager myBleManager;

    //Manager for the (BNO055) sensor data
    private Bno055DataManager myBno055DataManager;

    //GPS location manager
    GPSLocationManager myGpsLocationManager;

    //UI Elements
    private TextView tv_ConnectionState;
    private TextView tv_GPSPositionState;
    private Spinner spinner_SelectDestOptions;
    private TextView tv_title_EnterAddressBelow;
    private EditText ev_Address;
    private TextView tv_title_Latitude;
    private EditText ev_Latitude;
    private TextView tv_title_Longitude;
    private EditText ev_Longitude;
    private Spinner spinner_SelectView;

    //broadCastReceiver
    private BroadcastReceiver myBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_NEW_GPS_POSITION)){
                tv_GPSPositionState.setText("GPS Position Found");
                tv_GPSPositionState.setBackgroundColor(Color.parseColor("#00FF00"));
            }
            else if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_GPS_POSITION_LOST)){
                tv_GPSPositionState.setText("GPS Position Not Found");
                tv_GPSPositionState.setBackgroundColor(Color.parseColor("#FF0000"));
            }
            else if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_BLE_MESSAGE_RECEIVED)){
                onBleMessageReceived();
            }
            else if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_BLE_CONNECTED)){
                tv_ConnectionState.setText("Connected");
                tv_ConnectionState.setBackgroundColor(Color.parseColor("#00FF00"));
            }
            else if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_BLE_DISCONNECTED)){
                tv_ConnectionState.setText("Not Connected");
                tv_ConnectionState.setBackgroundColor(Color.parseColor("#FF0000"));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_navigation_menu);

        myBleManager = BleManager.getInstance(getApplicationContext());
        myBno055DataManager = Bno055DataManager.getInstance();
        myGpsLocationManager = GPSLocationManager.getInstance(getApplicationContext(), this);
        myRunningActivityManager = runningActivityManager.getInstance();

        getGUIElements();
        manageVisibilities();
    }

    @Override
    protected void onResume(){
        super.onResume();

        //Updates the information in the text fields (connection state, calibration values);
        updateFields();

        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Const.INTENT_ACTION_NOTIFY_NEW_GPS_POSITION);
        myIntentFilter.addAction(Const.INTENT_ACTION_NOTIFY_BLE_MESSAGE_RECEIVED);
        myIntentFilter.addAction(Const.INTENT_ACTION_NOTIFY_BLE_CONNECTED);
        myIntentFilter.addAction(Const.INTENT_ACTION_NOTIFY_BLE_DISCONNECTED);
        this.registerReceiver(myBroadCastReceiver, myIntentFilter);


    }

    @Override
    protected void onPause(){
        super.onPause();
        this.unregisterReceiver(myBroadCastReceiver);
    }

    private void getGUIElements(){
        tv_ConnectionState = findViewById(R.id.tv_connection_state);
        tv_GPSPositionState = findViewById(R.id.tv_GPS_position_found);
        spinner_SelectDestOptions = findViewById(R.id.spinner_pnm_SelectDestOptions);
        tv_title_EnterAddressBelow = findViewById(R.id.tv_title_pnm_EnterBelow);
        ev_Address = findViewById(R.id.ev_pnm_Address);
        tv_title_Latitude = findViewById(R.id.tv_title_pnm_latitude);
        ev_Latitude = findViewById(R.id.ev_pnm_latitude);
        tv_title_Longitude = findViewById(R.id.tv_title_pnm_longitude);
        ev_Longitude = findViewById(R.id.ev_pnm_longitude);
        spinner_SelectView = findViewById(R.id.spinner_pnm_SelectView);

        //from https://stackoverflow.com/questions/1337424/android-spinner-get-the-selected-item-change-event
        spinner_SelectDestOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                manageVisibilities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                return;
            }

        });
    }

    private void manageVisibilities(){
        String selectedDestinationSelectionMethod = spinner_SelectDestOptions.getSelectedItem().toString();
        if(selectedDestinationSelectionMethod.equals("Address")){
            tv_title_EnterAddressBelow.setVisibility(TextView.VISIBLE);
            ev_Address.setVisibility(View.VISIBLE);
            tv_title_Latitude.setVisibility(View.INVISIBLE);
            ev_Latitude.setVisibility(View.INVISIBLE);
            tv_title_Longitude.setVisibility(View.INVISIBLE);
            ev_Longitude.setVisibility(View.INVISIBLE);
        }
        else{
            tv_title_EnterAddressBelow.setVisibility(TextView.INVISIBLE);
            ev_Address.setVisibility(View.INVISIBLE);
            tv_title_Latitude.setVisibility(View.VISIBLE);
            ev_Latitude.setVisibility(View.VISIBLE);
            tv_title_Longitude.setVisibility(View.VISIBLE);
            ev_Longitude.setVisibility(View.VISIBLE);
        }
    }

    protected void updateFields(){
        if(myBleManager.isConnected()){
            tv_ConnectionState.setText("Connected");
            tv_ConnectionState.setBackgroundColor(Color.parseColor("#00FF00"));
        }
        else{
            tv_ConnectionState.setText("Not Connected");
            tv_ConnectionState.setBackgroundColor(Color.parseColor("#FF0000"));
        }
        if(myGpsLocationManager.getGpsLocationFound()){
            tv_GPSPositionState.setText("GPS Position Found");
            tv_GPSPositionState.setBackgroundColor(Color.parseColor("#00FF00"));
        }
        else{
            tv_GPSPositionState.setText("GPS Position Not Found");
            tv_GPSPositionState.setBackgroundColor(Color.parseColor("#FF0000"));
        }
    }

    //on results
    protected void onBleMessageReceived(){

        try {
            String newMessage = myBleManager.getLastReceivedMessage();

            //necessary, because the analyseReceivedData function also updates the values in the Bno055DataManager singleton
            myBno055DataManager.analyseReceivedData(newMessage);

        }catch (final Exception ex){
            Log.e("ConCalMen-MessageRecvd", ex.getMessage());
        }
    }

    public void onClickStartPointNavigation(View view) {

        double latitude = 0;
        double longitude = 0;
        String selectedDestinationSelectionMethod = spinner_SelectDestOptions.getSelectedItem().toString();
        String selectedView = spinner_SelectView.getSelectedItem().toString();

        if(selectedDestinationSelectionMethod.equals("Address")){
            String myAddress = ev_Address.getText().toString();

            //from https://stackoverflow.com/questions/9698328/how-to-get-coordinates-of-an-address-in-android
            //other possibility is to use GeoLocation from osmDroid resp. osmBonusPack (import org.osmdroid.util.GeoPoint;)
            try {
                Geocoder myGeocoder = new Geocoder(getApplicationContext());
                List<Address> addresses;
                addresses = myGeocoder.getFromLocationName(myAddress, 1);
                if(addresses.size() > 0) {
                    latitude= addresses.get(0).getLatitude();
                    longitude= addresses.get(0).getLongitude();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Verify Address!", Toast.LENGTH_LONG).show();
                    return;
                }

            }catch(final Exception ex){
                Toast.makeText(getApplicationContext(), "ERROR: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

        }
        else{
            String strLatitude = ev_Latitude.getText().toString();
            String strLongitude = ev_Longitude.getText().toString();
            if( !(Utils.isCorrectDoubleFormat(strLatitude) && Utils.isCorrectDoubleFormat(strLongitude)) ){
                Toast.makeText(getApplicationContext(), "The contents of the Longitude and Latitude fields must be parseable to double!", Toast.LENGTH_LONG).show();
                return;
            }

            latitude = Double.parseDouble(strLatitude);
            longitude = Double.parseDouble(strLongitude);
        }


        Intent openPointNavigationActivity = new Intent(this, PointNavigationActivity.class);

        openPointNavigationActivity.putExtra("Latitude", latitude);
        openPointNavigationActivity.putExtra("Longitude", longitude);

        myRunningActivityManager.incrementPointNavigationID();
        if(selectedView.equals("Map")) {
            myRunningActivityManager.setPointNavigationMapViewActive();
        }
        else{
            myRunningActivityManager.setPointNavigationCompassViewActive();
        }

        startActivity(openPointNavigationActivity);
    }
}

