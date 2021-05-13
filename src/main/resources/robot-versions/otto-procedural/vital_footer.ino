// MODULE VITAL -  FOOTER

/*
 *  BATTERY MONITORING
 */

int measure_battery()
{
  analogReference(INTERNAL);
  float volt = analogRead(A0);
  analogReference(DEFAULT);
  // volt * 1.1 * 92.2 / 8.2 / 1023 (8k2 Ohm out of 82k+8k2=92.2 KOhm)
  return (int)(0.5 + 100.0 * volt * 0.01209021768);
}

// check battery, stop robot if necessary
void check_battery()
{
  static long last_measurement = 0;
  static long low_battery_measurement_count = 0;

  if (ignore_batteries) return;

  long time = millis();
  if (time - last_measurement < 500) return;

  last_measurement = time;

  if (measure_battery() > 620) {
    // battery OK
    low_battery_measurement_count = 0;
    return;
  }
  low_battery_measurement_count++;
  if (low_battery_measurement_count < 5) return;

  // battery level is too low -> send SOS signal, stop robot
  delay(50);
#ifdef MODULE_BLUETOOTH
  bluetooth_println_flash(PSTR("!!!!!!!!!!!!!!!! Replace batteries !!!!!!!!!!!!!!!!!!!!"));
#endif
#ifdef MODULE_SERIAL
  Serial.println(F("!!!!!!!!!!!!!!!! Replace batteries !!!!!!!!!!!!!!!!!!!!"));
#endif
#ifdef MODULE_MOTION
  motion_disable();
#endif
  for (int i = 2; i < 20; i++)
    pinMode(i, INPUT);
#ifdef MODULE_SOUND
  mp3_set_volume(0);
  while(1) SOS();
#endif
  while(1); //stop
}


/*
 *  INTERRUPT MANAGEMENT
 */

static uint8_t old_pinb, new_pinb;

ISR(PCINT0_vect)
{
  new_pinb = PINB;

  // listen to ultrasound measurement completion
	#ifdef MODULE_SENSOR
		if ((new_pinb ^ old_pinb) & 1) {
			if (new_pinb & 1)
			  pulse_start = micros();
			else {
				distance = (int16_t)((micros() - pulse_start) / 58);
				new_distance = 1;
			}
		}
	#endif

	#ifdef MODULE_BLUETOOTH
		if ((new_pinb ^ old_pinb) & 16)	{
			uint32_t tm = micros();
			if (bluetooth_state == BLUETOOTH_STATE_IDLE)
			{
				time_startbit_noticed = tm;
				bluetooth_state = BLUETOOTH_STATE_RECEIVING;
				receiving_byte = 0xFF;
				next_bit_order = 0;
			}
			else if (tm - time_startbit_noticed > one_byte_duration)
			{
				bluetooth_buffer[bluetooth_buf_wp] = receiving_byte;
				bluetooth_buf_wp++;
				if (bluetooth_buf_wp == BLUETOOTH_BUFFER_LENGTH) bluetooth_buf_wp = 0;
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
					bluetooth_buffer[bluetooth_buf_wp] = receiving_byte;
					bluetooth_buf_wp++;
					if (bluetooth_buf_wp == BLUETOOTH_BUFFER_LENGTH) bluetooth_buf_wp = 0;
					bluetooth_state = BLUETOOTH_STATE_IDLE;
				}
			} else
				next_bit_order = (tm - time_startbit_noticed - half_of_one_bit_duration) / one_bit_duration;
		}
	#endif

  old_pinb = new_pinb;
}