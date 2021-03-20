//VITAL FOOTER
int measure_bat()
{
  analogReference(INTERNAL); 
  float volt = analogRead(A0); 
  analogReference(DEFAULT);
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
#ifdef MODULE_SERIAL
      serial_println_flash(PSTR("!!!!!!!!!!!!!!!! Replace batteries !!!!!!!!!!!!!!!!!!!!")); 
#endif
#ifdef MODULE_SOUND
      mp3_set_volume(0);
#endif
#ifdef MODULE_MOTION
      for (int i = 0; i < 6; i++)
        s[i].detach();
#endif
      for (int i = 2; i < 20; i++)
        pinMode(i, INPUT);
#ifdef MODULE_SOUND
      while(1) SOS();
#else
			while(1);
#endif
    }
    else measurement_count = 0;
  }
}

#ifdef MODULE_SOUND
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
#endif
