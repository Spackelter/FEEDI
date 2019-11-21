package edu.kit.itiv.feedi_app;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenu extends AppCompatActivity {

    //for controlling the active mode
    private runningActivityManager myRunningActivityManager;

    //ble manager
    private BleManager myBleManager;

    //Manager for the (BNO055) sensor data
    private Bno055DataManager myBno055DataManager;

    //GUI elements
    protected TextView tvConnectionState;

    //broadCastReceiver
    private BroadcastReceiver myBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_BLE_CONNECTED)){
                tvConnectionState.setText("Connected");
                tvConnectionState.setBackgroundColor(Color.parseColor("#00FF00"));
            }
            else if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_BLE_DISCONNECTED)){
                tvConnectionState.setText("Not Connected");
                tvConnectionState.setBackgroundColor(Color.parseColor("#FF0000"));
            }
            else if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_BLE_MESSAGE_RECEIVED)){
                onBleMessageReceived();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        tvConnectionState = findViewById(R.id.tv_connection_state);
        myBleManager = BleManager.getInstance(getApplicationContext());
        myBno055DataManager = Bno055DataManager.getInstance();
        myRunningActivityManager = runningActivityManager.getInstance();
    }

    @Override
    public void onResume(){
        super.onResume();

        updateFields();

        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Const.INTENT_ACTION_NOTIFY_BLE_CONNECTED);
        myIntentFilter.addAction(Const.INTENT_ACTION_NOTIFY_BLE_DISCONNECTED);
        myIntentFilter.addAction(Const.INTENT_ACTION_NOTIFY_BLE_MESSAGE_RECEIVED);
        this.registerReceiver(myBroadCastReceiver, myIntentFilter);

        if(!myBleManager.BleEnabled()){
            requestBluetoothEnable();
        }
        if(!myBleManager.hasLocationPermission()){
            requestLocationPermission();
        }

    }

    @Override
    public void onPause(){
        super.onPause();

        this.unregisterReceiver(myBroadCastReceiver);
    }

    //on results

    /**
     * onActivityResult
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case Const.REQUEST_ENABLE_BLUETOOTH:
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, "App closes because bluetooth is disabled!", Toast.LENGTH_LONG); //Toast not visible because app closes instantly
                    finish();
                }
                break;

            case Const.REQUEST_FINE_LOCATION_PERMISSION:
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, "App closes because of missing location permissions!", Toast.LENGTH_LONG); //Toast not visible because app closes instantly
                    finish();
                }
                break;

            default:
                break;
        }


    }


    protected void onBleMessageReceived(){

        try {
            String newMessage = myBleManager.getLastReceivedMessage();

            //necessary, because the analyseReceivedData function also updates the values in the Bno055DataManager singleton
            myBno055DataManager.analyseReceivedData(newMessage);

        }catch (final Exception ex){
            Log.e("ConCalMen-MessageRecvd", ex.getMessage());
        }
    }

    //updates the connection state field
    protected void updateFields(){
        if(myBleManager.isConnected()){
            tvConnectionState.setText("Connected");
            tvConnectionState.setBackgroundColor(Color.parseColor("#00FF00"));
        }
        else{
            tvConnectionState.setText("Not Connected");
            tvConnectionState.setBackgroundColor(Color.parseColor("#FF0000"));
        }
    }

    //OnClick functions
    public void onClickConnectionCalibration(View view) {
        Intent openConnectionCalibrationActivity = new Intent(this, ConnectionCalibrationActivity.class);
        startActivity(openConnectionCalibrationActivity);
    }

    public void onClickPreferences(View view) {
        Intent openPreferencesActivity = new Intent(this, PreferencesMenuActivity.class);
        startActivity(openPreferencesActivity);
    }

    public void onClickCompass(View view) {
        Intent openCompassActivity = new Intent(this, CompassActivity.class);
        startActivity(openCompassActivity);
    }

    public void onClickPointToNorth(View view) {
        myRunningActivityManager.setPointToNorthActive();
        Intent openPointToNorthActivity = new Intent(this, PointToNorthActivity.class);
        startActivity(openPointToNorthActivity);
    }

    public void onClickPointNavigation(View view) {
        if(myRunningActivityManager.getPointNavigationCompassViewIsActive() ||
                myRunningActivityManager.getPointNavigationMapViewIsActive()){
            Intent openPointNavigationActivity = new Intent(this, PointNavigationActivity.class);
            startActivity(openPointNavigationActivity);
        }
        Intent openPointNavigationMenuActivity = new Intent(this, PointNavigationMenuActivity.class);
        startActivity(openPointNavigationMenuActivity);
    }

    public void onClickStopCurrentActivity(View view) {
        myBleManager.sendMessage("<S>");
        myRunningActivityManager.setToNoActiveMode();
    }

    //functions requesting permissions and bluetooth enable

    private void requestBluetoothEnable(){
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetoothIntent, Const.REQUEST_ENABLE_BLUETOOTH);
        Log.d(Const.LOG_TAG_BLE_MANAGER, "INFO: Requested that the user enable bluetooth. Try starting the scan again.");
    }

    public void requestLocationPermission(){
        if(Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Const.REQUEST_FINE_LOCATION_PERMISSION);
        }
        else{
            Toast.makeText(this, "ERROR: Forgot to enable ACCESS_FINE_LOCATION permission programatically", Toast.LENGTH_LONG);
            Log.d(Const.LOG_TAG_BLE_MANAGER, "ERROR: Forgot to enable ACCESS_FINE_LOCATION permission programatically");
        }
    }

}
