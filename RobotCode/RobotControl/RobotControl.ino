// Authors: Ed Baker, Alex Henderson, Jake Maynard

#include <NXShield.h>
#include <Wire.h>
#include <NXTLight.h>
#include <RFIDuino.h>
#include "StopWatch.h"
#include "tag.h"
#include "pilot.h"

NXShield nxshield;
NXTLight light1;
NXTLight light2;
RFIDuino rfid;

PILOT pilot(nxshield, light1, light2);

const char ID =  'A';  // Unique id for each robot

struct coords {
  int x = 5;
  int y = 5;
};

int robNum = 2;

// used for xbee receive
String inputString = "";

coords currentPos;

int heading = 0;
byte tagData[5];                  //Holds the ID numbers from the tag
byte tagDataBuffer[5];
Tag tagRef[5][5];

char outBuff[14] = "rob0_pos:x,y";

void mapInit();
int parseInput();
void get_pos();
void move(int targ_heading);
void goForward();
boolean verifyPosition();
void serialEvent();

int signum(int val);

void setup() {
  mapInit();
  Serial.begin(9600);
  delay(500);

  inputString.reserve(200);

  nxshield.init(SH_HardwareI2C);
  nxshield.waitForButtonPress(BTN_GO);
  light1.init(&nxshield, SH_BAS2);
  light2.init(&nxshield, SH_BBS2);

  light1.setReflected();
  light2.setReflected();

  delay(500);
  pilot.resetMotors();
  delay(500);

  outBuff[3] = ID;
  //ID initial location
  bool initRead = true;

  while (initRead) {
    initRead = !(rfid.decodeTag(tagData));
  }

  rfid.transferToBuffer(tagData, tagDataBuffer);
  get_pos();

  nxshield.ledSetRGB(5, 0, 0);
}


void loop() {
  outBuff[9] = currentPos.x + '0';
  outBuff[11] = currentPos.y + '0';
  bool goodString = false;
  int targ_heading = -1;
  do {
    inputString = "";

    //Broadcast location
    Serial.println(outBuff);
    serialEvent();

    int count = 1;
    while (inputString.charAt(0)!='s' || inputString.charAt(3) != ID) {
      inputString = "";
      serialEvent();
      if (count % 10000 == 0) {
        Serial.println(outBuff);
      }
      count++;
    }// end loop

    if (inputString.length() >= 8)
      targ_heading = parseInput();

  } while (targ_heading < 0);

  //Move
  move(targ_heading);
  rfid.transferToBuffer(tagData, tagDataBuffer);

  // update heading
  heading = targ_heading;

  //ID new location
  get_pos();

}

// initialize the map with tag IDs
void mapInit()
{
  tagRef[0][0].setTagData(112, 0, 39, 35, 62);
  tagRef[1][0].setTagData(112, 0, 39, 29, 194);
  tagRef[2][0].setTagData(112, 0, 39, 53, 177);
  tagRef[3][0].setTagData(112, 0, 39, 50, 27);
  tagRef[4][0].setTagData(112, 0, 39, 27, 46);
  tagRef[0][1].setTagData(112, 0, 39, 35, 208);
  tagRef[1][1].setTagData(112, 0, 38, 249, 99);
  tagRef[2][1].setTagData(112, 0, 39, 45, 110);
  tagRef[3][1].setTagData(112, 0, 39, 11, 27);
  tagRef[4][1].setTagData(108, 0, 66, 242, 128);
  tagRef[0][2].setTagData(112, 0, 39, 63, 5);
  tagRef[1][2].setTagData(112, 0, 38, 228, 83);
  tagRef[2][2].setTagData(112, 0, 39, 13, 234);
  tagRef[3][2].setTagData(112, 0, 38, 225, 35);
  tagRef[4][2].setTagData(112, 0, 39, 16, 72);
  tagRef[0][3].setTagData(112, 0, 38, 244, 82);
  tagRef[1][3].setTagData(112, 0, 39, 56, 139);
  tagRef[2][3].setTagData(112, 0, 38, 252, 57);
  tagRef[3][3].setTagData(112, 0, 38, 242, 65);
  tagRef[4][3].setTagData(112, 0, 38, 239, 75);
  tagRef[0][4].setTagData(112, 0, 39, 26, 79);
  tagRef[1][4].setTagData(112, 0, 39, 10, 181);
  tagRef[2][4].setTagData(112, 0, 39, 26, 6);
  tagRef[3][4].setTagData(112, 0, 38, 238, 119);
  tagRef[4][4].setTagData(112, 0, 39, 20, 118);
}


