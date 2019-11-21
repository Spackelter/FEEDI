
/*************************************************************************
 * This program controls FEEDI
 * 
  */

//Used libraries
 #include <Arduino.h>
 #include <SPI.h>
 #include "Adafruit_BLE.h"
 #include "Adafruit_BluefruitLE_SPI.h"
 #include "Adafruit_BluefruitLE_UART.h"
 
 #include <Wire.h>
 #include <Adafruit_Sensor.h>
 #include <Adafruit_BNO055.h>
 #include <utility/imumaths.h>

 #include "BluefruitConfig.h" //file copied from the examples

 #if SOFTWARE_SERIAL_AVAILABLE
  #include <SoftwareSerial.h>
 #endif

 //Constants
 #define FACTORYRESET_ENABLE      1
 #define MINIMUM_FIRMWARE_VERSION "0.6.6"
 #define MODE_LED_BEHAVIOUR       "MODE"

// Create the bluefruit object,
//hardware SPI, using SCK/MOSI/MISO hardware SPI pins and then user selected CS/IRQ/RST
Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);
//Create BNO055
Adafruit_BNO055 bno = Adafruit_BNO055();

//Constants
const int ledPin =  LED_BUILTIN;// the number of the LED pin

const int vibrationMotorFront = 11; // where the n channel mosfet connecting the vibration motor to gnd is connected
const int vibrationMotorRight = 6;
const int vibrationMotorLeft = 10;
const int vibrationMotorBack = 5; 


// A small helper
void error(const __FlashStringHelper*err) {
  //Serial.println(err);
  while (1);
}


/**************************************************************************/
/*!
    @brief  Sends a char array via bluetooth
*/
/**************************************************************************/
void bluetoothSend(char inputs[]){
  
// Send characters to Bluefruit
    //Serial.print("[Send] ");
    //Serial.println(inputs);

    ble.print("AT+BLEUARTTX=");
    ble.println(inputs);

    // check response stastus
    if (! ble.waitForOK() ) {
      //Serial.println(F("Failed to send?"));
    }
}



//Copied from https://coderwall.com/p/zfmwsg/arduino-string-to-char
char* string2char(String command){
    if(command.length()!=0){
        char *p = const_cast<char*>(command.c_str());
        return p;
    }
}

/**************************************************************************/
/*!
    @brief  Sets up the HW an the BLE module (this function is called
            automatically on startup)
*/
/**************************************************************************/
void setup() {

  pinMode(ledPin, OUTPUT);
  pinMode(vibrationMotorFront, OUTPUT);
  pinMode(vibrationMotorRight, OUTPUT);
  pinMode(vibrationMotorLeft, OUTPUT);
  pinMode(vibrationMotorBack, OUTPUT);
  analogWrite(vibrationMotorFront,0);
  analogWrite(vibrationMotorRight,0);
  analogWrite(vibrationMotorLeft,0);
  analogWrite(vibrationMotorBack,0);


/* Initialise the module */
  //Serial.print(F("Initialising the Bluefruit LE module: "));

  if ( !ble.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  //Serial.println( F("OK!") );

  if ( FACTORYRESET_ENABLE )
  {
    /* Perform a factory reset to make sure everything is in a known state */
    //Serial.println(F("Performing a factory reset: "));
    if ( ! ble.factoryReset() ){
      error(F("Couldn't factory reset"));
    }
  }

  
  /* Initialise the BNO055 sensor */
  if(bno.begin())
  {
    setBno055Discovered(true);
  }

  /* Disable command echo from Bluefruit */
  ble.echo(false);

  //Serial.println("Requesting Bluefruit info:");
  /* Print Bluefruit information */
  ble.info();

  //Serial.println(F("Waiting for connection in UART mode..."));
  //Serial.println();

  ble.verbose(false);  // debug info is a little annoying after this point!

  /* Wait for connection */
  while (! ble.isConnected()) {
      delay(500);
  }

  // LED Activity command is only supported from 0.6.6
  if ( ble.isVersionAtLeast(MINIMUM_FIRMWARE_VERSION) )
  {
    // Change Mode LED Activity
    //Serial.println(F("******************************"));
    //Serial.println(F("Connected"));
    //Serial.println(F("******************************"));
    //Serial.println(F("Change LED activity to " MODE_LED_BEHAVIOUR));
    ble.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
    //Serial.println(F("******************************"));
  }

}


/**************************************************************************/
/*!
    @brief  Constantly poll for new command (or response) data and react to it
*/
/**************************************************************************/
void loop() {

  //If there is no Bluetooth connection
  if(!ble.isConnected()){
    //Stop all vibrations
    switchModeFromMessage("<S>");
  }

  //From FeediModeManager. Feedi acts depending on the mode it is in
  doModeTasks();

  // Check for incoming characters from Bluefruit
  ble.println("AT+BLEUARTRX");
  ble.readline();
  if (strcmp(ble.buffer, "OK") == 0) {
    // no data
    return;
  }
  // Some data was found, its in the buffer
  String receivedData = ble.buffer;
  //Serial.print(F("[Recv] ")); Serial.println(receivedData); //for debugging purposes
 
  if(receivedData.equals("<d>")){ //dummy message sent every 5 seconds from the smartphone to check if the connection is still alive
    bluetoothSend("k"); //only length 1 because it might interfere with bnodata of length up to 19
  }
  
  else if(receivedData.startsWith("<n") || receivedData.startsWith("<v") || receivedData.indexOf("<S>")!=-1){
    switchModeFromMessage(receivedData);
  }
  else{
    bluetoothSend("Command unknown!");
  }
  
  ble.waitForOK();

}
