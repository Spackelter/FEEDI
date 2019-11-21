package edu.kit.itiv.feedi_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.SharedPreferences;
import android.widget.SeekBar;
import android.widget.TextView;

public class PreferencesPointNavigationActivity extends AppCompatActivity {

    //To save user preferences
    private SharedPreferences pointNavigationPreferences;

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
        setContentView(R.layout.activity_preferences_point_navigation);

        getGUIElements();
        configureSeekBars();
        //mode 0 = mode private
        pointNavigationPreferences = getSharedPreferences("pointNavigationPreferences", 0);
    }

    @Override
    public void onResume(){
        applyUserPreferences();
        super.onResume();
    }

    private void savePnUserPreferences(){
        int vibCycleOnTime = sbVibrationCycleOnTime.getProgress();
        int vibCycleOffTime = sbVibrationCycleOffTime.getProgress();
        int vibCycleTotalTime = sbVibrationCycleTotalTime.getProgress();
        int vibTimeBtwCycles = sbTimeBtwVibrationCycles.getProgress();

        SharedPreferences.Editor myPreferencesEditor = pointNavigationPreferences.edit();
        myPreferencesEditor.putInt("CycleOnTime", vibCycleOnTime);
        myPreferencesEditor.putInt("CycleOffTime", vibCycleOffTime);
        myPreferencesEditor.putInt("CycleTotalTime", vibCycleTotalTime);
        myPreferencesEditor.putInt("TimeBetweenCycles", vibTimeBtwCycles);
        myPreferencesEditor.commit();
    }

    private void applyUserPreferences(){
        int vibCycleOnTime = pointNavigationPreferences.getInt("CycleOnTime", 1);
        int vibCycleOffTime = pointNavigationPreferences.getInt("CycleOffTime", 5);
        int vibCycleTotalTime = pointNavigationPreferences.getInt("CycleTotalTime", 5);
        int vibTimeBtwCycles = pointNavigationPreferences.getInt("TimeBetweenCycles", 60);

        sbVibrationCycleOnTime.setProgress(vibCycleOnTime);
        sbVibrationCycleOffTime.setProgress(vibCycleOffTime);
        sbVibrationCycleTotalTime.setProgress(vibCycleTotalTime);
        sbTimeBtwVibrationCycles.setProgress(vibTimeBtwCycles);
    }

    private void resetPreferences(){
        SharedPreferences.Editor myPreferencesEditor = pointNavigationPreferences.edit();
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
        tvVibrationCycleOnTime = findViewById(R.id.tv_pn_vibration_cycle_on_time);
        tvVibrationCycleOffTime = findViewById(R.id.tv_pn_vibration_cycle_off_time);
        tvVibrationCycleTotalTime = findViewById(R.id.tv_pn_vibration_cycle_total_time);
        tvTimeBtwVibrationCycles = findViewById(R.id.tv_pn_time_btw_vibCycles);
        sbVibrationCycleOnTime = findViewById(R.id.sb_pn_vibration_cycle_on_time);
        sbVibrationCycleOffTime = findViewById(R.id.sb_pn_vibration_cycle_off_time);
        sbVibrationCycleTotalTime = findViewById(R.id.sb_pn_vibration_cycle_total_time);
        sbTimeBtwVibrationCycles = findViewById(R.id.sb_pn_time_btw_vibCycles);
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
    public void onClickSavePnPreferences(View view) {
        savePnUserPreferences();
    }

    public void onClickResetPnPreferences(View view) {
        resetPreferences();
    }
}
