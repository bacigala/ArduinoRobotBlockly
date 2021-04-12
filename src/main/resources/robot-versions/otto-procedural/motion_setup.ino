
// MOTION SETUP

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

krok = 7;
spomalenie = 6;
