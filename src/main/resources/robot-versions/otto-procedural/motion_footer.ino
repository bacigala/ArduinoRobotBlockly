// MODULE MOTION - FOOTER

/*
 * COMPLEX MOTION (short choreography)
 */

uint16_t fwd_time[] = {100,1,1,100,1,1,1,100,1,1,1,100,1,100,1};
uint8_t fwd_servo[] = {1,2,6,4,3,6,5,1,2,6,5,3,4,1,2};
uint8_t fwd_val[] = 	{48,69,180,104,104,90,0,111,146,0,90,62,69,46,69};
uint8_t fwd_len = 15;

uint8_t bwd_val[] = {6,2,36,66,90,180,60,60,90,0,109,156,90,0,90,120,120,36,70,90,6,0};
uint8_t lt_val[] = {6,5,36,66,90,180,120,80,90,0,111,156,90,0,90,110,80,36,70,90,6,0};
uint8_t rt_val[] = {6,4,36,66,90,180,80,120,90,0,109,156,90,0,90,80,110,36,70,90,6,0};

void turn_left() {
  complex_move(fwd_time, fwd_servo, lt_val, fwd_len);
}

void turn_right() {
  complex_move(fwd_time, fwd_servo, rt_val, fwd_len);
}

void go_forward() {
  complex_move(fwd_time, fwd_servo, fwd_val, fwd_len);
}

void go_backward() {
  complex_move(fwd_time, fwd_servo, bwd_val, fwd_len);
}

void complex_move(uint16_t *ch_time, uint8_t *ch_servo, uint8_t *ch_val, int ch_len ) {
  for (int i = 0; i < ch_len - 1; i++) {
    delay(ch_time[i]);
    if (ch_servo[i] < 7) motor_set(ch_servo[i], ch_val[i]);
    else i = special_command(i, ch_servo[i], ch_val[i], ch_len);
    check_battery();
  }
}

int special_command(uint16_t i, uint8_t command, uint8_t argument, int len) {
  if (command == 10) slowdown = argument;
	else if (command == 14) reset_motors();
  return i;
}


/*
 * SIMPLE MOTION GESTURES
 */

void reset_motors() {
  for (int i = 0; i < 6; i++) {
    current_position[i] = current_calibration[i];
    s[i].write(current_calibration[i]);
    delay(300);
  }
}

void stand_on_tiptoes() {
  motor_set(1, 50);
  motor_set(2, 130);
}

void stand_on_heels() {
  motor_set(1, 130);
  motor_set(2, 50);
}

void wave_hand() {
  motor_set(5, 0);
  motor_set(5, 35);
  motor_set(5, 0);
  motor_set(5, 35);
}

void wave_hand2() {
  uint8_t save_slowdown = slowdown;
  slowdown = 0;
  motor_set(5, 180);
  motor_set(6, 180);
  delay(1000);
  motor_set(5, 90);
  motor_set(6, 90);
  slowdown = save_slowdown;
  delay(1000);
}


/*
 * SIMPLE MOVE - move single motor
 */
void motor_set(int8_t srv, uint8_t position)
{
  int8_t delta;
  srv--;

  if (servo_inverted[srv]) position = 180 - position;
  
  if ((int16_t)position + (int16_t)current_calibration[srv] - 90 < limit_lower[srv])
    position = limit_lower[srv];
  else
    position += current_calibration[srv] - 90;
  
  if (position > limit_upper[srv]) position = limit_upper[srv];

  if (slowdown == 0) {
    current_position[srv] = position;
    s[srv].write(current_position[srv]);
  } else {
    if (current_position[srv] < position) delta = 1;
    else delta = -1;
    while (current_position[srv] != position) {
      current_position[srv] += delta;
      s[srv].write(current_position[srv]);
      delay(slowdown);
      delay(slowdown);
    }
  }
}


/*
 * LOAD CALIBRATION (can be stored by otto-basic RobotVersion)
 */
void load_calibration_EEPROM()
{
  uint8_t value = EEPROM.read(1);
  if ((value != '~') && (value != '1') && (value != '2') && (value != '3')) return;
  for (int i = 2; i < 8; i++) {
    int16_t k = EEPROM.read(i);
    if (k > 127) servo_inverted[i - 2] = 1;
    else servo_inverted[i - 2] = 0;
    k &= 127;
    default_calibration[i - 2] = k + (90-63);
  }
  for (int i = 0; i < 6; i++)
    limit_lower[i] = EEPROM.read(i + 9);
  for (int i = 0; i < 6; i++)
    limit_upper[i] = EEPROM.read(i + 15);
}


/*
 * DISABLE MOTORS (called on battery low level)
 */
void motion_disable() {
  for (int i = 0; i < 6; i++)
    s[i].detach();
}