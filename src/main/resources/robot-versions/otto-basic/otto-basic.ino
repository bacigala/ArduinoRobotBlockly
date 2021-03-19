#include <Servo.h>
#include <EEPROM.h>
#include <avr/pgmspace.h>

// odkomentujte nasledujuci riadok, ak je vas robot vytlaceny na 3D tlaciarni
#define ROBOT_3D_PRINTED 1

// klavesy pre pravy a lavy touch senzor
#define KEY_TOUCH1 '*'
#define KEY_TOUCH2 '/'

// alebo namiesto toho prikazy
#define CMD_TOUCH1 1
#define CMD_TOUCH2 2

#define ECHO_BT_TO_USB 1

#define US_TRIG  7
#define US_ECHO  8

#define BT_RX   12
#define BT_TX   4

#define SIRENA 13

#define MP3_OUTPUT_PIN 15   // connect Rx pin of DFPlayer to A1

#define TOUCH1   16    // A2
#define TOUCH2   17    // A3

//maximalna dlzka choreografie
#define CHOREO_LEN 200

#define CAS_OTTO_SA_ZACNE_NUDIT 9000

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

// znaky, ktorymi sa ovladaju jednotlive stupne volnosti
char znaky_zmien[] = {'a', 'q', ';', 'p', 'z', 'x', ',', '.', 'd', 'c', 'k', 'm' };
// co robia jednotlive znaky (znamienko urcuje smer)
int8_t zmeny[] = {-SERVO_LAVA_RUKA, SERVO_LAVA_RUKA,
                  SERVO_PRAVA_RUKA, -SERVO_PRAVA_RUKA,
                  -SERVO_LAVA_NOHA, SERVO_LAVA_NOHA,
                  -SERVO_PRAVA_NOHA, SERVO_PRAVA_NOHA,
                  SERVO_LAVA_PATA, -SERVO_LAVA_PATA,
                  -SERVO_PRAVA_PATA, SERVO_PRAVA_PATA
                 };

// sem si mozno ulozit svoju kalibraciu
uint8_t prednastavena_kalibracia[] = { 90, 90, 90, 90, 90, 90 };

uint8_t dolny_limit[] = {0, 0, 0, 0, 0, 0, 0, 0};
uint8_t horny_limit[] = {180, 180, 180, 180, 180, 180};

Servo s[6];

uint16_t ch_time[CHOREO_LEN];
uint8_t ch_servo[CHOREO_LEN];
uint8_t ch_val[CHOREO_LEN];
int ch_len;
uint8_t kalib[6];
int stav[6];
int krok;
uint8_t spomalenie;
uint8_t auto_start;

volatile int16_t distance;
uint32_t cas_ked_naposledy_stlacil;
uint8_t vypni_nudu;

uint8_t volume;
uint8_t current_song = 1;
uint8_t quiet = 0;

static uint8_t ignore_batteries = 0;

void setup() {
  volume = 30;
  
  Serial.begin(9600);
  init_tone2();
  init_serial(9600);
  init_ultrasonic();
  mp3_set_volume(volume);

  randomSeed(analogRead(1));
  s[0].attach(PIN_SERVO_PRAVA_PATA);
  s[1].attach(PIN_SERVO_LAVA_PATA);
  s[2].attach(PIN_SERVO_PRAVA_NOHA);
  s[3].attach(PIN_SERVO_LAVA_NOHA);
  s[4].attach(PIN_SERVO_LAVA_RUKA);
  s[5].attach(PIN_SERVO_PRAVA_RUKA);
  precitaj_kalibraciu_z_EEPROM();
  for (int i = 0; i < 6; i++)
  {
    kalib[i] = prednastavena_kalibracia[i];
    stav[i] = kalib[i];
    s[i].write(stav[i]);
  }
  ch_len = 0;
  krok = 7;
  vypni_nudu = 1;
  spomalenie = 6;

  while (Serial.available()) Serial.read();
  Serial.println(F("Hi! Press U for USB-powered run."));
  delay(5000);
  if (Serial.available())
  {
    if (Serial.read() == 'U') 
    {
      ignore_batteries = 1;
      Serial.println(F("USB-powered"));
    }
//    usb_active = 1;
  }
  else Serial.println(F("Bat.powered"));

  ahoj();
  ruky();
  delay(100);
  serial_println_flash(PSTR("\r\n  Otto DTDT, verzia 2018/09/01"));
  if (auto_start > 0) 
  {
    delay(7000);
    while (serial_available()) serial_read();
    while (Serial.available()) Serial.read();
    precitaj_choreografiu_z_EEPROM(auto_start);
    zatancuj_choreografiu(ch_time, ch_servo, ch_val, ch_len);
  }
  cas_ked_naposledy_stlacil = millis();
}

char check_touch()
{
  // ak chcete namiesto prikazu dotykovymi senzormi
  // vratit klaves, pouzite toto:
  if (digitalRead(TOUCH1)) return KEY_TOUCH1;
  else if (digitalRead(TOUCH2)) return KEY_TOUCH2;
  // namiesto tohto:
//  if (digitalRead(TOUCH1)) menu_command(CMD_TOUCH1);
//  else if (digitalRead(TOUCH2)) menu_command(CMD_TOUCH2);
  // toto nechajte tak:
  else return -1;
}

void loop() {
  char z = check_touch();
  if (serial_available()) z = serial_read();
#ifdef ECHO_BT_TO_USB
  if (Serial.available()) z = Serial.read();
#endif

  if (z != -1)
  {
    cas_ked_naposledy_stlacil = millis();
    if (pohyb_znakom(z)) return;
    else if (pohyb_kombinacia(z)) return;
    else if (vydaj_zvukovy_efekt(z)) return;
    else if (ma_zahrat_melodiu(z)) return;
    else if (z == '@') nacitaj_choreografiu();
    else if (z == '?') vypis_choreografiu();
    else if (z == 't') zatancuj_choreografiu(ch_time, ch_servo, ch_val, ch_len);
    else if (z == '-') ahoj();
    else if (z == ' ') reset();
    else if (z == 'H') kalibruj();
    else if (z == 'J') nastav_limity();
    else if (z == 'G') vypis_kalibraciu();
    else if (z == 'L') nacitaj_kalibraciu();
    else if (z == 'E') zapis_kalibraciu_do_EEPROM();
    else if (z == 'R') ruky();
    else if (z == '9') zvys_krok();
    else if (z == '1') zniz_krok();
    else if (z == '8') zvys_spomalenie();
    else if (z == '7') zniz_spomalenie();
    else if (z == 'U') test_ultrazvuk();
    else if (z == 'S') zobraz_stav();
    else if (z == 'W') skus_zapisat_choreografiu_do_EEPROM();
    else if (z == 'X') skus_naformatovat_EEPROM_choreografie();
    else if (z == 'C') skus_nacitat_choreografiu_z_EEPROM();
    else if (z == 'A') nastav_automaticky_start();
    else if (z == 'V') prepni_nudu();
    else if (z == '$') vasa_dalsia_funkcia();
  }
  int16_t d = measure_distance();
  if (d < 10) menu_ultrasonic_request();

  skus_ci_sa_nenudi();
  check_battery();
}

void vasa_dalsia_funkcia()
{
  serial_println_flash(PSTR("lalala"));
}

void nastav_automaticky_start()
{
  serial_print_flash(PSTR("Automaticky start [1,2,3, 0=vypni]:"));
  char z;
  do { z = read_char(); } while ((z < '0') || (z > '3'));
  if (z == '0') EEPROM.write(1, '~');
  else EEPROM.write(1, z);
  serial_println_char(z);
  serial_println_flash(PSTR("ok"));
}

void prepni_nudu()
{
  vypni_nudu ^= 1;
  serial_print_flash(PSTR("ticho:"));
  serial_println_num(vypni_nudu);
}

void skus_ci_sa_nenudi()
{
  if ((millis() - cas_ked_naposledy_stlacil > CAS_OTTO_SA_ZACNE_NUDIT) && !vypni_nudu)
  {
    uint8_t rnd = random(21);
    switch (rnd)
    {
      case 0: zatancuj_choreografiu(ch_time, ch_servo, ch_val, ch_len); break;
      case 1: zahraj_melodiu(1); break;
      case 2: zahraj_melodiu(2); break;
      case 3: zahraj_melodiu(3); break;
      case 4: zahraj_melodiu(4); break;
      case 5: for (int i = 0; i < 20; i++)
        {
          rnd = random(16) + 1;
          zvukovy_efekt(rnd);
        }
        break;
      default: zvukovy_efekt(rnd - 5); break;
    }
    cas_ked_naposledy_stlacil = millis();
  }
}

