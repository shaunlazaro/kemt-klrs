/*
    Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleWrite.cpp
    Ported to Arduino ESP32 by Evandro Copercini
*/
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <ESP32Servo.h>

// variables to get position since servo motors have no direct feedback
static const int verticalServoPin = 12;
static const int horizontalServoPin = 13;
//String value = "";
//String prev_value = "";
int vposition = 0;
int hposition = 0;

Servo hServo;
Servo vServo;

// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define SERVER_NAME "PhysiKneed-BT"


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

void set_position(int v,int h) {
  vServo.write(v);
  hServo.write(h);
  vposition = v;
  hposition = h;
}

class MyCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) {
    String value = pCharacteristic->getValue();
    int vpos = get_number_from_serial(value, 0);
    int hpos = get_number_from_serial(value, 1);
    set_position(vpos,hpos);  
    pCharacteristic->setValue("");
  }
};

void setup() {
  Serial.begin(115200);
  hServo.attach(horizontalServoPin);
  vServo.attach(verticalServoPin);
  // default position when start
  vServo.write(45);
  hServo.write(90);
  vposition = 45;
  hposition = 90;

  //Serial.println("1- Download and install an BLE scanner app in your phone");
  //Serial.println("2- Scan for BLE devices in the app");
  //Serial.println("3- Connect to MyESP32");
  //Serial.println("4- Go to CUSTOM CHARACTERISTIC in CUSTOM SERVICE and write something");
  //Serial.println("5- See the magic =)");

  BLEDevice::init(SERVER_NAME);
  BLEServer *pServer = BLEDevice::createServer();

  BLEService *pService = pServer->createService(SERVICE_UUID);

  BLECharacteristic *pCharacteristic =
    pService->createCharacteristic(CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_WRITE_NR);

  pCharacteristic->setCallbacks(new MyCallbacks());

  pCharacteristic->setValue("Hello World");
  pService->start();

  BLEAdvertising *pAdvertising = pServer->getAdvertising();
  pAdvertising->start();
}



void loop() {
  // put your main code here, to run repeatedly:
  delay(100);
}
