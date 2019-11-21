//Statuses of FEEDI
const int NO_MODE = 0;
const int DEBUG = 1;
const int POINT_TO_NORTH = 2;
const int POINT_NAVIGATION = 3;

int currentMode = 0;

//vibration cycle data (in milliseconds) (for point to North)
unsigned int vibCycleOnTimePtn = 100;
unsigned int vibCycleOffTimePtn = 700;
unsigned int vibCycleTotalTimePtn = 5000;
unsigned long timeBtwVibCyclesPtn = 60000;
unsigned long timeLastVibrationCycleStartedStopped=0;

//vibration cycle data (in milliseconds) (point to Navigation)
unsigned int vibCycleOnTimePn = 100;
unsigned int vibCycleOffTimePn = 700;
unsigned int vibCycleTotalTimePn = 5000;
unsigned long timeBtwVibCyclesPn = 60000;
unsigned long timeLastVibrationCycleStarted=0;
int targetAngle=0; //the angle between the target position and the current user position

//variable to manage the vibration cycle (for both navigation types)
boolean vibrationCycleInProgress = false;


void onModeChanged(){
  if(currentMode == NO_MODE){
    vibrationCycleInProgress = false;
    dontVibrate();
  }
  else if(currentMode == POINT_TO_NORTH){
    vibrationCycleInProgress = true;
    timeLastVibrationCycleStartedStopped = millis(); //measures when last vibration cycle started or stopped (whatever happened last)
    setVibrationCycleParameters(vibCycleOnTimePtn, vibCycleOffTimePtn);
  }
  else if(currentMode == POINT_NAVIGATION){
    vibrationCycleInProgress = true;
    timeLastVibrationCycleStarted = millis(); //measures when last vibration cycle started 
    setVibrationCycleParameters(vibCycleOnTimePn, vibCycleOffTimePn);
  }
}

void updateCycleValues(int feediMode, String myMessage){
  if(feediMode==POINT_TO_NORTH){
    int startPos = 2;
    int endPos = 4;
    String temp = myMessage.substring(startPos,endPos);
    //temp string now contains "XY" where X.Y is the vibration cycle on time in seconds
    vibCycleOnTimePtn = 100* temp.toInt();
    startPos = 4;
    endPos = 6;
    temp = myMessage.substring(startPos,endPos);
    //temp string now contains "XY" where X.Y is the vibration cycle off time in seconds
    vibCycleOffTimePtn = 100* temp.toInt();

    startPos = 6;
    endPos = 8;
    temp = myMessage.substring(startPos,endPos);
    vibCycleTotalTimePtn = 1000* temp.toInt();

    startPos = 8;
    endPos = 11;
    temp = myMessage.substring(startPos,endPos);
    timeBtwVibCyclesPtn = 1000* temp.toInt();
  }
  else if(feediMode==POINT_NAVIGATION){
    int startPos = 2;
    int endPos = 5;
    String temp = myMessage.substring(startPos,endPos);
    //temp now contains the target angle (value between 0 and 360, not negative, resolution: 1°)
    targetAngle = temp.toInt();
    
    startPos = 5;
    endPos = 7;
    temp = myMessage.substring(startPos,endPos);
    //temp string now contains "XY" where X.Y is the vibration cycle on time in seconds
    vibCycleOnTimePn = 100* temp.toInt();
    
    startPos = 7;
    endPos = 9;
    temp = myMessage.substring(startPos,endPos);
    //temp string now contains "XY" where X.Y is the vibration cycle off time in seconds
    vibCycleOffTimePn = 100* temp.toInt();

    startPos = 9;
    endPos = 11;
    temp = myMessage.substring(startPos,endPos);
    vibCycleTotalTimePn = 1000* temp.toInt();
  }
}

void switchModeFromMessage(String receivedMessage){
  if(receivedMessage.indexOf("<S>")!=-1){
    currentMode = NO_MODE;
  }
  else if(receivedMessage.startsWith("<n")){ //vibrate towards north
    if(receivedMessage.length() < 12 || receivedMessage.indexOf(">")==-1){
      //An ERROR occurred
      return;
    }
    currentMode = POINT_TO_NORTH;
    updateCycleValues(currentMode, receivedMessage);
  }
  else if(receivedMessage.startsWith("<v")){ //start vibration (used for point navigation)
    if(receivedMessage.length() < 12 || receivedMessage.indexOf(">")==-1){
      //Serial.println("ERROR - pn"); //for debugging purposes
      return;
    }
      //Serial.println("Entering point navigation mode"); //for debugging purposes
    currentMode = POINT_NAVIGATION;
    updateCycleValues(currentMode, receivedMessage);
  }
  onModeChanged();
}

//Checks which mode FEEDI is currently in and does the tasks required in that mode
//(like sending data to the smartphone, vibrating,...)
void doModeTasks(){
  //Send calibration values and orientation angle (in any case)
    sendCalOr();

  if(currentMode == NO_MODE){
    return;
  }
  else if(currentMode == POINT_TO_NORTH){

    if(timeBtwVibCyclesPtn != 0){
      unsigned long currentTime = millis();
      if(vibrationCycleInProgress){
        if(currentTime-timeLastVibrationCycleStartedStopped > vibCycleTotalTimePtn){
          timeLastVibrationCycleStartedStopped = currentTime;
          dontVibrate();
          vibrationCycleInProgress = false;
          return;
        }
      }
      else{
        if(currentTime-timeLastVibrationCycleStartedStopped > timeBtwVibCyclesPtn){
          timeLastVibrationCycleStartedStopped = currentTime;
          vibrationCycleInProgress = true;
        }
        else{
          return;
        }
      }
      
    }

    double vibrationAngle = getAngle();
    vibrationAngle = -vibrationAngle; //because the device needs to vibrate towards -userOrientation
    setVibrationAngle(vibrationAngle);
    
    doVibrate(vibrationAngle, getMyMaxIntensity());
    return;
  }
  else if(currentMode == POINT_NAVIGATION){

    if(!vibrationCycleInProgress){
      dontVibrate();
      return;
    }
    
    unsigned long currentTime = millis();
    if(currentTime-timeLastVibrationCycleStarted > vibCycleTotalTimePn){ //the next vibration cycle will be triggered by the smartphone again
      dontVibrate();
      vibrationCycleInProgress = false;
      return;
    }
 

    double currentOrientation = getAngle();
    double vibrationAngle = targetAngle - currentOrientation;
    setVibrationAngle(vibrationAngle);
    
    doVibrate(vibrationAngle, getMyMaxIntensity());
    return;
  }
  
}
