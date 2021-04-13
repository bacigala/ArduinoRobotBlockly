
// MOTION FUNCTIONS (FOOTER)

uint16_t fwd_time[] = {1,	1,100,1,1,1,100,1,1,1,100,1,100,1,1,100,1,100,1,1,1,0};
uint8_t fwd_servo[] = {10,11, 1,  2,  1,  6,  4,  3,  6,  5,  1,  2,  2,  6,  5,  3,  4,  1,  2,  1,  9, 0};
uint8_t fwd_val[] = 	{6,	1,	36, 66, 90, 180,120,120,90, 0,  109,156,90, 0,  90, 60, 60, 36, 70, 90, 6, 0};
uint8_t fwd_len = 22;
uint8_t bwd_val[] = {6,2,36,66,90,180,60,60,90,0,109,156,90,0,90,120,120,36,70,90,6,0};
uint8_t lt_val[] = {6,5,36,66,90,180,120,80,90,0,111,156,90,0,90,110,80,36,70,90,6,0};
uint8_t rt_val[] = {6,4,36,66,90,180,80,120,90,0,109,156,90,0,90,80,110,36,70,90,6,0};

void chod_smerom(int s)
{
  if (s == 1) zatancuj_choreografiu(fwd_time, fwd_servo, fwd_val, fwd_len);
  else if (s == 2) zatancuj_choreografiu(fwd_time, fwd_servo, bwd_val, fwd_len);
  else if (s == 3) zatancuj_choreografiu(fwd_time, fwd_servo, rt_val, fwd_len);
  else if (s == 4) zatancuj_choreografiu(fwd_time, fwd_servo, lt_val, fwd_len);  
}

uint32_t koniec_tanca;

void zatancuj_choreografiu(uint16_t *ch_time, uint8_t *ch_servo, uint8_t *ch_val, int ch_len )
{
  koniec_tanca = millis() + 3600000;
  for (int i = 0; i < ch_len - 1; i++)
  {
    delay(ch_time[i]);
    if (millis() > koniec_tanca) {
      break; 
    }
    if (ch_servo[i] < 7) nastav_koncatinu(ch_servo[i], ch_val[i]);
    else i = specialny_prikaz(i, ch_servo[i], ch_val[i], ch_len);
#ifdef MODULE_SERIAL
    if (serial_available() || Serial.available()) {
      serial_println_flash(PSTR("FAIL: Serial was available."));
      //break;
    }
#endif
    check_battery();
  }
  beep();
}

int specialny_prikaz(uint16_t i, uint8_t prikaz, uint8_t argument, int len)
{
  if (prikaz == 8) koniec_tanca = millis() + 1000 * (uint32_t)argument; 
  else if (prikaz == 9) 
  {
#ifdef MODULE_SERIAL
    if (serial_available() || Serial.available()) return len - 1;
#endif
#ifdef MODULE_ULTRASONIC
    if (measure_distance() < 10) return len - 1;
#endif
    return ((int)argument) - 1;
  }
  else if (prikaz == 10) spomalenie = argument;
#ifdef MODULE_SOUND
  //else if (prikaz == 11) melody_play(argument);
  else if (prikaz == 12) sound_effect8();
  else if (prikaz == 13) melody_stop();
#endif
	else if (prikaz == 14) reset_motors();
  return i;
}

void ruky()
{
  uint8_t odloz_spomalenie = spomalenie;
  spomalenie = 0;
#ifdef ROBOT_3D_PRINTED
  nastav_koncatinu(5, 180);
  nastav_koncatinu(6, 180);
#else
  nastav_koncatinu(5, 0);
  nastav_koncatinu(6, 0);
#endif
  delay(1000);
  nastav_koncatinu(5, 90);
  nastav_koncatinu(6, 90);
  spomalenie = odloz_spomalenie;
  delay(1000);
#ifdef MODULE_SOUND
  beep();
#endif
}

void nastav_koncatinu(int8_t srv, uint8_t poloha)
{
  int8_t delta;
  srv--;

  if (servo_invertovane[srv]) poloha = 180 - poloha;
  
  if ((int16_t)poloha + (int16_t)kalib[srv] - 90 < 0) poloha = 0;
  else poloha += kalib[srv] - 90;
  
  if (poloha > 180) poloha = 180;

  if (spomalenie == 0) {
    stav[srv] = poloha;
    s[srv].write(stav[srv]);
  }
  else {  
    if (stav[srv] < poloha) delta = 1;
    else delta = -1;
    while (stav[srv] != poloha)
    {
      stav[srv] += delta;
      s[srv].write(stav[srv]);
      delay(spomalenie);
    }
  }
}

void reset_motors()
{
  for (int i = 0; i < 6; i++)
  {
    stav[i] = kalib[i];
    s[i].write(kalib[i]);
    delay(300);
  }
  beep();
}

void to_tiptoes() {
	nastav_koncatinu(1, 50);
	nastav_koncatinu(2, 130);
}

void to_heels() {
	nastav_koncatinu(1, 130);
	nastav_koncatinu(2, 50);
}

void wave_hand() {
	nastav_koncatinu(5, 0);
	nastav_koncatinu(5, 35);
	nastav_koncatinu(5, 0);
	nastav_koncatinu(5, 35);
}

void precitaj_kalibraciu_z_EEPROM()
{
  uint8_t value = EEPROM.read(1);
  if ((value != '~') && (value != '1') && (value != '2') && (value != '3')) return;
  //if (value != '~') auto_start = value - '0';
  //else auto_start = 0;
  for (int i = 2; i < 8; i++)
  {
    int16_t k = EEPROM.read(i);
    if (k > 127) servo_invertovane[i - 2] = 1;
    else servo_invertovane[i - 2] = 0;
    k &= 127;
    prednastavena_kalibracia[i - 2] = k + (90-63);
  }
  for (int i = 0; i < 6; i++)
    dolny_limit[i] = EEPROM.read(i + 9);
  for (int i = 0; i < 6; i++)
    horny_limit[i] = EEPROM.read(i + 15);
}
