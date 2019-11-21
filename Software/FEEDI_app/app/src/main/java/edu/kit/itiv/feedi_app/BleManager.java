package edu.kit.itiv.feedi_app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BleManager {

    //"Global instance"
    private static BleManager instance = null;

    //Context
    Context myContext;

    //Handlers
    private android.os.Handler myHandler;

    //Bluetooth related variables and constants
    private UUID myAdvertisedUuid;
    private BluetoothManager myBluetoothManager;
    private BluetoothAdapter myBluetoothAdapter;
    private GattClientCallback myGattClientCallback;
    private BluetoothGatt myBluetoothGatt;
    private BluetoothGattCharacteristic myGattCharacteristicTx;
    private BluetoothGattCharacteristic myGattCharacteristicRx;
    private BluetoothGattDescriptor myBluetoothGattDescriptor;
    private List myBluetoothGattDescriptorList = new ArrayList<BluetoothGattDescriptor>();

    //status variables
    private boolean currentlyBLEScanning = false;
    private boolean currentlyBLEConnected = false;
    private boolean rxInitialized = false;
    private boolean txInitialized = false;
    private boolean bleConnected = false;
    private long timeOfLastReceivedMessage = Utils.getCurrentTimeInMillis();

    //Thread verifying the connection state
    private Thread myVerifyConnectionThread;

    //results
    private List myBLEScanResults = new ArrayList<String>();
    private Map<String, BluetoothDevice> myScanResults;
    private List myFoundBluetoothDevices = new ArrayList<BluetoothDevice>();
    private String myLastReceivedMessage = "initial value";

    /**
     * Constructor (is private to ensure there is only one activity
     * @param context
     */
    private BleManager(Context context){

        myContext = context;

        myBluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        myBluetoothAdapter = myBluetoothManager.getAdapter();

        if(!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(myContext, "YOUR DEVICE CANNOT USE BLUETOOTH LOW ENERGY", Toast.LENGTH_LONG);
            Log.d(Const.LOG_TAG_BLE_MANAGER, "ERROR: Device has no BLE capacity!");
            return;
        }

    }

    /**
     * Constructor. If an instance of this class is initialized using this constructor, the
     * scanLE18() function only returns devices advertising the UUID indicated in filterUuid
     * @param context
     * @param filterUuid
     */
    private BleManager(Context context, UUID filterUuid){

        myContext = context;
        myAdvertisedUuid = filterUuid;

        myBluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        myBluetoothAdapter = myBluetoothManager.getAdapter();

        if(!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(myContext, "YOUR DEVICE CANNOT USE BLUETOOTH LOW ENERGY", Toast.LENGTH_LONG);
            Log.d(Const.LOG_TAG_BLE_MANAGER, "ERROR: Device has no BLE capacity!");
            return;
        }

    }


    //bluetooth functions

    //Scan for devices (BLE (&BT?))
    public int scanLE18(){
        Utils.log(Const.LOG_TAG_BLE_MANAGER,"Entered scanLE18");

        if(!hasPermissions() || currentlyBLEScanning){
            Utils.log(Const.LOG_TAG_BLE_MANAGER, "Application does not have the required permissions -> App needs to be closed");
            return Const.CODE_LACKS_OF_PERMISSION;
        }
        else if(currentlyBLEScanning){
            Utils.log(Const.LOG_TAG_BLE_MANAGER,"Application is already BLE scanning -> return");
            return Const.CODE_ALREADY_BLE_SCANNING;
        }

        myBLEScanResults = new ArrayList<String>();
        myFoundBluetoothDevices = new ArrayList<BluetoothDevice>();

        myScanResults = new HashMap<>();
        BluetoothAdapter.LeScanCallback myBLEScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {


                if( myAdvertisedUuid!=null && !parseUUIDs(scanRecord).contains(myAdvertisedUuid)){
                    return;
                }

                addScanResult18(bluetoothDevice, myScanResults);
                myBLEScanResults.add(bluetoothDevice.getName() + "," + bluetoothDevice.getAddress());
                myFoundBluetoothDevices.add(bluetoothDevice);

            }
        };
        currentlyBLEScanning = true;

        myBluetoothAdapter.startLeScan(myBLEScanCallback);

        myHandler = new android.os.Handler();
        myHandler.postDelayed(() -> {

            if(currentlyBLEScanning && myBluetoothAdapter != null && myBluetoothAdapter.isEnabled()){
                currentlyBLEScanning = false;
                myBluetoothAdapter.stopLeScan(myBLEScanCallback);

                if(myScanResults.isEmpty()){
                    return;
                }

                Intent notifyScanComplete = new Intent();
                notifyScanComplete.setAction(Const.INTENT_ACTION_NOTIFY_BLE_SCAN_COMPLETE);
                myContext.sendBroadcast(notifyScanComplete);

            }

            myHandler = null;
        }, Const.MY_SCAN_PERIOD);

        return Const.CODE_OK;
    }

    public void connectToDevice(BluetoothDevice myNewDevice){
        connectDevice(myNewDevice);
    }

    public void disconnect(){
        bleConnected = false;
        disconnectGattServer();
    }

    private void connectDevice(BluetoothDevice myDevice){

        myGattClientCallback = new GattClientCallback();
        myBluetoothGatt = myDevice.connectGatt(myContext, false, myGattClientCallback);

        bleConnected=true;
        timeOfLastReceivedMessage = Utils.getCurrentTimeInMillis();

        Intent notifyMessageReceived = new Intent();
        notifyMessageReceived.setAction(Const.INTENT_ACTION_NOTIFY_BLE_CONNECTED);
        myContext.sendBroadcast(notifyMessageReceived);

        startVerifyingConnectionState(); // starts a thread verifying the connection state periodically

    }

    private void startVerifyingConnectionState(){

        myVerifyConnectionThread = new Thread(){
            @Override
            public void run(){
                while(bleConnected){

                    sendMessage("<d>");

                    try{
                        Thread.sleep(5000);
                    }catch(final Exception ex){
                        Utils.log(Const.LOG_TAG_BLE_MANAGER, "ERROR during sleep of myVerifyConnectionThread.");
                    }

                    final long currentTime = Utils.getCurrentTimeInMillis();
                    if(currentTime-timeOfLastReceivedMessage > Const.MY_TIMEOUT){
                        bleConnected = false;
                        disconnectGattServer();
                    }

                }
            }
        };
        myVerifyConnectionThread.start();

    }

    private void disconnectGattServer(){
        currentlyBLEConnected = false;
        if(myBluetoothGatt != null){
            myBluetoothGatt.disconnect();
            myBluetoothGatt.close();
        }
        Intent notifyMessageReceived = new Intent();
        notifyMessageReceived.setAction(Const.INTENT_ACTION_NOTIFY_BLE_DISCONNECTED);
        myContext.sendBroadcast(notifyMessageReceived);
    }

    /**
     * Sends a message via the established ble connection
     * @param myMessage
     * @return 0 in case of success, -1 if not connected or one of the gatt characteristics is null, -2 if an other error occurred
     */
    public int sendMessage(String myMessage){

        if(!currentlyBLEConnected || myGattCharacteristicTx==null || myGattCharacteristicRx==null){
            return -1;
        }

        byte[] myMessageBytes = new byte[0];
        try{
            myMessageBytes = myMessage.getBytes("UTF-8");
        }catch(final Exception ex){
            Utils.log(Const.LOG_TAG_BLE_MANAGER,"ERROR: Failed to convert message string to byte array");
        }

        myGattCharacteristicTx.setValue(myMessageBytes);
        boolean success = myBluetoothGatt.writeCharacteristic(myGattCharacteristicTx);

        if(!success){
            return -2;
        }

        return 0;

    }

    //functions requesting permissions, bluetooth enable, etc. and checking permissions


    private boolean hasPermissions(){
        boolean result = true;
        if(!BleEnabled()){
            result = false;
        }
        if(!hasLocationPermission()){
            result = false;
        }
        return result;
    }

    public boolean BleEnabled(){
        boolean result = myBluetoothAdapter != null && myBluetoothAdapter.isEnabled();
        return result;
    }

    public boolean hasLocationPermission() {
        if(Build.VERSION.SDK_INT >= 23){
            return myContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public void addScanResult18(BluetoothDevice newBluetoothDevice, Map<String, BluetoothDevice> scanResults){
        String newDeviceAddress = newBluetoothDevice.getAddress();
        myScanResults.put(newDeviceAddress, newBluetoothDevice);
    }



    //additional functions

    //Copied from The6thSenseApp-master
    //Also contained in the official Adafruit bluefruit LE app
    // Filtering by custom UUID is broken in Android 4.3 and 4.4, see:
    //   http://stackoverflow.com/questions/18019161/startlescan-with-128-bit-uuids-doesnt-work-on-native-android-ble-implementation?noredirect=1#comment27879874_18019161
    // This is a workaround function from the SO thread to manually parse advertisement data.
    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData, offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            Log.e(Const.LOG_TAG_BLE_MANAGER, e.toString());
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
    }

    //END: Copied from The6thSenseApp-master - was also included in the official Adafruit ble app

    //Callbacks
    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (status == BluetoothGatt.GATT_FAILURE) {
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer();
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                currentlyBLEConnected = true;
                myBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }

            BluetoothGattService myGattServiceUart = gatt.getService(MyUuids.UART_UUID);
            myGattCharacteristicTx = myGattServiceUart.getCharacteristic(MyUuids.TX_UUID);
            myGattCharacteristicRx = myGattServiceUart.getCharacteristic(MyUuids.RX_UUID);
            myGattCharacteristicTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            rxInitialized = myBluetoothGatt.setCharacteristicNotification(myGattCharacteristicRx, true);
            txInitialized = myBluetoothGatt.setCharacteristicNotification(myGattCharacteristicTx, true);

            myBluetoothGattDescriptor = myGattCharacteristicRx.getDescriptor(MyUuids.CHARACTERISTIC_CONFIG_UUID);
            if (myBluetoothGattDescriptor == null) {
                Utils.log(Const.LOG_TAG_BLE_MANAGER, "ERROR: myBluetoothGattDescriptor == null");
                return;
            }

            myBluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            myBluetoothGatt.writeDescriptor(myBluetoothGattDescriptor);



        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (characteristic == myGattCharacteristicRx) {
                byte[] receivedMessageBytes = characteristic.getValue();
                String receivedMessage = null;
                try {
                    receivedMessage = new String(receivedMessageBytes, "UTF-8");
                } catch (final Exception ex) {
                    Utils.log(Const.LOG_TAG_BLE_MANAGER, "ERROR in onCharacteristicChanged: " + ex);
                }

                myLastReceivedMessage = receivedMessage;
                timeOfLastReceivedMessage = Utils.getCurrentTimeInMillis();

                Intent notifyMessageReceived = new Intent();
                notifyMessageReceived.setAction(Const.INTENT_ACTION_NOTIFY_BLE_MESSAGE_RECEIVED);
                myContext.sendBroadcast(notifyMessageReceived);

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }
    }


    //getters and setters


    //To access a "global" instance of BleManager from anywhere
    public static BleManager getInstance(Context context) {
          if (instance == null) {
              instance = new BleManager(context, MyUuids.UART_UUID);
          }
          return instance;
    }

    /**
     * returns the discovered devices
     * @return a map of the discovered devices, null if no scan for devices has been executed
     */
    public Map<String, BluetoothDevice> getDiscoveredDevices(){
        return myScanResults;
    }

    /**
     * returns the scan results as an arraylist of strings in the format: deviceName,deviceAddress
     * @return an arraylist containing the devices, null if no scan for devices has been done
     */
    public List getScanResult(){
        return  myBLEScanResults;
    }

    public List getFoundDevices(){
        return myFoundBluetoothDevices;
    }

    /**
     * returns the last received message
     * @return a String containing the last received message, "initial value" if no messages have been received yet
     */
    public String getLastReceivedMessage(){
        return myLastReceivedMessage;
    }

    public boolean isConnected(){
        return bleConnected;
    }


}



