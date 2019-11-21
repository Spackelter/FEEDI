package edu.kit.itiv.feedi_app;

import android.util.Log;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * This class contains some small, helpful functions
 */
public class Utils {

    public Utils(){

    }

    /**
     * retunrs the current time in milliseconds since January 1st, 1970
     * @return the current time in milliseconds
     */
    public static long getCurrentTimeInMillis(){
        Calendar myCalendar = Calendar.getInstance();
        myCalendar.setTime(new Date());

        return myCalendar.getTimeInMillis();
    }


    public static void log(String myLogTag, String myLog){
        Log.d(myLogTag, myLog);
    }

    /**
     * converts a quaternion into euler angles
     * (NOT USED ANYMORE, but was used at one point to convert orientation information retrieved from the BNO055)
     * Assumes Quaternion is normalized
     * from http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/
     * @param quaternion (w,x,y,z)
     * @return
     */
    public static double[] quaternionToEuler(double[] quaternion){
        try {
            double[] euler = new double[3];

            double test = quaternion[1] * quaternion[2] + quaternion[0] * quaternion[3];

            if (test > 0.499) { //singularity - north pole
                euler[0] = 2 * Math.atan2(quaternion[1], quaternion[0]) * 180/Math.PI;
                euler[1] = 90;
                euler[2] = 0;
                return euler;
            }
            if (test < -0.499) { //singularity - south pole
                euler[0] = -2 * Math.atan2(quaternion[1], quaternion[0]) * 180/Math.PI;
                euler[1] = -90;
                euler[2] = 0;
                return euler;
            }

            double sqx = quaternion[1] * quaternion[1];
            double sqy = quaternion[2] * quaternion[2];
            double sqz = quaternion[3] * quaternion[3];

            euler[0] = Math.atan2(2 * quaternion[2] * quaternion[0] - 2 * quaternion[1] * quaternion[3], 1 - 2 * sqy * sqz) * 180/Math.PI;;
            euler[1] = Math.asin(2 * test) * 180/Math.PI;;
            euler[2] = Math.atan2(2 * quaternion[1] * quaternion[0] - 2 * quaternion[2] * quaternion[3], 1 - 2 * sqx * sqz) * 180/Math.PI;;

            return euler;
        }catch(final Exception ex){
            Log.e("QUAT2EUL", "ERROR: " + ex.getMessage());
            double[] euler = new double[3];
            euler[0]=-1000;
            euler[1]=-1000;
            euler[2]=-1000;
            return euler;
        }
    }

    //from https://stackoverflow.com/questions/3543729/how-to-check-that-a-string-is-parseable-to-a-double
    static boolean isCorrectDoubleFormat(String myString){

        final String Digits     = "(\\p{Digit}+)";
        final String HexDigits  = "(\\p{XDigit}+)";
// an exponent is 'e' or 'E' followed by an optionally
// signed decimal integer.
        final String Exp        = "[eE][+-]?"+Digits;
        final String fpRegex    =
                ("[\\x00-\\x20]*"+ // Optional leading "whitespace"
                        "[+-]?(" +         // Optional sign character
                        "NaN|" +           // "NaN" string
                        "Infinity|" +      // "Infinity" string

                        // A decimal floating-point string representing a finite positive
                        // number without a leading sign has at most five basic pieces:
                        // Digits . Digits ExponentPart FloatTypeSuffix
                        //
                        // Since this method allows integer-only strings as input
                        // in addition to strings of floating-point literals, the
                        // two sub-patterns below are simplifications of the grammar
                        // productions from the Java Language Specification, 2nd
                        // edition, section 3.10.2.

                        // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                        "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

                        // . Digits ExponentPart_opt FloatTypeSuffix_opt
                        "(\\.("+Digits+")("+Exp+")?)|"+

                        // Hexadecimal strings
                        "((" +
                        // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                        "(0[xX]" + HexDigits + "(\\.)?)|" +

                        // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                        "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                        ")[pP][+-]?" + Digits + "))" +
                        "[fFdD]?))" +
                        "[\\x00-\\x20]*");// Optional trailing "whitespace"

        if(Pattern.matches(fpRegex, myString)){
            return true;
        }
        return false;
    }

}
