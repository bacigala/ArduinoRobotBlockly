// MODULE MOTION - SETUP

s[0].attach(PIN_SERVO_FOOT_RIGHT);
s[1].attach(PIN_SERVO_FOOT_LEFT);
s[2].attach(PIN_SERVO_LEG_RIGHT);
s[3].attach(PIN_SERVO_LEG_LEFT);
s[4].attach(PIN_SERVO_ARM_LEFT);
s[5].attach(PIN_SERVO_ARM_RIGHT);

load_calibration_EEPROM();

// move motors to default position
for (int i = 0; i < 6; i++) {
  current_calibration[i] = default_calibration[i];
  current_position[i] = current_calibration[i];
	s[i].write(current_position[i]);
}

slowdown = 6;