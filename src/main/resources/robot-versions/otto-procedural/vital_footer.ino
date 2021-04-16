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


// SHARED WITH SERIAL & ULTRASOUND
ISR(PCINT0_vect)
{
  new_pinb = PINB;
	
	#ifdef MODULE_ULTRASOUND
		if ((new_pinb ^ old_pinb) & 1)
		{
			if (new_pinb & 1) pulse_start = micros();
			else 
			{
				distance = (int16_t)((micros() - pulse_start) / 58);
				new_distance = 1;
			}
		}
	#endif

	#ifdef MODULE_SERIAL
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
	#endif

  old_pinb = new_pinb;
}

