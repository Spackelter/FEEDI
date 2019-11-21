package edu.kit.itiv.feedi_app;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class PreferencesPointToNorthActivity extends AppCompatActivity {

    //To save user preferences
    private SharedPreferences pointToNorthPreferences;

    //BleManager
    BleManager myBleManager;

    //GUI elements
    private TextView tvVibrationCycleOnTime;
    private TextView tvVibrationCycleOffTime;
    private TextView tvVibrationCycleTotalTime;
    private TextView tvTimeBtwVibrationCycles;
    private SeekBar sbVibrationCycleOnTime;
    private SeekBar sbVibrationCycleOffTime;
    private SeekBar sbVibrationCycleTotalTime;
    private SeekBar sbTimeBtwVibrationCycles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_point_to_north);

        myBleManager = BleManager.getInstance(getApplicationContext());

        getGUIElements();
        configureSeekBars();
        //mode 0 = mode private
        pointToNorthPreferences = getSharedPreferences("pointToNorthPreferences", 0);
    }

    @Override
    public void onResume(){
        applyUserPreferences();
        super.onResume();
    }

    private void saveUserPreferences(){
        int vibCycleOnTime = sbVibrationCycleOnTime.getProgress();
        int vibCycleOffTime = sbVibrationCycleOffTime.getProgress();
        int vibCycleTotalTime = sbVibrationCycleTotalTime.getProgress();
        int vibTimeBtwCycles = sbTimeBtwVibrationCycles.getProgress();

        SharedPreferences.Editor myPreferencesEditor = pointToNorthPreferences.edit();
        myPreferencesEditor.putInt("CycleOnTime", vibCycleOnTime);
        myPreferencesEditor.putInt("CycleOffTime", vibCycleOffTime);
        myPreferencesEditor.putInt("CycleTotalTime", vibCycleTotalTime);
        myPreferencesEditor.putInt("TimeBetweenCycles", vibTimeBtwCycles);
        myPreferencesEditor.commit();
    }

    private void applyUserPreferences(){
        int vibCycleOnTime = pointToNorthPreferences.getInt("CycleOnTime", 1);
        int vibCycleOffTime = pointToNorthPreferences.getInt("CycleOffTime", 5);
        int vibCycleTotalTime = pointToNorthPreferences.getInt("CycleTotalTime", 5);
        int vibTimeBtwCycles = pointToNorthPreferences.getInt("TimeBetweenCycles", 60);

        sbVibrationCycleOnTime.setProgress(vibCycleOnTime);
        sbVibrationCycleOffTime.setProgress(vibCycleOffTime);
        sbVibrationCycleTotalTime.setProgress(vibCycleTotalTime);
        sbTimeBtwVibrationCycles.setProgress(vibTimeBtwCycles);
    }

    private void resetPreferences(){
        SharedPreferences.Editor myPreferencesEditor = pointToNorthPreferences.edit();
        myPreferencesEditor.putInt("CycleOnTime", 1);
        myPreferencesEditor.putInt("CycleOffTime", 5);
        myPreferencesEditor.putInt("CycleTotalTime", 5);
        myPreferencesEditor.putInt("TimeBetweenCycles", 60);
        myPreferencesEditor.commit();
        sbVibrationCycleOnTime.setProgress(1);
        sbVibrationCycleOffTime.setProgress(5);
        sbVibrationCycleTotalTime.setProgress(5);
        sbTimeBtwVibrationCycles.setProgress(60);
    }

    private void getGUIElements(){
        tvVibrationCycleOnTime = findViewById(R.id.tv_ptn_vibrationCycleOnTime);
        tvVibrationCycleOffTime = findViewById(R.id.tv_ptn_vibrationCycleOffTime);
        tvVibrationCycleTotalTime = findViewById(R.id.tv_ptn_vibrationCycleTotalTime);
        tvTimeBtwVibrationCycles = findViewById(R.id.tv_ptn_time_btw_vibrationCycles);
        sbVibrationCycleOnTime = findViewById(R.id.sb_ptn_vibrationCycleOnTime);
        sbVibrationCycleOffTime = findViewById(R.id.sb_ptn_vibrationCycleOffTime);
        sbVibrationCycleTotalTime = findViewById(R.id.sb_ptn_vibrationCycleTotalTime);
        sbTimeBtwVibrationCycles = findViewById(R.id.sb_ptn_time_btw_vibration_cycles);
    }

    private void configureSeekBars(){

        sbVibrationCycleOnTime.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {

                        double min = 0.1;
                        double step = 0.1;
                        double value = min + (progress * step);
                        tvVibrationCycleOnTime.setText(doubleToString(value));

                    }
                }
        );

        sbVibrationCycleOffTime.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {

                        double min = 0.0;
                        double step = 0.1;
                        double value = min + (progress * step);
                        tvVibrationCycleOffTime.setText(doubleToString(value));

                    }
                }
        );

        sbVibrationCycleTotalTime.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {

                        int min = 5;
                        int step = 1;
                        int value = min + (progress * step);
                        tvVibrationCycleTotalTime.setText(Integer.toString(value));

                    }
                }
        );

        sbTimeBtwVibrationCycles.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {


                            int min = 0;
                            int step = 1;
                            int value = min + (progress * step);
                            tvTimeBtwVibrationCycles.setText(Integer.toString(value));

                    }
                }
        );

    }


    //help functions

    /**
     *
     * @param value
     * @return a string formatted as follows: #0.0
     */
    private String doubleToString(double value){
        String strdouble = Double.toString(value);
        int decPointPlace = strdouble.indexOf(".");
        if(decPointPlace != -1){
            strdouble = strdouble.substring(0, decPointPlace+2);
        }
        return strdouble;
    }

    //onClick functions
    public void onClickPtnSavePreferences(View view) {
        saveUserPreferences();
        if(runningActivityManager.getInstance().getPointToNorthIsActive()){
            //update the navigation parameters in FEEDI
            if(myBleManager.isConnected()){
                String startCommand = BleMessageFactory.getPointToNorthCommand(pointToNorthPreferences);
                myBleManager.sendMessage(startCommand);
            }
        }
    }

    public void onClickPtnResetPreferences(View view) {
        resetPreferences();
    }
}
