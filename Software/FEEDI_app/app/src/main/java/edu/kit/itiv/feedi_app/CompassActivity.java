package edu.kit.itiv.feedi_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class CompassActivity extends AppCompatActivity {

    //ble manager
    private BleManager myBleManager;

    //Manager for the (BNO055) sensor data
    private Bno055DataManager myBno055DataManager;

    //GUI elements
    private TextView tvConnectionState;
    private TextView tvOrientationAngle;
    private ImageView ivCompassImage;

    //current Compass orientation
    private float lastCompassOrientation = 0;

    //broadCastReceiver
    private BroadcastReceiver myBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_BLE_MESSAGE_RECEIVED)){
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
        setContentView(R.layout.activity_compass);

        myBleManager = BleManager.getInstance(getApplicationContext());
        myBno055DataManager = Bno055DataManager.getInstance();

        ivCompassImage = findViewById(R.id.img_compass);
        tvConnectionState = findViewById(R.id.tv_connection_state);
        tvOrientationAngle = findViewById(R.id.tv_orientation);
    }

    @Override
    protected void onResume(){
        super.onResume();

        //Updates the information in the text fields (connection state, calibration values);
        updateFields();

        IntentFilter myIntentFilter = new IntentFilter();
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

    protected void onBleMessageReceived(){

        try {
            String newMessage = myBleManager.getLastReceivedMessage();

            int messagetype =  myBno055DataManager.analyseReceivedData(newMessage);

            if (messagetype == 0){

                float newOrient = myBno055DataManager.getOrientation();
                rotateCompass(newOrient);
                tvOrientationAngle.setText(Float.toString(newOrient));

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
        float newOr = myBno055DataManager.getOrientation();
        tvOrientationAngle.setText(Float.toString(newOr));
        rotateCompass(newOr);
    }

    protected void rotateCompass(float newOrientation){
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                lastCompassOrientation,
                -newOrientation,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        ivCompassImage.startAnimation(ra);
        lastCompassOrientation = -newOrientation;
    }

}
