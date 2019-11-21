package edu.kit.itiv.feedi_app;

//This class contains the UUIDs used in the application

import java.util.UUID;

public class MyUuids {

    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID   = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID   = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    // UUID copied from the adafruit BLE application
    // verified on internet sites such as
    // https://www.allaboutcircuits.com/projects/how-to-communicate-with-a-custom-ble-using-an-android-app/
    //See https://stackoverflow.com/questions/18699251/finding-out-android-bluetooth-le-gatt-profiles
    //Quote "For example, all BLE characteristic UUIDs are of the form
    //0000XXXX-0000-1000-8000-00805f9b34fb
    // The assigned number for the Heart Rate Measurement characteristic UUID is listed as 0x2A37, which is how the developer of the sample code could arrive at:
    //00002a37-0000-1000-8000-00805f9b34fb" -> assigned number in this case: 0x2902
    //from nordic (who built the BLE module used on the adafruit feather) webpage:
    // https://devzone.nordicsemi.com/f/nordic-q-a/24974/client-characteristic-configuration-descriptor-uuid
    public static UUID CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public MyUuids(){

    }

}

