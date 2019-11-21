package edu.kit.itiv.feedi_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.StrictMode;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;


public class PointNavigationActivity extends AppCompatActivity {

    //for controlling the active mode
    private runningActivityManager myRunningActivityManager;

    //own and target location
    private double targetLatitude = 0;
    private double targetLongitude = 0;

    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private float currentRotation = 0;

    //Place of user preferences
    private SharedPreferences pointNavigationPreferences;

    //ble manager
    private BleManager myBleManager;

    //Manager for the (BNO055) sensor data
    private Bno055DataManager myBno055DataManager;

    //GPS Location Manager
    private GPSLocationManager myGpsLocationManager;

    //GUI elements
    private ImageView ivDestinationDiscImage;
    private ImageView ivCompassImage;
    private TextView tvConnectionState;
    private TextView tvGPSPosFoundState;
    private TextView tv_Orientation;
    private TextView tv_Destination;
    private TextView tv_DistanceToTarget;

    //current Compass and Destination disc orientations
    private float lastCompassOrientation = 0;
    private float lastDestinationDiscOrientation = 0;

    boolean navigationStarted = false;

    //for the mapview mode
    Marker userPosMarker = null;
    Marker targetPosMarker = null;
    MapView map;
    GeoPoint currentPosition = new GeoPoint(0.0, 0.0);
    GeoPoint targetPosition = new GeoPoint(0.0, 0.0);

