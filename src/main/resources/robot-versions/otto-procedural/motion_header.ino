// MODULE MOTION - HEADER

#define MODULE_MOTION 1
#define ROBOT_3D_PRINTED 1
#include <Servo.h>
#include <EEPROM.h>

// servo PIN
#define PIN_SERVO_ARM_LEFT   10
#define PIN_SERVO_ARM_RIGHT  11
#define PIN_SERVO_LEG_LEFT   9
#define PIN_SERVO_LEG_RIGHT  6
#define PIN_SERVO_FOOT_LEFT  5
#define PIN_SERVO_FOOT_RIGHT 3

// servo numbering
#define SERVO_ARM_LEFT   5
#define SERVO_ARM_RIGHT  6
#define SERVO_LEG_LEFT   4
#define SERVO_LEG_RIGHT  3
#define SERVO_FOOT_LEFT  2
#define SERVO_FOOT_RIGHT 1

// 1 if servo inverted
uint8_t servo_inverted[6] = {0, 0, 0, 0, 0, 0};

// defaults
uint8_t default_calibration[] = { 90, 90, 90, 90, 90, 90 };
uint8_t limit_lower[] = {0, 0, 0, 0, 0, 0, 0, 0};
uint8_t limit_upper[] = {180, 180, 180, 180, 180, 180};

Servo s[6];
uint8_t current_calibration[6];
int current_position[6];
uint8_t slowdown;