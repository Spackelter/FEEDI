package edu.kit.itiv.feedi_app;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

public class ConnectionCalibrationActivity extends AppCompatActivity {

    //ble manager
    private BleManager myBleManager;

    //Manager for the (BNO055) sensor data
    private Bno055DataManager myBno055DataManager;

    //GUI elements
    protected TextView tvConnectionState;
    protected TextView tvSystemCalibration;
    protected TextView tvGyroCalibration;
    protected TextView tvAcceleroCalibration;
    protected TextView tvMagnetoCalibration;
    protected ListView lvBleDevices;
    protected int selectedIteminListView = -1;
    protected ArrayAdapter myBleListAdapter;

    //broadCastReceiver
    private BroadcastReceiver myBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_BLE_SCAN_COMPLETE)){
                onBleScanResults();
            }
            else if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_BLE_MESSAGE_RECEIVED)){
                onBleMessageReceived();
            }
            else if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_BLE_CONNECTED)){
                tvConnectionState.setText("Connected");
                tvConnectionState.setBackgroundColor(Color.parseColor("#00FF00"));
            }
            else if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_BLE_DISCONNECTED)){
                tvConnectionState.setText("Not Connected");
                tvConnectionState.setBackgroundColor(Color.parseColor("#FF0000"));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_calibration);


        myBleManager = BleManager.getInstance(getApplicationContext());
        myBno055DataManager = Bno055DataManager.getInstance();

        tvConnectionState = findViewById(R.id.tv_connection_state);
        tvSystemCalibration = findViewById(R.id.tv_systemCal);
        tvGyroCalibration = findViewById(R.id.tv_gyroCal);
        tvAcceleroCalibration = findViewById(R.id.tv_accelCal);
        tvMagnetoCalibration = findViewById(R.id.tv_magnetoCal);
        lvBleDevices = findViewById(R.id.lv_found_devices);
    }

    @Override
    protected void onResume(){
        super.onResume();

        //Updates the information in the text fields (connection state, calibration values);
        updateFields();

        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Const.INTENT_ACTION_NOTIFY_BLE_SCAN_COMPLETE);
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

    //on results

    protected void onBleScanResults(){

        List myScanResults = myBleManager.getScanResult();
        Map<String, BluetoothDevice> myScannedDevices = myBleManager.getDiscoveredDevices();
        List<BluetoothDevice> myDevices = myBleManager.getFoundDevices();

        myBleListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, myDevices);
        lvBleDevices.setAdapter(myBleListAdapter);
        myBleListAdapter.notifyDataSetChanged();

        lvBleDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedIteminListView = i;
                lvBleDevices.invalidate();
            }
        });

    }

    protected void onBleMessageReceived(){

        try {
            String newMessage = myBleManager.getLastReceivedMessage();

            int messagetype =  myBno055DataManager.analyseReceivedData(newMessage);

            if (messagetype == 0) {

                int[] newCal = myBno055DataManager.getCalibration();
                tvSystemCalibration.setText(Integer.toString(newCal[0]));
                tvGyroCalibration.setText(Integer.toString(newCal[1]));
                tvAcceleroCalibration.setText(Integer.toString(newCal[2]));
                tvMagnetoCalibration.setText(Integer.toString(newCal[3]));

            }
        }catch (final Exception ex){
            Log.e("ConCalMen-MessageRecvd", ex.getMessage());
        }
    }


    protected void updateFields(){
        if(myBleManager.isConnected()){
            tvConnectionState.setText("Connected");
            tvConnectionState.setBackgroundColor(Color.parseColor("#00FF00"));
        }
        else{
            tvConnectionState.setText("Not Connected");
            tvConnectionState.setBackgroundColor(Color.parseColor("#FF0000"));
        }
        int[] newCal = myBno055DataManager.getCalibration();
        tvSystemCalibration.setText(Integer.toString(newCal[0]));
        tvGyroCalibration.setText(Integer.toString(newCal[1]));
        tvAcceleroCalibration.setText(Integer.toString(newCal[2]));
        tvMagnetoCalibration.setText(Integer.toString(newCal[3]));
    }

    //onClick functions
    public void onClickBluetoothScan(View view) {
        int scanFeedBack = myBleManager.scanLE18();
        if(scanFeedBack == Const.CODE_LACKS_OF_PERMISSION){

        }
    }

    public void onClickConnectDisconnect(View view) {

        if(myBleManager.isConnected()){
            myBleManager.disconnect();
            return;
        }

        BluetoothDevice selectedDevice = (BluetoothDevice) lvBleDevices.getItemAtPosition(selectedIteminListView);
        if(selectedDevice == null){
            Toast.makeText(this, "First, you need to select a device in the device list", Toast.LENGTH_LONG).show();
            return;
        }
        myBleManager.connectToDevice(selectedDevice);

    }

    public void onClickCalibrationInfoButton(View view) {
        Intent openCalibrationInformationActivity = new Intent(this, CalibrationInformationActivity.class);
        startActivity(openCalibrationInformationActivity);
    }


}
