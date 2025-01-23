//#include <Servo.h>
#include <ESP32Servo.h>
static const int verticalServoPin = 12;
static const int horizontalServoPin = 13;

// variables to get position since servo motors have no direct feedback
int vposition = 0;
int hposition = 0;

Servo hServo;
Servo vServo;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  hServo.attach(horizontalServoPin);
  vServo.attach(verticalServoPin);
  // default position when start
  vServo.write(45);
  hServo.write(45);
  vposition = 45;
  hposition = 45;
}

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

void loop() {
  while(Serial.available()){
    String data = Serial.readStringUntil('\n');
    Serial.print("Recieved Data: ");
    Serial.println(data);


    int vpos = get_number_from_serial(data, 0);
    int hpos = get_number_from_serial(data, 1);
    set_position(vpos, hpos);
  }
  delay(1000);
}