const uint8_t klavesy_efektov[] PROGMEM = {'v', 'b', 'n', 'j', 'h', 'g', 'f', 's', 'l', 'o', 'i', 'u', 'y', 'r', 'e', 'w'};
#define POCET_KLAVES_EFEKTOV 16

uint8_t vydaj_zvukovy_efekt(char z)
{
  for (int i = 0; i < POCET_KLAVES_EFEKTOV; i++)
    if (z == pgm_read_byte(klavesy_efektov + i))
    {
      zvukovy_efekt(i + 1);
      return 1;
    }
  return 0;
}

uint8_t ma_zahrat_melodiu(char z)
{
  if (z == 9) zahraj_melodiu(1);
  else if (z == '*') zahraj_melodiu(2);
  else if (z == '/') zahraj_melodiu(3);
  else if (z == '+') zahraj_melodiu(4);
  else if (z == '!') zahraj_melodiu(5);
  else if (z == '%') zahraj_melodiu(6);
  else if ((z == 27) || (z == ':')) zastav_melodiu();
  else if (z == 'M') zastav_melodiu();
  else if (z == 'B')
  {
      if (quiet) mp3_set_volume(volume);
      else mp3_set_volume(0);   
      serial_print_flash(PSTR("music:"));
      serial_println_num(quiet);
      quiet = 1 - quiet;
  }
  else if (z == 'F')
  {
    mp3_play(current_song);
    delay(20);
    mp3_set_volume(volume);
  }
  else if ((z == 'O') || (z == 'I'))
  {
    if (z == 'O') if (current_song < 255) current_song++;
    if (z == 'I') if (current_song > 1) current_song--;
    serial_print_flash(PSTR("song "));
    serial_println_num(current_song);
  }
  else return 0;
  return 1;
}

void efekt1()
{
  uint16_t fre = 100;
  for (int i = 0; i < 200; i++)
  {
    tone2(fre, 2);
    fre += 10;
    delay(2);
  }
}

void efekt2()
{
  uint16_t fre = 2060;
  for (int i = 0; i < 200; i++)
  {
    tone2(fre, 2);
    fre -= 10;
    delay(2);
  }
}

void efekt3()
{
  uint16_t fre;
  for (int i = 0; i < 200; i++)
  {
    fre = random(3000) + 20;
    tone2(fre, 2);
    delay(2);
  }
}

void efekt4()
{
  uint16_t fre = 100;
  for (int i = 0; i < 200; i++)
  {
    (i & 1) ? tone2(fre, 2) : tone2(2100 - fre, 2);
    fre += 10;
    delay(2);
  }
}

void efekt5()
{
  uint16_t fre = 100;
  for (int i = 0; i < 100; i++)
  {
    (i & 1) ? tone2(fre, 4) : tone2(2100 - fre, 4);
    fre += 20;
    delay(4);
  }
}

void efekt6()
{
  uint16_t d = 12;
  for (int i = 0; i < 20; i++)
  {
    tone2(1760, 10);
    delay(d);
    d += 3;
  }
}

void efekt7()
{
  for (int i = 0; i < 40; i++)
  {
    if (i & 1) tone2(1760, 10);
    delay(10);
  }
}

void efekt8()
{
  uint16_t d = 72;
  for (int i = 0; i < 20; i++)
  {
    tone2(1760, 10);
    delay(d);
    d -= 3;
  }
}

void efekt9()
{
  float fre = 3500;
  while (fre > 30)
  {
    tone2((uint16_t)fre, 2);
    fre *= 0.97;
    delay(2);
  }
}

void efekt10()
{
  float fre = 30;
  while (fre < 3000)
  {
    tone2((uint16_t)fre, 2);
    fre *= 1.03;
    delay(2);
  }
}

void efekt11()
{
  uint16_t fre = 3500;
  uint16_t d = 42;
  for (int i = 0; i < 20; i++)
  {
    tone2(fre, d);
    fre -= 165;
    delay(d);
    d -= 2;
  }
}

void efekt12()
{
  float fre = 110;
  uint8_t d = 42;
  for (int i = 0; i < 20; i++)
  {
    tone2(fre, d);
    fre += 165;
    delay(d);
    d -= 2;
  }
}

void efekt13()
{
  uint16_t fre = 3400;
  uint8_t d = 200;
  uint8_t delta = 1;
  for (int i = 0; i < 20; i++)
  {
    tone2(fre, d);
    fre -= 165;
    delay(d);
    d -= delta;
    delta++;
  }
  tone2(110, 1000);
  delay(1000);
}

void efekt14()
{
  uint16_t fre = 880;
  int16_t d = 20;
  for (int i = 0; i < 20; i++)
  {
    tone2(fre, d);
    delay(d);
    fre += random(50) - 25;
    d += random(10) - 5;
    if (d < 0) d = 1;
  }
}

void efekt15()
{
  uint16_t fre = 440;
  int16_t d = 20;
  for (int i = 0; i < 20; i++)
  {
    tone2(fre, d);
    delay(d);
    fre += random(25) - 12;
    d += random(10) - 5;
    if (d < 0) d = 1;
  }
}

void efekt16()
{
  uint16_t fre = 1760;
  int16_t d = 20;
  for (int i = 0; i < 20; i++)
  {
    tone2(fre, d);
    delay(d);
    fre += random(100) - 50;
    d += random(10) - 5;
    if (d < 0) d = 1;
  }
}

void zvukovy_efekt(uint8_t cislo)
{
  switch (cislo) {
    case 1: efekt1(); break;
    case 2: efekt2(); break;
    case 3: efekt3(); break;
    case 4: efekt4(); break;
    case 5: efekt5(); break;
    case 6: efekt6(); break;
    case 7: efekt7(); break;
    case 8: efekt8(); break;
    case 9: efekt9(); break;
    case 10: efekt10(); break;
    case 11: efekt11(); break;
    case 12: efekt12(); break;
    case 13: efekt13(); break;
    case 14: efekt14(); break;
    case 15: efekt15(); break;
    case 16: efekt16(); break;
  }
}

void test_ultrazvuk()
{
  while ((serial_available() == 0) && (Serial.available() == 0))
  {
    serial_println_num(measure_distance());
    delay(100);
  }
  serial_read();
}

void menu_ultrasonic_request()
{
  uint32_t tm = millis();
  int d = measure_distance();
  int count = 0;
  int count2 = 0;
  while ((millis() - tm < 1500) && ((d < 15) || (count2 < 5)) && (count < 10))
  {
    delay(10);
    d = measure_distance();
    if (d == 10000) count++;
    else count = 0;
    if (d >= 15) count2++;
    else count2 = 0;
    if (serial_available() || Serial.available()) return;
  }
  if (millis() - tm >= 1500)
  {
    ultrasonic_menu();
    cas_ked_naposledy_stlacil = millis();
  }
}

void ultrasonic_menu()
{
  int selection = 0;
  tone2( 880, 200);

  do {
    int count = 0;
    int cnt10000 = 0;
    do {
      int32_t d = measure_distance();
      if (d == 10000) 
      {
        cnt10000++;
        delay(10);
        if (cnt10000 == 30) break;
        continue;
      }
      if (d >= 20) count++;
      else count = 0;
      delay(10);
    } while (!serial_available() && !Serial.available() && (count < 20));

    tone2( 440, 200);
    uint32_t tm = millis();
    while ((millis() - tm < 2000) && !serial_available() && !Serial.available()) 
    {
      int32_t d = measure_distance();
      if (d < 15) count++;
      else count = 0;
      if (count > 10) break;
      delay(20);
    }
    
    if (millis() - tm >= 2000)
    {
      tone2( 2000, 50);
      menu_command(selection);
      return;
    }
    selection++;
    for (int i = 0; i < selection; i++)
    {
      tone2( 1261, 50);
      delay(250);
    }
  } while (!serial_available() && !Serial.available());
  while (serial_available()) serial_read();
  while (Serial.available()) Serial.read();
}

void menu_command(int cmd)
{
  if (cmd == 1) vpred();
  if (cmd == 2) {
    precitaj_choreografiu_z_EEPROM(1);
    zatancuj_choreografiu(ch_time, ch_servo, ch_val, ch_len);
  }
  if (cmd == 3) {
    precitaj_choreografiu_z_EEPROM(2);
    zatancuj_choreografiu(ch_time, ch_servo, ch_val, ch_len);
  }
  if (cmd == 4) {
    precitaj_choreografiu_z_EEPROM(3);
    zatancuj_choreografiu(ch_time, ch_servo, ch_val, ch_len);
  }
  if (cmd == 5) zahraj_melodiu(1);
  if (cmd == 6) melodia_jedna_druhej();
  if (cmd == 7) ahoj();
  if (cmd == 8) zahraj_melodiu(2);
  if (cmd == 9) zahraj_melodiu(3);
  if (cmd == 10) zahraj_melodiu(4);
  if (cmd == 11) zahraj_melodiu(5);
}

