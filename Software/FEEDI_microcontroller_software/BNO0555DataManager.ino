//For FEEDI attached to the right side of the leg!
/***Variables***/
boolean bno055Discovered = false;
unsigned long previousMillisBle = 0;
unsigned long previousMillisBno = 0;

boolean sendData = true;

//Format of the following char: "C3112O125"
//order of calibration data: system, gyro, accel, mag
char buffcalorient[12]; //contains calibration of the BNO055 and user orientation


unsigned long Bno055SampleRate = 100;
unsigned long BLESampleRate = 200;
int dataToSend = 0;

//current orientation of the user
double angle = 90;


void setBno055Discovered(boolean discovered){
  bno055Discovered = discovered;
}


void updateCalibrationOrientation(){

  unsigned long currentMillisBno = millis();
  if(currentMillisBno-previousMillisBno<Bno055SampleRate){
    return;
  }

  previousMillisBno = currentMillisBno;

  uint8_t system, gyro, accel, mag = 0;
  bno.getCalibration(&system, &gyro, &accel, &mag);

  String calibrationOrientation = "(C" + String(system,DEC) + String(gyro,DEC) + String(accel,DEC) + String(mag,DEC);

  imu::Vector<3> magn = bno.getVector(Adafruit_BNO055::VECTOR_MAGNETOMETER);
  imu::Vector<3> grav = bno.getVector(Adafruit_BNO055::VECTOR_GRAVITY);

  /*Serial.println(magn.x());
  Serial.println(magn.y());
  Serial.println(magn.z());
  Serial.println(grav.x());
  Serial.println(grav.y());
  Serial.println(grav.z()); For debugging purposes*/

  double frontBackAngle = 0;
  double sideAngle = 0;
  
  frontBackAngle = asin(-grav.y()/9.81); //alpha
  if(frontBackAngle == PI/2){
    sideAngle = 0; //beta
  }
  else{
    sideAngle = asin(grav.z()/(9.81*cos(frontBackAngle))); //beta
  }

  //turning back magnetic field coordinate system to compensate leg angles
  double my= sin(frontBackAngle) * cos(sideAngle) * magn.x(); 
  my += cos(frontBackAngle)*magn.y();
  my +=  sin(frontBackAngle) * sin(sideAngle)* magn.z();
  double mz = - sin(sideAngle) * magn.x() + cos(sideAngle) * magn.z();
  
  angle = 90;
  angle = atan2(-mz,-my) * 180/PI;

  // to ensure the angle is between 0 and 360 degrees
  while(angle>=360){
    angle-=360;
  }
  while(angle<0){
    angle+=360;
  }

  calibrationOrientation += "O" + String(angle,0) + ")"; //resolution: 1 degree
  strcpy(buffcalorient,string2char(calibrationOrientation));

  //Serial.print(angle); //for debugging purposes
  //Serial.print("   "); //for debugging purposes
  
}

void sendCalOr(){

  unsigned long currentMillisBle = millis();
  if(currentMillisBle-previousMillisBle<BLESampleRate){
    return;
  }

  //switch case structure is a relict of a previous implementation method,
  //where different strings matching together were send one at a time
  switch(dataToSend){
  
    case 0: //send calibration
    updateCalibrationOrientation();
    if(sendData){
      bluetoothSend(buffcalorient);
      //Serial.println(buffcalorient); //for debugging purposes
    }
    dataToSend = 0;
    previousMillisBle = currentMillisBle;
    return;
    break;

      
    default:
    dataToSend = 0;
    previousMillisBle = currentMillisBle;
    return;
    break;

    
  }
  
  return;
  
}

//returns the current orientation of the user
double getAngle(){
 updateCalibrationOrientation();
 return angle;
}
