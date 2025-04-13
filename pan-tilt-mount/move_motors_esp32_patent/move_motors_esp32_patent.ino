#include "BluetoothSerial.h"
#include <ESP32Servo.h>
#include <math.h>

// Servo Motor Variables
static const int verticalServoPin = 12;
static const int horizontalServoPin = 13;
Servo hServo;
Servo vServo;

double move_x = 0;
double move_y = 0;

static const int default_vpos = 90;
static const int default_hpos = 90;

int vposcurrent = default_vpos;
int hposcurrent = default_hpos;

static const int h_limit_high = 180;
static const int h_limit_low = 0;
static const int v_limit_high = 135;
static const int v_limit_low = 45;

double* normalized_values = new double[2];


// Movement Interval Variables
unsigned long last_recieved_message = 0;
unsigned long previousMillis = 0;
static const int move_interval = 75; // ms
static const int stop_interval = 500; // ms
static const int invert_constant = 1; // x
static const int invert_constant2 = -1; // y

// Bluetooth variables
BluetoothSerial SerialBT;
static const String bluetooth_name = "RePose-BT";
bool isConnected = false;

// Other variables
static const char target_char = ','; // Character to count

double get_number_from_serial(String data, int num_index, char split) {
  // format should be "double1,double2" -> returns double1 if index = 0, else double2
  int index = data.indexOf(split);
  String pos;
  if (num_index == 0) {
    pos = data.substring(0,index);
  }
  else {
    pos = data.substring(index+1);
  }
  return pos.toDouble();
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

double* normalize(int one, int two) {
  // Dynamically allocate memory for an array of 2 doubles
  one *= invert_constant;
  two *= invert_constant2;
  double* arr = new double[2];
  
  // Perform the normalization
  if (one != 0 || two != 0)
  {
    arr[0] = (double)one / sqrt(one * one + two * two);
    arr[1] = (double)two / sqrt(one * one + two * two);
    double maxValue = max(abs(arr[0]), abs(arr[1]));
    // Normalize the array by dividing each element by the maximum value
    arr[0] /= maxValue;
    arr[1] /= maxValue;
  }
  else {
    arr[0] =0;
    arr[1] =0;
  }

  //Serial.print("Array normalized is: ");
  //Serial.print(arr[0]);
  //Serial.print(" ");
  //Serial.println(arr[1]);
  return arr; 
}

void reset_pos() {
  
  // TODO: Refactor
  unsigned long currentMillis = millis();
  unsigned long previousMillisTEMP = millis();
  unsigned long vpostarget = default_vpos;
  unsigned long hpostarget = default_hpos;
  unsigned long moveIntervalTEMP = 50;
  while(vposcurrent != vpostarget || hposcurrent != hpostarget)
  {
    currentMillis = millis();
    if (currentMillis - previousMillisTEMP >= moveIntervalTEMP) {
      previousMillisTEMP = currentMillis;

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
    vposcurrent = default_vpos;
    hposcurrent = default_hpos;
}

void trimTrailing(String& str) {
    // Find the position of the last non-whitespace character
    int endIndex = str.length() - 1;

    // Remove trailing spaces and newline characters
    while (endIndex >= 0 && (isspace(str[endIndex]) || str[endIndex] == '\n' || str[endIndex] == '\r')) {
        endIndex--;
    }

    // Create a substring from the start to the endIndex (exclusive)
    str = str.substring(0, endIndex + 1);
}


void setup() {
  Serial.begin(115200); // Debugging on Serial Monitor
  // bluetooth setup
  SerialBT.begin(bluetooth_name); // Bluetooth name
  Serial.println("Bluetooth Serial Started. Device name: " + bluetooth_name);

  // Servo motor setup
  hServo.attach(horizontalServoPin);
  vServo.attach(verticalServoPin);

  hServo.write(90);
  vServo.write(90);
  reset_pos();
  
  normalized_values[0] = 0;
  normalized_values[1] = 0;
}

void onBluetoothLineComplete(String data)
{
  trimTrailing(data);
  if (data == "RESET")
  {
    Serial.println("EWRESFET: " + data); // print to serial for checking if recieved 
    reset_pos();
    normalized_values = normalize(0,0);
    move_x = 0;
    move_y = 0;
    last_recieved_message = millis();
  }
  else
  {
    int count = std::count(data.begin(), data.end(), target_char);
    Serial.println(count);
    if (count > 0) {
      String parsed_data = extractPair(data, count-1);
      Serial.println(parsed_data);
      int vpostarget = get_number_from_serial(parsed_data, 1, target_char);
      int hpostarget = get_number_from_serial(parsed_data, 0, target_char); 

      normalized_values = normalize(hpostarget,vpostarget);
      move_x = 0;
      move_y = 0;
      last_recieved_message = millis();
    }
  }
  Serial.println("Received: " + data); // print to serial for checking if recieved 
  //Serial.println(data=="RESET"); // print to serial for checking if recieved 
  //SerialBT.println("Parsed: " + data); // Echo thhe data back
}

int tempChar = -1;
String message = "";



void loop() {
  if (SerialBT.hasClient() && !isConnected) {
    Serial.println("Bluetooth Device Connected!");
    isConnected = true;

  }
  if (!SerialBT.hasClient() && isConnected) {
    Serial.println("Bluetooth Device Disconnected!");
    isConnected = false;

    // if client connected, then disconnect, set back to starting pos
    reset_pos();
    Serial.println("Resetting to default position");
    // flush all previous client dats
    SerialBT.flush();
    Serial.println("flushed all previous buffers and client data");
  }

  tempChar = SerialBT.read();
  if(tempChar != -1)
  {
    // Serial.println(tempChar);
    if (tempChar == 13)
    {
      // typewriter: ding!
    }
    else if (tempChar == 10)
    {
      Serial.println(message);
      onBluetoothLineComplete(message);
      message = "";
    }
    else
    {
      message += (char)tempChar;
    }

  }

  //Move both servos gradually (non-blocking)
  unsigned long currentMillis = millis();

  if (currentMillis - previousMillis >= move_interval && currentMillis - last_recieved_message <= stop_interval) {
    previousMillis = currentMillis;

    move_x += normalized_values[0]; // horizontal
    move_y += normalized_values[1]; // verticalServoPin
    //Serial.print("move x:");
    //Serial.println(move_x);
    //Serial.print("move y:");
    //Serial.println(move_y);

    //Serial.print("hposcurrent:");
    //Serial.println(hposcurrent);
    //Serial.print("vposcurrent:");
    //Serial.println(vposcurrent);

    if (move_x >= 1) {
      move_x -= 1;
      if (hposcurrent + 1 <= h_limit_high) {
        hposcurrent += 1;
        hServo.write(hposcurrent);
        // Serial.println("LLL");
      }
    }
    else if (move_x <= -1) {
      move_x += 1;
      if (hposcurrent - 1 >= h_limit_low) {
        hposcurrent -= 1;
        hServo.write(hposcurrent);
        // Serial.println("RRR");
      }
    }

    if (move_y >= 2) {
      move_y -= 2;
      if (vposcurrent + 1 <= v_limit_high) {
        vposcurrent += 1;
        vServo.write(vposcurrent);
        // Serial.println("UUU");
      }

    }
    else if (move_y <= -2) {
      move_y += 2;
      if (vposcurrent - 1 >= v_limit_low) {
        vposcurrent -= 1;
        vServo.write(vposcurrent);
        // Serial.println("DDDD");
      }
    }
  }
}