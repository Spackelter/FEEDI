package edu.kit.itiv.feedi_app;

import android.content.SharedPreferences;

public class BleMessageFactory {

    /**
     * default constructor
     */
    public BleMessageFactory(){

    }

    /**
     * Creates a point to North command string to send to FEEDI based on the user preferences
     * @param pointToNorthPreferences
     * @return a point to North command string to send to FEEDI based on the user preferences
     */
    public static String getPointToNorthCommand(SharedPreferences pointToNorthPreferences){

        //vibCycleOnTime & vibCycleOffTime in tenths of a second, to shorten the string to send
        //1+ because vibCycleOnTime has to be atleast 0.1 seconds
        int vibCycleOnTime = 1 + pointToNorthPreferences.getInt("CycleOnTime", 1);
        int vibCycleOffTime = pointToNorthPreferences.getInt("CycleOffTime", 5);

        // vibCycleTotalTime and vibTimeBtwCycles in seconds
        //5+ because vibCycleTotalTime has to be atleast 5 seconds
        int vibCycleTotalTime = 5 + pointToNorthPreferences.getInt("CycleTotalTime", 5);
        int vibTimeBtwCycles = pointToNorthPreferences.getInt("TimeBetweenCycles", 60);

        String resultingMessage = "<n";
        if(vibCycleOnTime<10){
            resultingMessage+="0";
        }
        resultingMessage += Integer.toString(vibCycleOnTime);
        if(vibCycleOffTime<10){
            resultingMessage+="0";
        }
        resultingMessage += Integer.toString(vibCycleOffTime);
        if(vibCycleTotalTime<10){
            resultingMessage+="0";
        }
        resultingMessage += Integer.toString(vibCycleTotalTime);
        if(vibTimeBtwCycles<100){
            resultingMessage+="0";
            if(vibTimeBtwCycles<10){
                resultingMessage+="0";
            }
        }
        resultingMessage += Integer.toString(vibTimeBtwCycles) + ">";

        return resultingMessage;
    }

    /**
     * Creates a point Navigation command string to send to FEEDI based on the user preferences
     * @param destinationAngle : the angle of the destination in relation to north (NOT the user orientation)
     * @param pointNavigationPreferences
     * @return a point Navigation command string to send to FEEDI based on the user preferences
     */
    public static String getPointNavigationCommand(float destinationAngle, SharedPreferences pointNavigationPreferences){

        //vibCycleOnTime & vibCycleOffTime in tenths of a second, to shorten the string to send
        //1+ because vibCycleOnTime has to be atleast 0.1 seconds
        int vibCycleOnTime = 1 + pointNavigationPreferences.getInt("CycleOnTime", 1);
        int vibCycleOffTime = pointNavigationPreferences.getInt("CycleOffTime", 5);

        // vibCycleTotalTime
        //5+ because vibCycleTotalTime has to be atleast 5 seconds
        int vibCycleTotalTime = 5 + pointNavigationPreferences.getInt("CycleTotalTime", 5);
        //angle prepared to convert it to an int and to make sure it is between 0 and 360 degrees
        int angle = prepareAngle(destinationAngle);


        String resultingMessage = "<v";

        if(angle<100){
            resultingMessage+="0";
        }
        if(angle<10){
            resultingMessage+="0";
        }
        resultingMessage += Integer.toString(angle);

        if(vibCycleOnTime<10){
            resultingMessage+="0";
        }
        resultingMessage += Integer.toString(vibCycleOnTime);
        if(vibCycleOffTime<10){
            resultingMessage+="0";
        }
        resultingMessage += Integer.toString(vibCycleOffTime);
        if(vibCycleTotalTime<10){
            resultingMessage+="0";
        }
        resultingMessage += Integer.toString(vibCycleTotalTime)+ ">";

        return resultingMessage;
    }

    /**
     * Rounds an angle and makes sure it is between 0° and 360°
     * @param angle
     * @return an angle between 0° and 360° with the precision of 1°
     */
    private static int prepareAngle(float angle){
        int myResult = Math.round(angle);
        while(myResult<0){
            myResult += 360;
        }
        while(myResult>=360){
            myResult = myResult-360;
        }
        return myResult;
    }

}
