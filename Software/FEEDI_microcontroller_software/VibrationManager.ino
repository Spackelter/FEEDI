/****Variables****/

//vibration state
boolean vibrate = false;
boolean currentlyVibrating = false; //to control the vibration cycle, true if  vibrate=true and currently vibrating
unsigned long previousMillis = 0;

//vibration parameters
float myAngle = 0;
int myMaxIntensity = 255;
unsigned int myVibrationOnTime = 1000;
unsigned int myVibrationOffTime = 1000;

//vibration intensities
int myVibrationIntensityFront = 0;
int myVibrationIntensityRight = 0;
int myVibrationIntensityLeft = 0;
int myVibrationIntensityBack = 0;


/****Functions****/
//helpful functions

void setVibrationAngle(float newAngle){
  myAngle = newAngle;
}

void setVibrationCycleParameters(unsigned int newVibCycleOnTime, unsigned int newVibCycleOffTime){
  myVibrationOnTime = newVibCycleOnTime;
  myVibrationOffTime = newVibCycleOffTime;
}

/**
 * updates the previousMillis value.
 */
void updatePreviousMillis(){
  previousMillis = millis();
}

/**
 * returns the angle (float, 0<=result<=360) from a String received via bluetooth.
 * The received string is in the format <1,angle,maxIntensityFactor,vibrationCycle>
 */
float getNewAngle(String receivedMessage){
  int startPos = receivedMessage.indexOf(",")+1; 
  int endPos = receivedMessage.indexOf(";");
  String newAngle = receivedMessage.substring(startPos,endPos);
  return newAngle.toFloat();
}

/**
 * returns the max intensity factor (float, 0<=result<=1) from a String received via bluetooth.
 * The received string is in the format <1,angle,maxIntensityFactor,vibrationCycle>
 */
float getNewMaxIntensityFactor(String receivedMessage){
  int startPos = receivedMessage.indexOf(";")+1; 
  int endPos = receivedMessage.indexOf(":");
  String newMaxIntensFac = receivedMessage.substring(startPos,endPos);
  return newMaxIntensFac.toFloat();
}

/**
 * returns the vibration on time cycle (int, in millis, 0<=result, 0 means permanent vibration) 
 * from a String received via bluetooth.
 * The received string is in the format <1,angle,maxIntensityFactor,vibrationCycle>
 */
int getNewVibrationOnTime(String receivedMessage){
  int startPos = receivedMessage.indexOf("Y")+1; 
  int endPos = receivedMessage.indexOf(">");
  String newVibrationCycle = receivedMessage.substring(startPos,endPos);
  return newVibrationCycle.toInt();
}

/**
 * returns the vibration cycle off time (int, in millis, 0<=result, 0 means permanent vibration) 
 * from a String received via bluetooth.
 * The received string is in the format <1,angle,maxIntensityFactor,vibrationCycle>
 */
int getNewVibrationOffTime(String receivedMessage){
  int startPos = receivedMessage.indexOf("N")+1; 
  int endPos = receivedMessage.indexOf(">");
  String newVibrationCycle = receivedMessage.substring(startPos,endPos);
  return newVibrationCycle.toInt();
}

//Functions responsible for the actual vibration

/**
 * Re-initializes the parameters necessary to vibrate.
 * For a vibration, this function needs to be called ONLY ONCE. From that point on,
 * the doVibrate function needs to be called in every loop cycle (see comment of the doVibrate function)
 */
void startVibrating(){
  vibrate = true;
  updatePreviousMillis();
}

/**
 * Stops the vibration.
 */
void stopVibrating(){
  vibrate = false;
  dontVibrate();
}

/**
 * Adapts an angle a  so 0<=a<360
 */
float adaptAngle(float myAngle){
  float resultingAngle = myAngle;
  while(resultingAngle >= 360){
    resultingAngle = resultingAngle - 360;
  }
  while(resultingAngle < 0){
    resultingAngle = resultingAngle + 360;
  }
  return resultingAngle;
}

/**
 * Returns the segment number of an angle between 0 and 360°
 */
int getSegment(float angle){
  if(angle>=0 && angle<90){
    return 1;
  }
  else if(angle>=90 && angle<180){
    return 2;
  }
  else if(angle>=180 && angle<270){
    return 3;
  }
  else if(angle>=270 && angle<360){
    return 4;
  }

  return -1;
}

/**
 * resets the values of all vibration intensities before calculating new intensities
 */
void resetAllVibrationIntensities(){
  myVibrationIntensityFront=0;
  myVibrationIntensityRight=0;
  myVibrationIntensityLeft=0;
  myVibrationIntensityBack=0;
}

/**
 * Recalculates the vibration intensity values off all vibration motors
 * (Each time, only 2 vibration intensities need to be calculated)
 */
