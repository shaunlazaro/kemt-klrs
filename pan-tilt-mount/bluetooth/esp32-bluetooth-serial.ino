#include "BluetoothSerial.h"
#include <ESP32Servo.h>

// Servo Motor Variables
static const int verticalServoPin = 12;
static const int horizontalServoPin = 13;
Servo hServo;
Servo vServo;
int vposcurrent = 0;
int hposcurrent = 0;
int vpostarget = 45;
int hpostarget = 90;

unsigned long previousMillis = 0;
const int moveInterval = 20;

char targetchar = ','; // Character to count

// Bluetooth variables
BluetoothSerial SerialBT;
const String bluetooth_name = "RePose-BT";
bool isConnected = false;

int get_number_from_serial(String data, int num_index) {
  // format should be "number1,number2"
  int index = data.indexOf(",");
  String pos;
  if (num_index == 1) {
    pos = data.substring(0,index);
  }
  else {
    pos = data.substring(index+1);
  }
  return pos.toInt();
}

void setup() {
  Serial.begin(115200); // Debugging on Serial Monitor
  // bluetooth setup
  SerialBT.begin(bluetooth_name); // Bluetooth name
  Serial.println("Bluetooth Serial Started. Device name: " + bluetooth_name);

  // Servo motor setup
  hServo.attach(horizontalServoPin);
  vServo.attach(verticalServoPin);
  vServo.write(45); // default position when start
  hServo.write(90);
  vposcurrent = 45;
  hposcurrent = 90;
}

void loop() {
  if (SerialBT.hasClient() && !isConnected) {
    Serial.println("Bluetooth Device Connected!");
    isConnected = true;
  }
  if (!SerialBT.hasClient() && isConnected) {
    Serial.println("Bluetooth Device Disconnected!");
    isConnected = false;
  }

  if (SerialBT.available()) {
    String data = SerialBT.readString(); // Read data from Bluetooth
    int count = std::count(data.begin(), data.end(), targetchar);
    if (count == 1) {
      vpostarget = get_number_from_serial(data, 0);
      hpostarget = get_number_from_serial(data, 1);  
    }
    Serial.println("Received: " + data); // print to serial for checking if recieved 
    SerialBT.println("Echo: " + data); // Echo the data back
  }

  //Move both servos gradually (non-blocking)
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= moveInterval) {
    previousMillis = currentMillis;

    if (vposcurrent < vpostarget) {
      vposcurrent++;
    } else if (vposcurrent > vpostarget) {
      vposcurrent--;
    }
    if (hposcurrent < hpostarget) {
      hposcurrent++;
    } else if (hposcurrent > hpostarget) {
      hposcurrent--;
    }
    vServo.write(vposcurrent); // default position when start
    hServo.write(hposcurrent);
    }
}