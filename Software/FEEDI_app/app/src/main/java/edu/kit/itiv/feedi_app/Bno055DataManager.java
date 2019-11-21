package edu.kit.itiv.feedi_app;

import android.util.Log;

public class Bno055DataManager {

    //"global" instance of the class
    private static Bno055DataManager instance = null;

    //Orientation: 0 = north, 90 = east,...
    private float orientation = 0;
    //Calibration values between 0 and 3 as received from the BNO055
    private int calSys = 0;
    private int calGyro = 0;
    private int calAcc = 0;
    private int calMag = 0;

    //default constructor
    private Bno055DataManager(){

    }

    /**
     * Updates the calibration or orientation values if the string receivedData contains info about them
     * @param receivedData
     * @return 0 if the calibration and orientation values were updated, -1 else
     */
    public int analyseReceivedData(String receivedData){
        try {

            if (receivedData.startsWith("(C") && receivedData.contains("O") && receivedData.endsWith(")")) {
                int pos = receivedData.indexOf("C") + 1;
                calSys = Integer.parseInt(receivedData.substring(pos, pos+1));
                calGyro = Integer.parseInt(receivedData.substring(pos+1, pos+2));
                calAcc = Integer.parseInt(receivedData.substring(pos+2, pos+3));
                calMag = Integer.parseInt(receivedData.substring(pos+3, pos+4));

                int startPos = receivedData.indexOf("O") + 1;
                int stopPos = receivedData.indexOf(")");
                orientation = Float.parseFloat(receivedData.substring(startPos, stopPos));

                return 0;
            }

            return -1;

        }catch(final Exception ex){
            Log.e("BNO-analyseReceivedData", ex.getMessage() );
            return -1;
        }
    }

    //Getters and Setters

    //To access a "global" instance of BleManager from anywhere
    public static Bno055DataManager getInstance() {
            if (instance == null) {
                instance = new Bno055DataManager();
            }
            return instance;
    }

    public float getOrientation(){
        return orientation;
    }

    /**
     * returns an array withth:
     * at 0 calSys,
     * at 1 calGyro,
     * at 2 calAcc,
     * at 3 calMag
     */
    public int[] getCalibration(){
        int cal[] = new int[4];

        cal[0] = calSys;
        cal[1] = calGyro;
        cal[2] = calAcc;
        cal[3] = calMag;

        return cal;
    }

}