void melodia_jedna_druhej()
{
  for (int i = 0; i < 2; i++)
  {
    tone2(262, 200);
    delay(200);
    tone2(330, 200);
    delay(200);

    tone2(262, 200);
    delay(200);
    tone2(330, 200);
    delay(200);

    tone2(392, 400);
    delay(400);

    tone2(392, 400);
    delay(400);
  }
}

// chodza pre vacsieho dreveneho robota
uint16_t chor_time[] = {500, 100, 1, 1, 100, 1, 1, 1, 100, 1, 1, 1, 100, 1, 100, 1, 1, 0};
uint8_t chor_servo[] = {11, 1, 2, 6, 4, 3, 6, 5, 1, 2, 6, 5, 3, 4, 1, 2, 9, 0};
uint8_t chor_val[] = {101, 48, 69, 180, 104, 104, 90, 0, 111, 146, 0, 90, 62, 69, 48, 69, 2, 0};
uint8_t chor_len = 18;

uint16_t fwd_time[] = {1,1,100,1,1,1,100,1,1,1,100,1,100,1,1,100,1,100,1,1,1,0};
uint8_t fwd_servo[] = {10,11,1,2,1,6,4,3,6,5,1,2,2,6,5,3,4,1,2,1,9,0};
uint8_t fwd_val[] = {6,1,36,66,90,180,120,120,90,0,109,156,90,0,90,60,60,36,70,90,6,0};
uint8_t fwd_len = 22;

uint8_t bwd_val[] = {6,2,36,66,90,180,60,60,90,0,109,156,90,0,90,120,120,36,70,90,6,0};
uint8_t lt_val[] = {6,5,36,66,90,180,120,80,90,0,111,156,90,0,90,110,80,36,70,90,6,0};
uint8_t rt_val[] = {6,4,36,66,90,180,80,120,90,0,109,156,90,0,90,80,110,36,70,90,6,0};


void vpred()
{
  while ((measure_distance() > 10) && !serial_available() && !Serial.available())
  {
    zatancuj_choreografiu(chor_time, chor_servo, chor_val, chor_len);
    check_battery();
  }
  pipni();
  reset();
}

void chod_smerom(int s)
{
  if (s == 1) zatancuj_choreografiu(fwd_time, fwd_servo, fwd_val, fwd_len);
  else if (s == 2) zatancuj_choreografiu(fwd_time, fwd_servo, bwd_val, fwd_len);
  else if (s == 3) zatancuj_choreografiu(fwd_time, fwd_servo, rt_val, fwd_len);
  else if (s == 4) zatancuj_choreografiu(fwd_time, fwd_servo, lt_val, fwd_len);  
}
void pipni()
{
  tone2( 1568, 50);
  delay(100);
  tone2( 1357, 50);
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
  pipni();
}