int parseInput() {
  int index = inputString.indexOf('_') + 1;
  if (index == 0)
    return -1;
  int new_x = inputString.charAt(index) - '0';
  int new_y = inputString.charAt(index+2) - '0';

  if(new_x==currentPos.x && new_y == currentPos.y)
    return -1;
  // Determine heading to new postion
  int targ_heading = -1;
  int x_dif = new_x - currentPos.x;
  int y_dif = new_y - currentPos.y;

  if (abs(y_dif) > abs(x_dif)) {
    if (signum(y_dif) < 0) {
      targ_heading = 0;
    } else if (signum(y_dif) > 0) {
      targ_heading = 2;
    }
  } else if (abs(x_dif) > abs(y_dif) || abs(x_dif) == abs(y_dif)) {
    if (signum(x_dif) < 0) {
      targ_heading = 3;
    } else if (signum(x_dif) > 0) {
      targ_heading = 1;
    }
  }
  
  // Return heading
  return targ_heading;
}

/*
 Identifies the grid position
 corresponding to current tag data
 */
void get_pos() {
  bool tagComp = false;
  for (int x = 0; x < 5; x++)
  {
    for (int y = 0; y < 5; y++)
    {
      tagComp = rfid.compareTagData(tagData, tagRef[x][y].tagData);
      if (tagComp)
      {
        currentPos.x = x;
        currentPos.y = y;
        rfid.successSound();
        return;
      }
    }
  }// end loop

}

/*
  Takes in a target heading, turns the robot the
  appropriate amount, and moves straight alongh the
  grid till a new RFID is read. Sets tagData as a
  side effect.
*/
void move(int targ_heading) {
  int turn;
  // Calculate turn increment
  if (targ_heading == 0 && heading == 3)
    turn = 1;
  else if (targ_heading == 3 && heading == 0)
    turn = -1;
  else
    turn = targ_heading - heading; //negative is left turn, positive is right

  // Turn appropriate direction
  for (int i = 0; i < abs(turn); i++) {
    if (turn < 0)
      pilot.turnLeft();
    else if (turn > 0)
      pilot.turnRight();
  }
  
  goForward();
}

void goForward(){
  pilot.straight();

  verifyPosition();
}

boolean verifyPosition(){
  StopWatch watchdog_timer;
  int readCount = 0;
  boolean done = false;
  boolean tagComp = false;
  boolean tagCheck = false;
  boolean verifyRead = false;
  watchdog_timer.start();
  while(!done)
  {
    if(watchdog_timer.elapsed()>5000){
      watchdog_timer.stop();
      goForward();
      return false;
    }
    tagCheck = rfid.decodeTag(tagData);   
      if (tagCheck==true){ 
        readCount++;
        if(readCount==1){
          rfid.transferToBuffer(tagData, tagDataBuffer);
        }
        else if(readCount==2){
          verifyRead = rfid.compareTagData(tagData, tagDataBuffer); //run the compareTagData function to compare the data in the buffer (the last read) with the data from the current read
          if (verifyRead == true){
             // do something with the data here
             for(int x = 0; x < 5; x++)
              {
                
                for(int y = 0; y < 5; y++)
                {
                  tagComp = rfid.compareTagData(tagDataBuffer, tagRef[x][y].tagData);
                  delay(100);
                  if(tagComp)
                  {
                    rfid.successSound();
                    currentPos.x = x;
                    currentPos.y = y;

                    done = true;
                    watchdog_timer.stop();
                  }
                }
              }
         
          } // end if verifyRead
        } 
      }       
    } // end while not done 
}

/*
  SerialEvent occurs whenever a new data comes in the
 hardware serial RX.  This routine is run between each
 time loop() runs, so using delay inside loop can delay
 response.  Multiple bytes of data may be available.
 */
void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    inputString += inChar;
    delay(10);
    if (inChar == '\n' || inChar == '\r') {
      return;
    }
  }
}

int signum(int val) {
  return ((0 < val) - (0 > val));
}