void setNewVibrationIntensities(int segment, int myTempAngle, int maxIntensity){
  switch(segment){
    case 1:
    {
      float angleInRad = myTempAngle * PI /180;
      myVibrationIntensityFront = maxIntensity*cos(angleInRad);
      myVibrationIntensityRight = maxIntensity*sin(angleInRad);
      }
      break;
    case 2:
    {
      float angleInRad = (myTempAngle-90) * PI /180;
      myVibrationIntensityRight = maxIntensity*cos(angleInRad);
      myVibrationIntensityBack = maxIntensity*sin(angleInRad);
      }
      break;
    case 3:
    {
      float angleInRad = (myTempAngle-180) * PI /180;
      myVibrationIntensityBack = maxIntensity*cos(angleInRad);
      myVibrationIntensityLeft = maxIntensity*sin(angleInRad);
      }
      break;
    case 4:
    {
      float angleInRad = (myTempAngle-270) * PI /180;
      myVibrationIntensityLeft = maxIntensity*cos(angleInRad);
      myVibrationIntensityFront = maxIntensity*sin(angleInRad);
      }
      break;
    default:
      
      break;
  }
}

/**
 * Starts the vibration with the calculated intensities
 */
void writeVibrationValues(){
  analogWrite(vibrationMotorFront, myVibrationIntensityFront);
  analogWrite(vibrationMotorRight, myVibrationIntensityRight);
  analogWrite(vibrationMotorLeft, myVibrationIntensityLeft);
  analogWrite(vibrationMotorBack, myVibrationIntensityBack);
  //Serial.println("Front: " + (String) myVibrationIntensityFront); //for debugging purposes
  //Serial.println("Right: " + (String) myVibrationIntensityRight); //for debugging purposes
  //Serial.println("Left: " + (String) myVibrationIntensityLeft); //for debugging purposes
  //Serial.println("Back: " + (String) myVibrationIntensityBack); //for debugging purposes
}

/**
 * Called when the band is vibrating and within the vibration cycle, the vibration is on
 * to check if, within the vibration cycle, the vibration shall be turned off. 
 */
boolean stillVibrate(){
  unsigned long currentMillis = millis();
  if(currentMillis - previousMillis > myVibrationOnTime){
    dontVibrate();
    currentlyVibrating = false;
    updatePreviousMillis();
    return false;  
  }
  else{
    return true;
  }
}

/**
 * Called when the band is vibrating and within the vibration cycle, the vibration is off
 * to check if, within the vibration cycle, the vibration shall be turned on. 
 */
boolean stillNotVibrate(){
  unsigned long currentMillis = millis();
  if(currentMillis - previousMillis < myVibrationOffTime){
    dontVibrate(); //not necessary?
    return true;
  }
  else{
    currentlyVibrating = true;
    updatePreviousMillis();
    return false;
  }
}

/**
 * Executes a vibration with 4 motors
 */
void doVibrate(float angle, int maxIntensity){
 
  if(myVibrationOffTime!=0){ //vibrationOffTime = 0 shall lead to permanent vibration
    if(!currentlyVibrating){
      if(stillNotVibrate()){
        return;
      }
    }
    else{
      if(!stillVibrate()){
        return;
      }
    }
  } 
  
  float myNewAngle = adaptAngle(angle);
  int myNewSegment = getSegment(myNewAngle);
  if(myNewSegment==-1){
    return;
  }
  resetAllVibrationIntensities();
  setNewVibrationIntensities(myNewSegment, myNewAngle, maxIntensity);
  writeVibrationValues();
  
}

/**
 * To make sure the vibration is stopped, all vibration are turned off by this function
 * (even if they are off already)
 */
void dontVibrate(){
  analogWrite(vibrationMotorFront, 0);
  analogWrite(vibrationMotorRight, 0);
  analogWrite(vibrationMotorLeft, 0);
  analogWrite(vibrationMotorBack, 0);
}


//getters and setters
/**
 * Sets the vibration parameters (angle, max intensity and vibration cycle) from the data contained in a String
 * received via bluetooth. First, The program needs to check if the string is formatted correctly.
 * (Correct format: <1,angleValue;maxIntensityValue:vibrationCycleValue>
 */
void setVibrationParameters(String myReceivedData){
  myAngle = getNewAngle(myReceivedData);
  myMaxIntensity = 255 * getNewMaxIntensityFactor(myReceivedData);
  myVibrationOnTime = getNewVibrationOnTime(myReceivedData);
  myVibrationOffTime = getNewVibrationOffTime(myReceivedData);
}

/**
 * Returns a String containing the vibration parameters (mode, angle, max intensity and vibration cycle)
 * For debugging purposes
 */
String getVibrationDataString(){
    //String resultingString = "m=" + String(vibration_mode);
    //resultingString += ", a=" + String(myAngle);
    String resultingString = ", a=" + String(myAngle);
    resultingString += ", i=" + String(myMaxIntensity);
    resultingString += ", t=" + String(myVibrationOnTime);
    if(getVibrate()){
      resultingString += ", vibrating";
    }
    else{
      resultingString += ", not vibrating";
    }
    return resultingString;
}

/**
 * Returns the max intensity value (int)
 */
int getMyMaxIntensity(){
  return myMaxIntensity;
}

/**
 * Returns the vibration angle (float)
 */
float getMyVibrationAngle(){
  return myAngle;
}

/**
 * Returns the boolean indicating if currently, a vibration cycle is in programs
 */
boolean getVibrate(){
  return vibrate;
}