    //for switching between views
    ConstraintLayout myCompassLayout;
    RelativeLayout myMapLayout;

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
            else if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_NEW_GPS_POSITION)){
                tvGPSPosFoundState.setText("GPS Position Found");
                tvGPSPosFoundState.setBackgroundColor(Color.parseColor("#00FF00"));
                onLocationUpdate();
            }
            else if(intent.getAction().equals(Const.INTENT_ACTION_NOTIFY_GPS_POSITION_LOST)){
                tvGPSPosFoundState.setText("GPS Position Not Found");
                tvGPSPosFoundState.setBackgroundColor(Color.parseColor("#FF0000"));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_navigation);

        pointNavigationPreferences = getSharedPreferences("pointNavigationPreferences", 0);

        myBleManager = BleManager.getInstance(getApplicationContext());
        myBno055DataManager = Bno055DataManager.getInstance();
        myGpsLocationManager = GPSLocationManager.getInstance(getApplicationContext(), this);
        myRunningActivityManager = runningActivityManager.getInstance();

        //see https://github.com/MKergall/osmbonuspack/wiki/Tutorial_0
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getGUIElements();
        configureMap();
        defineMarkers();
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(myRunningActivityManager.getPointNavigationMapViewIsActive()){
            setMapViewVisible();
        }
        else{
            setCompassViewVisible();
        }

        targetLatitude = getIntent().getDoubleExtra("Latitude",0);
        targetLongitude = getIntent().getDoubleExtra("Longitude",0);

        //Updates the information in the text fields (connection state, calibration values);
        updateFields();

        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Const.INTENT_ACTION_NOTIFY_BLE_MESSAGE_RECEIVED);
        myIntentFilter.addAction(Const.INTENT_ACTION_NOTIFY_BLE_CONNECTED);
        myIntentFilter.addAction(Const.INTENT_ACTION_NOTIFY_BLE_DISCONNECTED);
        myIntentFilter.addAction(Const.INTENT_ACTION_NOTIFY_NEW_GPS_POSITION);
        this.registerReceiver(myBroadCastReceiver, myIntentFilter);

        if(myGpsLocationManager.getGpsLocationFound()){
            startNavigation();
        }

    }

    @Override
    protected void onPause(){
        super.onPause();
        this.unregisterReceiver(myBroadCastReceiver);
    }

    private void getGUIElements(){
        myCompassLayout = (ConstraintLayout) findViewById(R.id.cl_compassLayout);
        myMapLayout = (RelativeLayout) findViewById(R.id.rl_mapLayout);
        tvConnectionState = findViewById(R.id.tv_connection_state);
        tvGPSPosFoundState = findViewById(R.id.tv_GPS_position_found);
        ivDestinationDiscImage = findViewById(R.id.img_pn_DestinationDisc);
        ivCompassImage = findViewById(R.id.img_pn_Compass);
        tv_Orientation = findViewById(R.id.tv_pn_orientation);
        tv_Destination = findViewById(R.id.tv_pn_Destination);
        tv_DistanceToTarget = findViewById(R.id.tv_pn_distance_to_target);
        map = (MapView) findViewById(R.id.map);
    }

    private void configureMap(){
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(15);
        GeoPoint myStartPoint = new GeoPoint(49.012650, 8.409400);
        if(currentLatitude == 0 && currentLongitude == 0){
            //The point where the map is centered at the beginning stays the ITIV
        }
        else{
            myStartPoint = new GeoPoint(currentLatitude, currentLongitude);
        }
        mapController.setCenter(myStartPoint);
        //Reload map (to apply all the changes)
        map.invalidate();

    }

    private void defineMarkers(){
        userPosMarker = new Marker(map);
        targetPosMarker = new Marker(map);
    }

    private void updateUserPositionOnMap(){
        //in the case the GPS signal has been lost
        if(currentLatitude == 0 || currentLongitude ==0){
            return;
        }
        currentPosition = new GeoPoint(currentLatitude, currentLongitude);
        if(userPosMarker != null && map.getOverlays().contains(userPosMarker)){
            map.getOverlays().remove(userPosMarker);
        }

        userPosMarker = new Marker(map);
        userPosMarker.setPosition(currentPosition);
        userPosMarker.setRotation(-currentRotation);
        map.getOverlays().add(userPosMarker);
        userPosMarker.setIcon(getResources().getDrawable(R.drawable.navigation_arrow_slightly_bigger));
        userPosMarker.setTitle("User position");

        map.invalidate();
    }

    public void updateTargetPositionOnMap(){
        targetPosition = new GeoPoint(targetLatitude, targetLongitude);
        if(targetPosMarker != null && map.getOverlays().contains(targetPosMarker)){
            map.getOverlays().remove(targetPosMarker);
        }

        targetPosMarker = new Marker(map);
        targetPosMarker.setPosition(targetPosition);
        targetPosMarker.setAnchor(Marker.ANCHOR_LEFT, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(targetPosMarker);
        targetPosMarker.setIcon(getResources().getDrawable(R.drawable.target_flag_smaller_filled));
        targetPosMarker.setTitle("Target position");

        map.invalidate();
    }

    protected void onBleMessageReceived(){

        try {
            String newMessage = myBleManager.getLastReceivedMessage();

            int messagetype =  myBno055DataManager.analyseReceivedData(newMessage);

            if (messagetype == 0){

                float newOrient = myBno055DataManager.getOrientation();
                currentRotation = newOrient;
                rotateCompass(newOrient);
                int newOrientInt = Math.round(newOrient);
                tv_Orientation.setText(Integer.toString(newOrientInt));

                if(myGpsLocationManager.getGpsLocationFound()) {
                    float newDestinationAngle = calculateDestinationAngle();
                    float newDiscAngle = newDestinationAngle + lastCompassOrientation; //because the discs position is relative to the compass which turns around
                    rotateDestinationDisc(newDiscAngle);
                    int newDestAngleInt = Math.round(newDestinationAngle);
                    tv_Destination.setText(Integer.toString(newDestAngleInt));
                    float newDistanceToTarget = (float) calculateDistanceToTarget();
                    int newDistToTarget = Math.round(newDistanceToTarget);
                    newDistanceToTarget = ((float)newDistToTarget)/1000;
                    tv_DistanceToTarget.setText(Float.toString(newDistanceToTarget));

                    updateUserPositionOnMap();
                }

            }
        }catch (final Exception ex){
            Log.e("ConCalMen-MessageRecvd", ex.getMessage());
        }
    }

    //main Navigation functions
    private void startNavigation(){
        navigationStarted = true;
        navigate(myRunningActivityManager.getCurrentPointNavigationID());
    }

    private void navigate(int pointNavigationID){

        /*
        * To prevent the navigation from going on after the stop button has been pressed
        * && to prevent 2 or more different point navigations from being "executed at the same time"
        * if, within the delay between vibration cycles of one navigation, a new point navigation has been started
         */
        if(myRunningActivityManager.getPointNavigationIsActive() && pointNavigationID == myRunningActivityManager.getCurrentPointNavigationID()) {
            float newDestinationAngle = calculateDestinationAngle();
            String myCommand = BleMessageFactory.getPointNavigationCommand(newDestinationAngle, pointNavigationPreferences);

            myBleManager.sendMessage(myCommand);

            int vibrationCycleTotalTime = 5 + pointNavigationPreferences.getInt("CycleTotalTime", 5);
            int timeBetweenVibrationCycles = pointNavigationPreferences.getInt("TimeBetweenCycles", 60);

            int delay = vibrationCycleTotalTime + timeBetweenVibrationCycles;
            delay = delay * 1000; //delay needed in milliseconds
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigate(pointNavigationID);
                }
            }, delay);

        }


    }

    /**
     * Calculates the destination angle (in degrees from north)
     * @return he destination angle
     */
    float calculateDestinationAngle(){

        if(currentLatitude == targetLatitude){
            if(currentLongitude < targetLongitude){
                return 90;
            }
            else{
                return -90;
            }
        }

        double destinationAngle = 0;
        double targetLatInRad = targetLatitude * Math.PI / 180;

        double distLat = 111000; //distance between two latitude degrees in meters
        double distLong = 111000 * Math.cos(targetLatInRad); //distance between two longitude degrees in meters
        double deltaLat = targetLatitude-currentLatitude;
        double deltaLong = targetLongitude-currentLongitude;
        double deltaX = deltaLong * distLong;
        double deltaY = deltaLat * distLat;

        destinationAngle = Math.atan2(deltaX, deltaY); //result of atan2 is between -Pi and Pi see https://www.tutorialspoint.com/java/lang/math_atan2.htm
        destinationAngle = destinationAngle * 180 / Math.PI; //reconvert from radians to degrees
        //Math.atan2 already handles the different cases of 2 positive angles, 2 negative angles or 1 positive an 1 negative angle
        //-> no further post-processing depending on the signs of the arguments needed

        if(destinationAngle<0){
            destinationAngle+=360;
        }

        float myDestinationAngle = (float) destinationAngle;

        return myDestinationAngle;
    }

    /**
     * Uses the haversine formula to calculate the distance (in meters) to the target location
     * see https://www.movable-type.co.uk/scripts/latlong.html
     * @return the distance (air-line distance) between the user position and the target position
     */
    private double calculateDistanceToTarget(){
        double distance = 0;
        double R = 6371000; //The Earth's radius in meters

        double currentLatInRad = currentLatitude * Math.PI / 180;
        double targetLatInRad = targetLatitude * Math.PI / 180;
        double deltaLatInRad = targetLatInRad - currentLatInRad;
        double deltaLongInRad = (targetLongitude-currentLongitude) * Math.PI / 180;

        double a = Math.sin(deltaLatInRad/2) * Math.sin(deltaLatInRad/2);
        a+= Math.cos(currentLatInRad) * Math.cos(targetLatInRad) * Math.sin(deltaLongInRad/2) * Math.sin(deltaLongInRad/2);

        double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        distance = R*c;

        return distance;
    }

    private void onLocationUpdate(){
        currentLatitude = myGpsLocationManager.getCurrentPosLatitude();
        currentLongitude = myGpsLocationManager.getCurrentPosLongitude();

        if(navigationStarted==false){
            startNavigation();
        }

        float newDestinationAngle = calculateDestinationAngle();
        float newDiscAngle = newDestinationAngle+lastCompassOrientation; //because the disc's position is relative to the compass which turns around
        rotateDestinationDisc(newDiscAngle);
        int newDestAngleInt = Math.round(newDestinationAngle);
        tv_Destination.setText(Integer.toString(newDestAngleInt));
        float newDistanceToTarget = (float) calculateDistanceToTarget();
        int newDistToTarget = Math.round(newDistanceToTarget);
        newDistanceToTarget = ((float)newDistToTarget)/1000;
        tv_DistanceToTarget.setText(Float.toString(newDistanceToTarget));

        updateUserPositionOnMap();
    }

    //diverse functions
    protected void updateFields(){
        if(myBleManager.isConnected()){
            tvConnectionState.setText("Connected");
            tvConnectionState.setBackgroundColor(Color.parseColor("#00FF00"));
        }
        else{
            tvConnectionState.setText("Not Connected");
            tvConnectionState.setBackgroundColor(Color.parseColor("#FF0000"));
        }
        if(myGpsLocationManager.getGpsLocationFound()){
            tvGPSPosFoundState.setText("GPS Position Found");
            tvGPSPosFoundState.setBackgroundColor(Color.parseColor("#00FF00"));
        }
        else{
            tvGPSPosFoundState.setText("GPS Position Not Found");
            tvGPSPosFoundState.setBackgroundColor(Color.parseColor("#FF0000"));
        }
        float newOr = myBno055DataManager.getOrientation();
        tv_Orientation.setText(Float.toString(newOr));
        rotateCompass(newOr);

        updateUserPositionOnMap();
        updateTargetPositionOnMap();
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
        ra.setDuration(100);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        ivCompassImage.startAnimation(ra);
        lastCompassOrientation = -newOrientation;
    }

    protected void rotateDestinationDisc(float newOrientation){
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                lastDestinationDiscOrientation,
                newOrientation,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(100);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        ivDestinationDiscImage.startAnimation(ra);
        lastDestinationDiscOrientation = newOrientation;
    }

    private void setCompassViewVisible(){
        myMapLayout.setVisibility(View.GONE);
        myCompassLayout.setVisibility(View.VISIBLE);
        myRunningActivityManager.setPointNavigationCompassViewActive();
    }

    private void setMapViewVisible(){
        myCompassLayout.setVisibility(View.GONE);
        myMapLayout.setVisibility(View.VISIBLE);
        myRunningActivityManager.setPointNavigationMapViewActive();
    }


    //OnClick methods
    public void onClickPnMapView(View view){
        setMapViewVisible();
    }

    public void onClickPnCompassView(View view){
        setCompassViewVisible();
    }

    public void onClickPnStop(View view) {
        myBleManager.sendMessage("<S>");
        Intent openPointNavigationMenuActivity = new Intent(this, PointNavigationMenuActivity.class);
        navigationStarted=false;
        myRunningActivityManager.setToNoActiveMode(); //to disable the navigation loop
        startActivity(openPointNavigationMenuActivity);
    }

}
