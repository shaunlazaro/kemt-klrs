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

String extractPair(String input, int pairIndex) {
  int startPos = 0;
  int pairCount = 0;
  
  // Loop to find the pair at the specified index
  while (pairCount < pairIndex) {
    startPos = input.indexOf('[', startPos);  // Find the start bracket
    if (startPos == -1) {
      return "";  // If no more pairs are found, return an empty string
    }
    
    int endPos = input.indexOf(']', startPos);  // Find the end bracket
    if (endPos == -1) {
      return "";  // If no closing bracket is found, return an empty string
    }
    
    // Move start position past the closing bracket for the next iteration
    startPos = endPos + 1;  
    pairCount++;  // Increment the pair count
  }

  // Now extract the pair at the given index
  int startPosBracket = input.indexOf('[', startPos) + 1;  // Move after the opening bracket
  int endPosBracket = input.indexOf(']', startPosBracket);  // Find the closing bracket

  if (startPosBracket != -1 && endPosBracket != -1) {
    return input.substring(startPosBracket, endPosBracket);  // Return the pair without brackets
  }
  return "";  // In case something went wrong
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

    // if client connected, then disconnect, set back to starting pos
    vpostarget = 45;
    hpostarget = 90;
    Serial.println("Reseting to default position");
    // flush all previous client dats
    SerialBT.flush();
    Serial.println("flushed all previous buffers and client data");
  }

  if (SerialBT.available()) {
    String data = SerialBT.readString(); // Read data from Bluetooth
    int count = std::count(data.begin(), data.end(), targetchar);
    Serial.println(count);
    if (count > 0) {
      String parsed_data = extractPair(data, count-1);
      Serial.println(parsed_data);
      vpostarget = get_number_from_serial(parsed_data, 0);
      hpostarget = get_number_from_serial(parsed_data, 1);  

    }

    // TEMP hard limits for first prototype
    if (hpostarget >= 150) {
      hpostarget = 150;
    }
    if (hpostarget <= 30) {
      hpostarget=30;
    }
    if (vpostarget >= 75) {
      vpostarget = 75;
    }
    if (vpostarget <= 20) {
      vpostarget=20;
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