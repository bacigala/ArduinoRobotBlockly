
// MOTION HEADER

#define MODULE_MOTION 1
#define ROBOT_3D_PRINTED 1
#include <Servo.h>
#include <EEPROM.h>

// cisla pinov, kde su zapojene servo motory
#define PIN_SERVO_LAVA_RUKA   10
#define PIN_SERVO_PRAVA_RUKA  11
#define PIN_SERVO_LAVA_NOHA   9
#define PIN_SERVO_PRAVA_NOHA  6
#define PIN_SERVO_LAVA_PATA   5
#define PIN_SERVO_PRAVA_PATA  3

// tu su serva cislovane 1-6
#define SERVO_LAVA_RUKA   5
#define SERVO_PRAVA_RUKA  6
#define SERVO_LAVA_NOHA   4
#define SERVO_PRAVA_NOHA  3
#define SERVO_LAVA_PATA   2
#define SERVO_PRAVA_PATA  1

// ak su niektore serva naopak, je tu jednotka
uint8_t servo_invertovane[6] = {0, 0, 0, 0, 0, 0};

// sem si mozno ulozit svoju kalibraciu
uint8_t prednastavena_kalibracia[] = { 90, 90, 90, 90, 90, 90 };

uint8_t dolny_limit[] = {0, 0, 0, 0, 0, 0, 0, 0};
uint8_t horny_limit[] = {180, 180, 180, 180, 180, 180};

Servo s[6];
uint8_t kalib[6];
int stav[6];
int krok;
uint8_t spomalenie;