void ahoj()
{
  tone2( 1568, 50);
  delay(70);
  tone2( 1175, 30);
  delay(50);
  tone2( 880, 30);
  delay(50);
  tone2( 1047, 50);
  delay(70);
  tone2( 1245, 30);
  delay(150);
  tone2( 1568, 50);
  delay(100);
  if (random(10) > 4) tone2( 1357, 50);
  else tone2( 1047, 50);
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

void zobraz_stav()
{
  for (int i = 0; i < 6; i++)
  {
    serial_print_flash(PSTR("S")); serial_print_num(i + 1); serial_print_flash(PSTR(": ")); serial_println_num(stav[i] - kalib[i] + 90);
  }
  serial_println_flash(PSTR("---"));
  pipni();
}

void pohyb(int8_t servo)
{
  int8_t srv = (servo > 0) ? servo : -servo;
  srv--;
  if (servo_invertovane[srv]) servo = -servo;
  if (servo > 0)
  {
    if (stav[srv] <= horny_limit[srv] - krok) stav[srv] += krok;
    else stav[srv] = horny_limit[srv];
    s[srv].write(stav[srv]);
  }
  else if (servo < 0)
  {
    if (stav[srv] >= dolny_limit[srv] + krok) stav[srv] -= krok;
    else stav[srv] = dolny_limit[srv];
    s[srv].write(stav[srv]);
  }
}

uint8_t pohyb_znakom(char z)
{
  for (int i = 0; i < 12; i++)
  {
    if (z == znaky_zmien[i])
    {
      int8_t servo = zmeny[i];
      pohyb(servo);
      return 1;
    }
  }
  return 0;
}

void kombinacia1()
{
  chod_smerom(1);
//  pohyb(SERVO_LAVA_NOHA);
//  pohyb(-SERVO_PRAVA_PATA);
}

void kombinacia2()
{
  chod_smerom(2);

//  pohyb(SERVO_PRAVA_NOHA);
//  pohyb(-SERVO_LAVA_PATA);
}

void kombinacia3()
{
  chod_smerom(3);

//  pohyb(SERVO_LAVA_RUKA);
//  pohyb(SERVO_PRAVA_RUKA);
}

void kombinacia4()
{
    chod_smerom(4);

//  pohyb(-SERVO_LAVA_RUKA);
//  pohyb(-SERVO_PRAVA_RUKA);
}

int pohyb_kombinacia(char z)
{
  if (z == '3') kombinacia1();
  else if (z == '4') kombinacia2();
  else if (z == '5') kombinacia3();
  else if (z == '6') kombinacia4();
  else return 0;
  return 1;
}

char read_char()
{
  while (!serial_available() && !Serial.available());
  if (serial_available()) return serial_read();
  else return Serial.read();
}

int nacitajCislo()
{
  int num = 0;
  int z;
  do {
    z = read_char();
    if (z == '#') while (z != 13) z = read_char();
  } while ((z < '0') || (z > '9'));
  while ((z >= '0') && (z <= '9'))
  {
    num *= 10;
    num += (z - '0');
    do {
      z = read_char();
      if (z == -1) delayMicroseconds(10);
    } while (z < 0);
  }
  return num;
}

void nacitaj_choreografiu()
{
  ch_len = 0;
  int tm;
  do {
    tm = nacitajCislo();
    ch_time[ch_len] = tm;
    ch_servo[ch_len] = nacitajCislo();
    ch_val[ch_len] = nacitajCislo();
    ch_len++;
    if (ch_len == CHOREO_LEN) break;
  } while (tm > 0);
  pipni();
}

void vypis_choreografiu()
{
  for (int i = 0; i < ch_len; i++)
  {
    serial_print_num(ch_time[i]);
    serial_print(" ");
    serial_print_num(ch_servo[i]);
    serial_print(" ");
    serial_println_num(ch_val[i]);
  }
  serial_println_flash(PSTR("---"));
  pipni();
}

uint32_t koniec_tanca;

void zatancuj_choreografiu(uint16_t *ch_time, uint8_t *ch_servo, uint8_t *ch_val, int ch_len )
{
  koniec_tanca = millis() + 3600000;
  for (int i = 0; i < ch_len - 1; i++)
  {
    delay(ch_time[i]);
    if (millis() > koniec_tanca) {
      serial_println_flash(PSTR("FAIL: Koniec tanca."));
      break; 
    }
    if (ch_servo[i] < 7) nastav_koncatinu(ch_servo[i], ch_val[i]);
    else i = specialny_prikaz(i, ch_servo[i], ch_val[i], ch_len);
    if (serial_available() || Serial.available()) {
      serial_println_flash(PSTR("FAIL: Serial was available."));
      break;
    }
    check_battery();
  }
  pipni();
}

int specialny_prikaz(uint16_t i, uint8_t prikaz, uint8_t argument, int len)
{
  if (prikaz == 8) koniec_tanca = millis() + 1000 * (uint32_t)argument; 
  else if (prikaz == 9) 
  {
    if (serial_available() || Serial.available()) return len - 1;
    if (measure_distance() < 10) return len - 1;
    return ((int)argument) - 1;
  }
  else if (prikaz == 10) spomalenie = argument;
  else if (prikaz == 11) zahraj_melodiu(argument);
  else if (prikaz == 12) zvukovy_efekt(argument);
  else if (prikaz == 13) zastav_melodiu();
  
  return i;
}

void reset()
{
  for (int i = 0; i < 6; i++)
  {
    stav[i] = kalib[i];
    s[i].write(kalib[i]);
    delay(300);
  }
  pipni();
}

uint8_t nalad_hodnotu_serva(uint8_t servo, uint8_t hodnota, uint8_t min_hodnota, uint8_t max_hodnota)
{
  serial_print_flash(PSTR(" (+/-/ENTER): "));
  serial_println_num(hodnota);
  s[servo].write(hodnota);
  char z;
  do {
    z = read_char();
    if ((z == '+') && (hodnota < max_hodnota)) hodnota++;
    else if ((z == '-') && (hodnota > min_hodnota)) hodnota--;
    if ((z == '+') || (z == '-'))
    {
      serial_print_num(hodnota); serial_print_char('\r');
      s[servo].write(hodnota);
    }
  } while (z != 13);
  return hodnota;
}

void kalibruj()
{
  for (int i = 0; i < 6; i++)
  {
    serial_print_num(i);
    kalib[i] = nalad_hodnotu_serva(i, kalib[i], 90-63, 90+63);
    serial_print_num(i);
    serial_print(": ");
    serial_println_num(kalib[i]);
  }
  for (int i = 0; i < 6; i++) {
    serial_print_num(kalib[i]);
    serial_print(" ");
  }
  serial_print_flash(PSTR(" Chyt robota zozadu za telo (ENTER):"));
  char z;
  do { z = read_char(); } while (z != 13);
  serial_println_char(z);

  serial_print_flash(PSTR("*** Nasledujuce koncatiny su lave/prave z pohladu robota.\r\nAk to nesedi, treba prehodit kabliky servo!\r\n"));

  for (int i = 0; i < 6; i++) servo_invertovane[i] = 0;
 
  nastav_koncatinu(SERVO_PRAVA_PATA, 0);
  serial_print_flash(PSTR("Prava pata: je von alebo dnu? (V/D):"));
  do { z = read_char(); } while ((z != 'V') && (z != 'D'));
  serial_println_char(z);
  nastav_koncatinu(SERVO_PRAVA_PATA, 90);
  if (z == 'D') servo_invertovane[SERVO_PRAVA_PATA - 1] = 0;
  else servo_invertovane[SERVO_PRAVA_PATA - 1] = 1;
  
  nastav_koncatinu(SERVO_LAVA_PATA, 0);
  serial_print_flash(PSTR("Lava pata: je von alebo dnu? (V/D):"));
  do { z = read_char(); } while ((z != 'V') && (z != 'D'));
  serial_println_char(z);
  nastav_koncatinu(SERVO_LAVA_PATA, 90);
  if (z == 'V') servo_invertovane[SERVO_LAVA_PATA - 1] = 0;
  else servo_invertovane[SERVO_LAVA_PATA - 1] = 1;
  
  nastav_koncatinu(SERVO_PRAVA_NOHA, 0);
  serial_print_flash(PSTR("Prava noha: vpredu je pata alebo spicka? (P/S):"));
  do { z = read_char(); } while ((z != 'P') && (z != 'S'));
  serial_println_char(z);
  nastav_koncatinu(SERVO_PRAVA_NOHA, 90);
  if (z == 'P') servo_invertovane[SERVO_PRAVA_NOHA - 1] = 1;
  else servo_invertovane[SERVO_PRAVA_NOHA - 1] = 0;

  nastav_koncatinu(SERVO_LAVA_NOHA, 0);
  serial_print_flash(PSTR("Lava noha: vpredu je pata alebo spicka? (P/S):"));
  do { z = read_char(); } while ((z != 'P') && (z != 'S'));
  serial_println_char(z);
  nastav_koncatinu(SERVO_LAVA_NOHA, 90);
  if (z == 'S') servo_invertovane[SERVO_LAVA_NOHA - 1] = 1;
  else servo_invertovane[SERVO_LAVA_NOHA - 1] = 0;

#ifdef ROBOT_3D_PRINTED
  nastav_koncatinu(SERVO_PRAVA_RUKA, 180);
#else
  nastav_koncatinu(SERVO_PRAVA_RUKA, 0);
#endif
  serial_print_flash(PSTR("Prava ruka: je hore alebo dole? (H/D):"));
  do { z = read_char(); } while ((z != 'H') && (z != 'D'));
  serial_println_char(z);
  nastav_koncatinu(SERVO_PRAVA_RUKA, 90);
#ifdef ROBOT_3D_PRINTED
  if (z == 'H') servo_invertovane[SERVO_PRAVA_RUKA - 1] = 0;
  else servo_invertovane[SERVO_PRAVA_RUKA - 1] = 1;
#else
  if (z == 'H') servo_invertovane[SERVO_PRAVA_RUKA - 1] = 1;
  else servo_invertovane[SERVO_PRAVA_RUKA - 1] = 0;
#endif
  nastav_koncatinu(SERVO_LAVA_RUKA, 0);
  serial_print_flash(PSTR("Lava ruka: je hore alebo dole? (H/D):"));
  do { z = read_char(); } while ((z != 'H') && (z != 'D'));
  serial_println_char(z);
  nastav_koncatinu(SERVO_LAVA_RUKA, 90);
  if (z == 'D') servo_invertovane[SERVO_PRAVA_RUKA - 1] = 0;
  else servo_invertovane[SERVO_PRAVA_RUKA - 1] = 1;

  serial_println_flash(PSTR("ok"));
  pipni();
}
 
void nastav_limity()
{
  for (int i = 0; i < 6; i++)
  {
    serial_print_num(i);
    serial_print_flash(PSTR("dolny"));
    dolny_limit[i] = nalad_hodnotu_serva(i, dolny_limit[i], 0, 180);
    serial_print_num(i);
    serial_print_flash(PSTR(" dolny: "));
    serial_println_num(dolny_limit[i]);
    s[i].write(kalib[i]);

    serial_print_num(i);
    serial_print_flash(PSTR("horny"));
    horny_limit[i] = nalad_hodnotu_serva(i, horny_limit[i], 0, 180);
    serial_print_num(i);
    serial_print_flash(PSTR(" horny: "));
    serial_println_num(horny_limit[i]);
    s[i].write(kalib[i]);
  }
  for (int i = 0; i < 6; i++) {
    serial_print_num(dolny_limit[i]);
    serial_print("-");
    serial_print_num(horny_limit[i]);
    serial_print(" ");
  }
  serial_println_flash(PSTR("ok"));
  pipni();
}

void vypis_kalibraciu()
{
  serial_print_flash(PSTR("stredy: "));
  for (int i = 0; i < 6; i++) {
    serial_print_num(kalib[i]);
    serial_print(" ");
  }
  serial_println();
  
  serial_print_flash(PSTR("invertovane servo: "));
  for (int i = 0; i < 6; i++)
  {
    serial_print_num(servo_invertovane[i]);
    serial_print_char(' ');
  }
  serial_println();
    
  serial_print_flash(PSTR("dolny limit: "));
  for (int i = 0; i < 6; i++) {
    serial_print_num(dolny_limit[i]);
    serial_print(" ");
  }
  serial_println();
  serial_print_flash(PSTR("horny limit: "));
  for (int i = 0; i < 6; i++) {
    serial_print_num(horny_limit[i]);
    serial_print(" ");
  }
  serial_println();
}

void nacitaj_kalibraciu()
{
  for (int i = 0; i < 6; i++)
    kalib[i] = nacitajCislo();
  vypis_kalibraciu();
  serial_println_flash(PSTR("ok"));
  pipni();
}

void zvys_krok()
{
  if (krok < 180) krok++;
  serial_print_flash(PSTR("krok: "));
  serial_println_num(krok);
}

void zniz_krok()
{
  if (krok > 0) krok--;
  serial_print_flash(PSTR("krok: "));
  serial_println_num(krok);
}

void zniz_spomalenie()
{
  if (spomalenie > 0) spomalenie--;
  serial_print_flash(PSTR("spomalenie: "));
  serial_println_num(spomalenie);
}

void zvys_spomalenie()
{
  if (spomalenie < 100) spomalenie++;
  serial_print_flash(PSTR("spomalenie: "));
  serial_println_num(spomalenie);
}

//nasleduju funkcie ktore pracuju s pamatou EEPROM

//EEPROM MAP:
// 0: slot number where choreography 3 starts (B3. Real address = B3 x 4)
// 1: marker '~' indicates calibration is already stored in EEPROM and will be loaded on startup
//           '1', '2', '3' indicate the same, but choreography 1,2, or 3 is automatically launched on startup
// 2-8: servo calibration (central points to be used instead of default 90)
//        they are shifted by (90-63) and the range is (90-63)...0  till (90+63)...127
//        bit 7 (value 128) indicates servo is inverted
// 9-14: lower limit for direct control for all 6 servos
// 15-20: upper limit for direct control for all 6 servos
// 21:    number of steps in choreography 1 (L1) 0=choreography not in memory
// 22:    number of steps in choreography 2 (L2) 0=not in memory
// 23:    number of steps in choreography 3 (L3) 0=not in memory
// 24..(L1 x 4 + 23)       choreography1  tuplets (uint16,uint8,uint8) x L1
// (1024 - L2 x 4)..1023   choreography2  same as above
// B3 x 4..(B3 x 4 + L3 x 4 - 1)  choreography 3  same as above

void skus_zapisat_choreografiu_do_EEPROM()
{
  serial_print_flash(PSTR("Cislo? [1-3]: "));
  char odpoved = read_char();
  if ((odpoved < '1') || (odpoved > '3')) return;
  serial_println_char(odpoved);
  uint8_t cislo = odpoved - '0';

  serial_print_flash(PSTR("Zapisat choreografiu do EEPROM c."));
  serial_print_char(odpoved);
  serial_print_flash(PSTR("? [Y/n]: "));
  odpoved = read_char();
  serial_println_char(odpoved);
  if (odpoved == 'Y')
    zapis_choreografiu_do_EEPROM(cislo);
}

void skus_nacitat_choreografiu_z_EEPROM()
{
  serial_print_flash(PSTR("Cislo? [1-3]: "));
  char odpoved = read_char();
  if ((odpoved < '1') || (odpoved > '3')) return;
  serial_println_char(odpoved);
  uint8_t cislo = odpoved - '0';

  serial_print_flash(PSTR("Precitat choreografiu z EEPROM c."));
  serial_print_char(odpoved);
  serial_print_flash(PSTR("? [Y/n]: "));
  odpoved = read_char();
  serial_println_char(odpoved);
  if (odpoved == 'Y')
    precitaj_choreografiu_z_EEPROM(cislo);
}

void skus_naformatovat_EEPROM_choreografie()
{
  serial_print_flash(PSTR("Formatovat EEPROM choreografii? [Y/n]:"));
  char odpoved = read_char();
  serial_println_char(odpoved);
  if (odpoved == 'Y')
    naformatuj_EEPROM_choreografie();
}

void  zapis_choreografiu_do_EEPROM(int slot)
{
  uint8_t b3 = EEPROM.read(0);
  uint8_t len1 = EEPROM.read(21);
  uint8_t len2 = EEPROM.read(22);
  uint8_t len3 = EEPROM.read(23);
  uint16_t wp = 65535;

  if ((len1 > CHOREO_LEN) || (len2 > CHOREO_LEN) || (len3 > CHOREO_LEN) || (b3  > 250 - len3) || ((len1 > b3) && (b3 != 0)) || (len3 + b3  > 250 - len2) || (len1 + len2 > 250))
    b3 = len1 = len2 = len3 = 0;

  if (slot == 1)
  {
    if (((ch_len < b3) || (len3 == 0)) && (ch_len + len2 + len3 <= 250))
    {
      EEPROM.write(21, ch_len);
      wp = 24;
    }
  }
  else if (slot == 2)
  {
    if ((250 - b3 - len3 > ch_len) && (ch_len + len1 + len3 <= 250))
    {
      EEPROM.write(22, ch_len);
      wp = 1000 - ch_len * 4;
    }
  }
  else if (slot == 3)
  {
    if (ch_len + len1 + len2 <= 250)
    {
      EEPROM.write(23, ch_len);
      EEPROM.write(0, (len1 - len2) / 2 + 125);
      wp = 4 * ((len1 - len2) / 2 + 125);
    }
  }

  if (wp == 65535)
    serial_println_flash(PSTR("not enough space"));
  else
  {
    for (int i = 0; i < ch_len; i++)
    {
      EEPROM.write(wp + 4 * i, ch_time[i] & 255);
      EEPROM.write(wp + 1 + 4 * i, ch_time[i] >> 8);
      EEPROM.write(wp + 2 + 4 * i, ch_servo[i]);
      EEPROM.write(wp + 3 + 4 * i, ch_val[i]);
    }
    serial_println_flash(PSTR("ok"));
  }
}

void precitaj_choreografiu_z_EEPROM(uint8_t slot)
{
  uint8_t b3 = EEPROM.read(0);
  uint8_t len1 = EEPROM.read(21);
  uint8_t len2 = EEPROM.read(22);
  uint8_t len3 = EEPROM.read(23);
  uint16_t rp = 65535;
  if ((len1 > CHOREO_LEN) || (len2 > CHOREO_LEN) || (len3 > CHOREO_LEN) || (b3  > 250 - len3) || ((len1 > b3) && (b3 != 0)) || (len3 + b3  > 250 - len2) || (len1 + len2 > 250))
    b3 = len1 = len2 = len3 = 0;

  if (slot == 1)
  {
    if (len1 > 0)
    {
      rp = 24;
      ch_len = len1;
    }
  }
  else if (slot == 2)
  {
    if (len2 > 0)
    {
      ch_len = len2;
      rp = 1000 - ch_len * 4;
    }
  }
  else if (slot == 3)
  {
    if (len3 > 0)
    {
      rp = b3 * 4;
      ch_len = len3;
    }
  }

  if (rp == 65535)
    serial_println_flash(PSTR("couldn't"));
  else
  {
    for (int i = 0; i < ch_len; i++)
    {
      ch_time[i] = ((uint16_t)EEPROM.read(rp + 4 * i)) |
                   (((uint16_t)EEPROM.read(rp + 1 + 4 * i)) << 8);
      ch_servo[i] = EEPROM.read(rp + 2 + 4 * i);
      ch_val[i] = EEPROM.read(rp + 3 + 4 * i);
    }
    serial_println_flash(PSTR("ok"));
    pipni();
  }
}

void naformatuj_EEPROM_choreografie()
{
  EEPROM.write(0, 0);
  EEPROM.write(21, 0);
  EEPROM.write(22, 0);
  EEPROM.write(23, 0);
  serial_println_flash(PSTR("ok"));
  pipni();
}

void precitaj_kalibraciu_z_EEPROM()
{
  uint8_t value = EEPROM.read(1);
  if ((value != '~') && (value != '1') && (value != '2') && (value != '3')) return;
  if (value != '~') auto_start = value - '0';
  else auto_start = 0;
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

void zapis_kalibraciu_do_EEPROM()
{
  serial_print_flash(PSTR("Naozaj chces zapisat kalibraciu do EEPROM? [Y/n]: "));
  char odpoved = read_char();
  serial_println_char(odpoved);
  if (odpoved == 'Y')
  {
    char kalib_state = EEPROM.read(1);
    if ((kalib_state == '~') || ((kalib_state >= '1') && (kalib_state <= '3')))
      EEPROM.write(1, kalib_state);
    else EEPROM.write(1, '~');
    for (int i = 2; i < 8; i++)
    {
      int16_t k = kalib[i - 2] - (90-63);
      if (k < 0) k = 0;
      else if (k > 127) k = 127;
      if (servo_invertovane[i - 2]) k += 128;      
      EEPROM.write(i, (uint8_t)k);
    }
    for (int i = 0; i < 6; i++)
      EEPROM.write(9 + i, dolny_limit[i]);
    for (int i = 0; i < 6; i++)
      EEPROM.write(15 + i, horny_limit[i]);
    serial_println_flash(PSTR("ok"));
  }
}

// nasleduje softverova implementacia serioveho portu
#define SERIAL_STATE_IDLE      0
#define SERIAL_STATE_RECEIVING 1
#define SERIAL_BUFFER_LENGTH   20

static volatile uint8_t serial_state;
static uint8_t serial_buffer[SERIAL_BUFFER_LENGTH];
static volatile uint8_t serial_buf_wp, serial_buf_rp;

static volatile uint8_t receiving_byte;

static volatile uint32_t time_startbit_noticed;
static volatile uint8_t next_bit_order;
static volatile uint8_t waiting_stop_bit;
static uint16_t one_byte_duration;
static uint16_t one_bit_duration;
static uint16_t one_bit_write_duration;
static uint16_t half_of_one_bit_duration;

static uint8_t old_pinb, new_pinb;
static volatile uint32_t pulse_start;
static volatile uint8_t new_distance;

void init_serial(uint32_t baud_rate)
{
  pinMode(BT_RX, INPUT);
  pinMode(BT_TX, OUTPUT);

  serial_state = SERIAL_STATE_IDLE;

  one_byte_duration = 9500000 / baud_rate;
  one_bit_duration = 1000000 / baud_rate;
  one_bit_write_duration = one_bit_duration - 1;
  half_of_one_bit_duration = 500000 / baud_rate;

  PCMSK0 |= 16; //PCINT4;
  PCIFR &= ~1; //PCIF0;
  PCICR |= 1; // PCIE0;
}

ISR(PCINT0_vect)
{
  new_pinb = PINB;

  if ((new_pinb ^ old_pinb) & 1)
  {
    if (new_pinb & 1) pulse_start = micros();
    else 
    {
      distance = (int16_t)((micros() - pulse_start) / 58);
      new_distance = 1;
    }
  }

  if ((new_pinb ^ old_pinb) & 16)
  {
    uint32_t tm = micros();
    if (serial_state == SERIAL_STATE_IDLE)
    {
      time_startbit_noticed = tm;
      serial_state = SERIAL_STATE_RECEIVING;
      receiving_byte = 0xFF;
      next_bit_order = 0;
    }
    else if (tm - time_startbit_noticed > one_byte_duration)
    {
      serial_buffer[serial_buf_wp] = receiving_byte;
      serial_buf_wp++;
      if (serial_buf_wp == SERIAL_BUFFER_LENGTH) serial_buf_wp = 0;
      time_startbit_noticed = tm;
      receiving_byte = 0xFF;
      next_bit_order = 0;
    }
    else if (PINB & 16)
    {
      int8_t new_next_bit_order = (tm - time_startbit_noticed - half_of_one_bit_duration) / one_bit_duration;
      while (next_bit_order < new_next_bit_order)
      {
        receiving_byte &= ~(1 << next_bit_order);
        next_bit_order++;
      }
      if (next_bit_order == 8)
      {
        serial_buffer[serial_buf_wp] = receiving_byte;
        serial_buf_wp++;
        if (serial_buf_wp == SERIAL_BUFFER_LENGTH) serial_buf_wp = 0;
        serial_state = SERIAL_STATE_IDLE;
      }
    } else
      next_bit_order = (tm - time_startbit_noticed - half_of_one_bit_duration) / one_bit_duration;
  }
  old_pinb = new_pinb;
}

uint8_t serial_available()
{
  cli();
  if (serial_buf_rp != serial_buf_wp)
  {
    sei();
    return 1;
  }
  if (serial_state == SERIAL_STATE_RECEIVING)
  {
    uint32_t tm = micros();
    if (tm - time_startbit_noticed > one_byte_duration)
    {
      serial_state = SERIAL_STATE_IDLE;
      serial_buffer[serial_buf_wp] = receiving_byte;
      serial_buf_wp++;
      if (serial_buf_wp == SERIAL_BUFFER_LENGTH) serial_buf_wp = 0;
      sei();
      return 1;
    }
  }
  sei();
  return 0;
}

int16_t serial_read()
{
  cli();
  if (serial_buf_rp != serial_buf_wp)
  {
    uint8_t ch = serial_buffer[serial_buf_rp];
    serial_buf_rp++;
    if (serial_buf_rp == SERIAL_BUFFER_LENGTH) serial_buf_rp = 0;
    sei();
    return ch;
  }

  if (serial_state == SERIAL_STATE_RECEIVING)
  {
    uint32_t tm = micros();
    if (tm - time_startbit_noticed > one_byte_duration)
    {
      uint8_t ch = receiving_byte;
      serial_state = SERIAL_STATE_IDLE;
      sei();
      return ch;
    }
  }
  sei();
  return -1;
}

void serial_write(uint8_t ch)
{
#ifdef ECHO_BT_TO_USB
  Serial.print((char)ch);
#endif
  PORTD &= ~16;
  delayMicroseconds(one_bit_write_duration);
  for (uint8_t i = 0; i < 8; i++)
  {
    if (ch & 1) PORTD |= 16;
    else PORTD &= ~16;
    ch >>= 1;
    delayMicroseconds(one_bit_write_duration);
  }
  PORTD |= 16;
  delayMicroseconds(one_bit_write_duration);
  delayMicroseconds(one_bit_write_duration);
  delayMicroseconds(one_bit_write_duration);
  delayMicroseconds(one_bit_write_duration);
  delayMicroseconds(one_bit_write_duration);
}

uint16_t serial_readln(uint8_t *ln, uint16_t max_length)
{
  uint16_t len;
  int16_t ch;
  do {
    ch = serial_read();
    if (ch == 13) continue;
  } while (ch == -1);

  do {
    if ((ch != 13) && (ch != 10) && (ch != -1))
    {
      *(ln++) = ch;
      max_length--;
      len++;
    }
    ch = serial_read();
  } while ((ch != 13) && max_length);
  *ln = 0;
  return len;
}

void serial_print_num(int32_t number)
{
  if (number < 0)
  {
    serial_write('-');
    number = -number;
  }
  int32_t rad = 1;
  while (number / rad) rad *= 10;
  if (number > 0) rad /= 10;
  while (rad)
  {
    serial_write((char)('0' + (number / rad)));
    number -= (number / rad) * rad;
    rad /= 10;
  }
}

void serial_print_char(char ch)
{
  serial_write(ch);
}

void serial_print(const char *str)
{
  while (*str) serial_write(*(str++));
}

void serial_println(const char *str)
{
  serial_print(str);
  serial_write(13);
  serial_write(10);
}

void serial_print_flash(const char *str)
{
  int ln = strlen_P(str);
  for (int i = 0; i < ln; i++)
    serial_write(pgm_read_byte(str + i));
}

void serial_println_flash(const char *str)
{
  serial_print_flash(str);
  serial_write(13);
  serial_write(10);
}

void serial_println_num(int32_t number)
{
  serial_print_num(number);
  serial_println();
}

void serial_println_char(char ch)
{
  serial_write(ch);
  serial_println();
}

void serial_println()
{
  serial_write(13);
  serial_write(10);
}

// nasleduje citanie z utltazvukoveho senzora

void init_ultrasonic()
{
  pinMode(US_ECHO, INPUT);
  pinMode(US_TRIG, OUTPUT);

  PCMSK0 |= 1; //PCINT0;
  PCIFR &= ~1; //PCIF0;
  PCICR |= 1; // PCIE0;
}

void start_distance_measurement()
{
  distance = 10000;
  new_distance = 0;
  digitalWrite(US_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(US_TRIG, LOW);
}

void wait_for_distance_measurement_to_complete()
{
  uint8_t counter = 0;
  while ((counter < 20) && !new_distance)
  {
    delay(1);
    counter++;
  }
  if (counter == 20)
  {
    pinMode(US_ECHO, OUTPUT);
    digitalWrite(US_ECHO, HIGH);
    delayMicroseconds(10);
    digitalWrite(US_ECHO, LOW);
    pinMode(US_ECHO, INPUT);
    delayMicroseconds(5);
    distance = 10000;
  }
}

int16_t measure_distance()
{
  start_distance_measurement();
  wait_for_distance_measurement_to_complete();
  return distance;
}

//-------------------------------- nasleduje prehravanie melodie a hranie cez timer2 v pozadi
#define SIRENE_PORT  PORTB
#define SIRENE_DDR   DDRB
#define SIRENE_PIN   5

#define FIS3 2960
#define G3 3136

float octave_4[] = { 2093.00, 2217.46, 2349.32, 2489.02, 2637.02, 2793.83, 2959.96, 3135.96, 3322.44, 3520.00, 3729.31, 3951.07 };

//popcorn
uint16_t dlzka_melodia[] = {0, 386, 26, 281, 217, 36, 234 };
const uint8_t melodia1[] PROGMEM = { 252, 50, 149,  49,
                                     28, 31, 35, 40, 49, 99, 38, 49, 99, 40, 49, 99, 35, 49, 99, 31, 49, 99, 35, 49, 99, 28, 49, 99, 49,
                                     28, 31, 35, 40, 49, 99, 38, 49, 99, 40, 49, 99, 35, 49, 99, 31, 49, 99, 35, 49, 99, 28, 49, 99, 149,
                                     40, 49, 99, 42, 49, 99, 43, 49, 99, 42, 49, 99, 43, 49, 99, 40, 49, 99, 42, 49, 99, 40, 49, 99, 42, 49, 99, 38, 49, 99, 40, 49, 99, 38, 49, 99, 40, 49, 99, 36, 49, 99, 40, 49, 99,
                                     28, 31, 35, 40, 49, 99, 38, 49, 99, 40, 49, 99, 35, 49, 99, 31, 49, 99, 35, 49, 99, 28, 49, 99, 49,
                                     28, 31, 35, 40, 49, 99, 38, 49, 99, 40, 49, 99, 35, 49, 99, 31, 49, 99, 35, 49, 99, 28, 49, 99, 149,
                                     40, 49, 99, 42, 49, 99, 43, 49, 99, 42, 49, 99, 43, 49, 99, 40, 49, 99, 42, 49, 99, 40, 49, 99, 42, 49, 99, 38, 49, 99, 40, 49, 99, 38, 49, 99, 40, 49, 99, 42, 49, 99, 43, 49, 99,
                                     49, 35, 38, 43, 47, 49, 99, 45, 49, 99, 47, 49, 99, 43, 49, 99, 38, 49, 99, 43, 49, 99, 35, 49, 99,
                                     49, 35, 38, 43, 47, 49, 99, 45, 49, 99, 47, 49, 99, 43, 49, 99, 38, 49, 99, 43, 49, 99, 35, 49, 99, 149 ,
                                     47, 49, 99, 254, 49, 99, 255, 49, 99, 254, 49, 99, 255, 49, 99, 47, 49, 99, 254, 49, 99, 47, 49, 99, 254, 49, 99, 45, 49, 99, 47, 49, 99, 45, 49, 99, 47, 49, 99, 43, 49, 99, 47, 49, 99,
                                     49, 35, 38, 43, 47, 49, 99, 45, 49, 99, 47, 49, 99, 43, 49, 99, 38, 49, 99, 43, 49, 99, 35, 49, 99,
                                     49, 35, 38, 43, 47, 49, 99, 45, 49, 99, 47, 49, 99, 43, 49, 99, 38, 49, 99, 43, 49, 99, 35, 49, 99, 149 ,
                                     47, 49, 99, 254, 49, 99, 255, 49, 99, 254, 49, 99, 255, 49, 99, 47, 49, 99, 254, 49, 99, 47, 49, 99, 254, 49, 99, 45, 49, 99, 47, 49, 99, 45, 49, 99, 47, 49, 99, 254, 49, 99, 255, 49, 99
                                   };

//kohutik jarabi
const uint8_t melodia2[] PROGMEM = { 252, 150, 119, 121, 173, 174, 124, 124, 124, 123, 171, 173, 123, 123, 123, 121, 169, 171, 121, 121, 121, 123, 171, 169, 119, 119 };

//kankan
const uint8_t melodia3[] PROGMEM = { 252, 100,
                                     251, 1, 184, 1, 32, 126, 149, 251, 1, 184, 1, 32, 126, 149, 251, 1, 184, 1, 32, 126, 251, 1, 184, 1, 32, 126, 251, 1, 184, 1, 32, 126, 251, 1, 184, 1, 32, 126,
                                     64, 71, 71, 73, 71, 69, 69, 73, 74, 78, 81, 78, 78, 76, 251, 1, 184, 1, 32, 126, 78, 68, 68, 78, 76, 69, 69, 73, 73, 71, 73, 71, 85, 83, 85, 83,
                                     64, 71, 71, 73, 71, 69, 69, 73, 74, 78, 81, 78, 78, 76, 251, 1, 184, 1, 32, 126, 78, 68, 68, 78, 76, 69, 69, 73, 73, 71, 73, 71, 71, 69, 119,
                                     135, 131, 128, 126, 75, 76, 78, 80, 81, 76, 80, 76, 81, 76, 80, 76, 81, 76, 80, 76, 81, 76, 80, 76,
                                     251, 2, 11, 3, 16, 19, 251, 1, 4, 3, 16, 19,
                                     251, 1, 4, 3, 16, 19, 251, 1, 4, 3, 16, 19,
                                     251, 1, 4, 3, 16, 19, 251, 1, 4, 3, 16, 19,
                                     251, 1, 4, 3, 16, 19, 251, 1, 4, 3, 16, 19,
                                     174, 88, 91, 90, 88, 143, 143, 93, 95, 90, 91, 138, 138, 88, 91, 90, 88, 86, 86, 85, 83, 81, 79, 78, 76,
                                     174, 88, 91, 90, 88, 143, 143, 93, 95, 90, 91, 138, 138, 88, 91, 90, 88, 86, 93, 89, 90, 136,
                                     64, 71, 71, 73, 71, 69, 69, 73, 74, 78, 81, 78, 78, 76, 251, 1, 184, 1, 32, 126,
                                     78, 76, 126, 78, 76, 126, 78, 76, 126, 78, 76, 126, 78, 76, 126, 78, 76, 126,
                                     78, 76, 78, 76, 78, 76, 78, 76,
                                     131, 119, 119, 119, 169
                                   };

//labutie jazero
const uint8_t melodia4[] PROGMEM = {
  252, 220, 66, 69, 73, 69, 66, 69, 73, 69, 66, 69, 73, 69, 66, 69, 73, 69,
  185, 78, 80, 81, 83, 251, 5, 39, 3, 8, 81, 251, 5, 39, 3, 8, 81, 251, 5, 39, 3, 8, 78, 81, 78, 73, 81, 178,
  99, 83, 81, 80,
  185, 78, 80, 81, 83, 251, 5, 39, 3, 8, 81, 251, 5, 39, 3, 8, 81, 251, 5, 39, 3, 8, 78, 81, 78, 73, 81, 178,
  149, 128, 130, 131, 133, 85, 86, 251, 6, 32, 3, 8, 86, 135, 86, 88, 251, 6, 224, 3, 8, 88, 136, 88, 90, 251, 7, 184, 3, 8, 90, 85, 81, 80, 78,
  130, 131, 133, 85, 86, 251, 6, 32, 3, 8, 86, 135, 86, 88, 251, 6, 224, 3, 8, 88, 136, 88, 90, 251, 7, 73, 3, 8, 86, 133, 86, 91, 251, 7, 184, 3, 8, 87, 251, 7, 184, 3, 8, 85,
  185, 78, 80, 81, 83, 251, 5, 39, 3, 8, 81, 251, 5, 39, 3, 8, 81, 251, 5, 39, 3, 8, 78, 81, 78, 73, 81, 178,
  99, 83, 81, 80,
  185, 78, 80, 81, 83, 251, 5, 39, 3, 8, 81, 251, 5, 39, 3, 8, 81, 251, 5, 39, 3, 8, 78, 81, 78, 73, 81, 178
};

//let it be https://www.musicnotes.com/sheetmusic/mtd.asp?ppn=MN0101556
const uint8_t melodia5[] PROGMEM = { 252, 200, 26, 26, 76, 26, 253, 78, 73, 76, 76, 31, 253, 83, 35, 85, 253, 85, 
                                     83, 83, 81, 131, 35, 253, 85, 86, 35, 35, 85, 83, 99, 35, 33, 83, 81, 181 
};

// koleda
const uint8_t melodia6[] PROGMEM = { 252, 50, 121, 130, 128, 126, 171, 121, 71, 71, 121, 130, 128, 126, 173, 123, 
        123, 123, 131, 130, 128, 175, 125, 125, 133, 133, 131, 128, 230, 121, 130, 128, 126, 171, 121, 
        71, 71, 121, 130, 128, 126, 173, 123, 123, 123, 131, 130, 128, 133, 133, 133, 133, 135, 133, 
        131, 140, 238, 130, 130, 180, 130, 130, 180, 130, 133, 126, 76, 78, 180, 130, 149, 131, 131, 
        131, 81, 81, 131, 130, 130, 80, 80, 130, 128, 128, 130, 178, 183, 130, 130, 180, 130, 130, 
        180, 130, 133, 126, 76, 78, 180, 130, 149, 131, 131, 131, 81, 81, 131, 130, 130, 80, 80, 
        133, 133, 131, 128, 176, 138, 149,
        121, 130, 128, 126, 171, 121, 71, 71, 121, 130, 128, 126, 173, 123, 
        123, 123, 131, 130, 128, 175, 125, 125, 133, 133, 131, 128, 230, 121, 130, 128, 126, 171, 121, 
        71, 71, 121, 130, 128, 126, 173, 123, 123, 123, 131, 130, 128, 133, 133, 133, 133, 135, 133, 
        131, 140, 238, 130, 130, 180, 130, 130, 180, 130, 133, 126, 76, 78, 180, 130, 149, 131, 131, 
        131, 81, 81, 131, 130, 130, 80, 80, 130, 128, 128, 130, 178, 183, 130, 130, 180, 130, 130, 
        180, 130, 133, 126, 76, 78, 180, 130, 149, 131, 131, 131, 81, 81, 131, 130, 130, 80, 80, 
        133, 133, 131, 128, 176, 138, 149 };

volatile int16_t music_speed = 800 / 16;
volatile const uint8_t *current_note ;
volatile uint16_t notes_remaining;
uint8_t dotted_note = 0;

void zahraj_melodiu(uint8_t cislo)
{
  if (cislo == 0) {
    zastav_melodiu();
    return;
  }
  if (cislo > 100) mp3_play(cislo - 100);
  else if (cislo > 220) mp3_set_volume(cislo - 220);
  else if (cislo == 1) current_note = melodia1;
  else if (cislo == 2) current_note = melodia2;
  else if (cislo == 3) current_note = melodia3;
  else if (cislo == 4) current_note = melodia4;
  else if (cislo == 5) current_note = melodia5;
  else if (cislo == 6) current_note = melodia6;
  notes_remaining = dlzka_melodia[cislo];

  next_note();
}

void next_note()
{
  uint16_t freq = 0, dur = 0;
  if (!notes_remaining) return;
  otto_translate_tone_flash(&freq, &dur);
  tone2(freq, dur);
}

void otto_translate_tone_flash(uint16_t *freq, uint16_t *del)
{
  do {
    uint8_t n = pgm_read_byte(current_note);
    if (n > 249)
    {
      if (n == 251)
      {
        current_note++;
        uint8_t f1 = pgm_read_byte(current_note);
        current_note++;
        uint8_t f2 = pgm_read_byte(current_note);
        current_note++;
        uint8_t d1 = pgm_read_byte(current_note);
        current_note++;
        uint8_t d2 = pgm_read_byte(current_note);
        *freq = (f1 << 8) + f2;
        *del = (music_speed * 16 * (long)d1) / d2;
        notes_remaining -= 4;
      }
      else if (n == 252)
      {
        current_note++;
        music_speed = pgm_read_byte(current_note);
        current_note++;
        notes_remaining -= 2;
        continue;
      }
      else if (n == 253) 
      {
        dotted_note = 1;
        current_note++;
        notes_remaining--;        
        continue;
      }
      else if (n == 254)
      {
        *freq = FIS3;
        *del = music_speed;
      }
      else if (n == 255)
      {
        *freq = G3;
        *del = music_speed;
      }
    }
    else
    {
      uint8_t len = n / 50;
      *del = music_speed;
      while (len--) *del *= 2;
      n = n % 50;
      if (n != 49)
      {
        uint8_t octave = (n + 5) / 12;
        n = (n + 5) % 12;
        float ffreq = octave_4[n];
        octave = 4 - octave;
        while (octave > 0)
        {
          ffreq /= 2.0;
          octave--;
        }
        *freq = (uint16_t)ffreq;
      }
      else *freq = 0;
    }
    notes_remaining--;
    current_note++;
    break;
  } while (1);
  if (dotted_note) 
  {
    *del += (*del) / 2;
    dotted_note = 0;
  }
}

static volatile uint8_t tone2_state;
static volatile uint8_t tone2_pause;
static volatile uint32_t tone2_len;

void init_tone2()
{
  notes_remaining = 0;
  tone2_pause = 0;
  dotted_note = 0;
  TCCR2A = 2;
  TCCR2B = 0;
  TIMSK2 = 2;
  SIRENE_DDR |= (1 << SIRENE_PIN);
}

ISR(TIMER2_COMPA_vect)
{
  if (!tone2_pause)
  {
    if (tone2_state)
    {
      SIRENE_PORT |= (1 << SIRENE_PIN);
      tone2_state = 0;
    }
    else
    {
      SIRENE_PORT &= ~(1 << SIRENE_PIN);
      tone2_state = 1;
    }
  }
  if ((--tone2_len) == 0)
  {
    TCCR2B = 0;
    tone2_pause = 0;
    next_note();
  }
}

void tone2(uint16_t freq, uint16_t duration)
{
  uint32_t period = ((uint32_t)1000000) / (uint32_t)freq;

  if (freq >= 977)  // prescaler 32
  {
    tone2_state = 0;
    tone2_len = ((uint32_t)duration * (uint32_t)1000) * 2 / period;
    if (tone2_len == 0) tone2_len++;
    TCNT2 = 0;
    OCR2A = (uint8_t) (250000 / (uint32_t)freq);
    TCCR2B = 3;
  }
  else if (freq >= 488) // prescaler 64
  {
    tone2_state = 0;
    tone2_len = ((uint32_t)duration * (uint32_t)1000) * 2 / period;
    if (tone2_len == 0) tone2_len++;
    TCNT2 = 0;
    OCR2A = (uint8_t) (125000 / (uint32_t)freq);
    TCCR2B = 4;
  }
  else if (freq >= 244) // prescaler 128
  {
    tone2_state = 0;
    tone2_len = ((uint32_t)duration * (uint32_t)1000) * 2 / period;
    if (tone2_len == 0) tone2_len++;
    TCNT2 = 0;
    OCR2A = (uint8_t) (62500 / (uint32_t)freq);
    TCCR2B = 5;
  }
  else if (freq >= 122) //prescaler 256
  {
    tone2_state = 0;
    tone2_len = ((uint32_t)duration * (uint32_t)1000) * 2 / period;
    if (tone2_len == 0) tone2_len++;
    TCNT2 = 0;
    OCR2A = (uint8_t) (31250 / (uint32_t)freq);
    TCCR2B = 6;
  }
  else if (freq >= 30) //prescaler 1024
  {
    tone2_state = 0;
    tone2_len = ((uint32_t)duration * (uint32_t)1000) * 2 / period;
    if (tone2_len == 0) tone2_len++;
 
   TCNT2 = 0;
    OCR2A = (uint8_t) (7813 / (uint32_t)freq);
    TCCR2B = 7;
  }
  else if (freq == 0)
  {
    tone2_pause = 1;
    tone2_state = 0;
    period = 1000000 / 500;
    tone2_len = ((uint32_t)duration * (uint32_t)1000) * 2 / period;
    TCNT2 = 0;
    OCR2A = (uint8_t) (125000 / (uint32_t)500);
    TCCR2B = 4;
  }
  else
  {
    TCCR2B = 0;
  }
}

void zastav_melodiu()
{
  notes_remaining = 0;
}

// dfplayer mini

// volume 0-30
void mp3_set_volume(uint8_t volume)
{
 mp3_send_packet(0x06, volume);  
}

void mp3_play(uint8_t song_number)
{
 mp3_send_packet(0x03, song_number);  
}

void mp3_send_byte(uint8_t pin, uint8_t val)
{
  pinMode(MP3_OUTPUT_PIN, OUTPUT);
  float start_transmission = micros();
  float one_bit = 1000000 / 9600.0;
  float next_change = start_transmission + one_bit;
  digitalWrite(pin, LOW);
  while (micros() < next_change);
  
  for (int i = 2; i < 10; i++)
  {
    if (val & 1) digitalWrite(pin, HIGH);
    else digitalWrite(pin, LOW);
    next_change = start_transmission + one_bit * i;
    val >>= 1;
    while (micros() < next_change);
  }

  digitalWrite(pin, HIGH);
  next_change = micros() + 2 * one_bit;
  while (micros() < next_change);
  pinMode(MP3_OUTPUT_PIN, INPUT);
}

void mp3_send_packet(uint8_t cmd, uint16_t param)
{
  mp3_send_byte(MP3_OUTPUT_PIN, 0x7E);
  mp3_send_byte(MP3_OUTPUT_PIN, 0xFF);
  mp3_send_byte(MP3_OUTPUT_PIN, 0x06);
  mp3_send_byte(MP3_OUTPUT_PIN, cmd);
  mp3_send_byte(MP3_OUTPUT_PIN, 0x00);
  mp3_send_byte(MP3_OUTPUT_PIN, (uint8_t)(param >> 8));
  mp3_send_byte(MP3_OUTPUT_PIN, (uint8_t)(param & 0xFF));
  uint16_t chksm = 0xFF + 0x06 + cmd + (param >> 8) + (param & 0xFF);
  chksm = -chksm;
  mp3_send_byte(MP3_OUTPUT_PIN, (uint8_t)(chksm >> 8));
  mp3_send_byte(MP3_OUTPUT_PIN, (uint8_t)(chksm & 0xFF));
  mp3_send_byte(MP3_OUTPUT_PIN, 0xEF);
}

int measure_bat()
{
  analogReference(INTERNAL); 
  float volt = analogRead(A0); 
  analogReference(DEFAULT);
  // volt * 1.1 * 242 / 22 / 1023  (22 KOhm out of 220+22=242 KOhm)

  // volt * 1.1 * 92.2 / 8.2 / 1023  (8k2 Ohm out of 82k+8k2=92.2 KOhm)
  return (int)(0.5 + 100.0 * volt * 0.01209021768);   
}

void check_battery()
{
  static long last_measurement = 0;
  static long measurement_count = 0;

  if (ignore_batteries) return;
  long tm = millis();
  if (tm - last_measurement > 500)
  {
    last_measurement = tm;
    if (measure_bat() < 620)  
    {
      measurement_count++;
      if (measurement_count < 5) return;
      delay(50);
//      usb_active = 1;
//      bt_active = 1;
      serial_println_flash(PSTR("!!!!!!!!!!!!!!!! Replace batteries !!!!!!!!!!!!!!!!!!!!")); 
      mp3_set_volume(0);
      for (int i = 0; i < 6; i++)
        s[i].detach();
      for (int i = 2; i < 20; i++)
        pinMode(i, INPUT);
      while(1) SOS();
    }
    else measurement_count = 0;
  }
}

void SOS()
{
  pinMode(SIRENA, OUTPUT);
  for (uint8_t i = 0; i < 3; i++)
  {
    tone2(1680, 100);
    delay(150);
  }
  delay(300);
  for (uint8_t i = 0; i < 3; i++)
  {
    tone2(1680, 300);
    delay(350);
  }
  delay(300);
  for (uint8_t i = 0; i < 3; i++)
  {
    tone2(1680, 100);
    delay(150);
  }
  delay(500);
}
