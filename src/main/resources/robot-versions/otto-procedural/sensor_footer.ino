// MODULE SENSOR - FOOTER

/*
 * ULTRASOUND
 */

void init_ultrasonic()
{
  pinMode(US_ECHO, INPUT);
  pinMode(US_TRIG, OUTPUT);

  PCMSK0 |= 1; //PCINT0;
  PCIFR &= ~1; //PCIF0;
  PCICR |= 1;  //PCIE0;
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
  while ((counter < 20) && !new_distance) {
    delay(1);
    counter++;
  }
  if (counter == 20) {
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

/**
 *	Wait for ultrasonic gesture input (# defined distance is recognised)
 *  Result is stored in global uint16_t US_last_seen_gesture
 * 
 *	@param max_distance - greater distances do not count
 *	@param time_ignore - shortest time between measurements
 * 					(if distance < max_distance for more than time_ignore, gesture++ is recognised
 * 	@param time_max_wait - no gesture recognised for more than this exits gesture recognition
 */
void ultrasonic_gesture_record(uint16_t max_distance, uint16_t time_ignore, uint16_t time_max_wait)
{
	uint16_t count = 0;
	uint32_t time_start = millis();
	int no_valid_distance = 0;
	
	while ((millis() - time_start) <= time_max_wait) {
		if (measure_distance() < max_distance) {
      no_valid_distance++;
      delay(10);
      if (no_valid_distance == 20) {
				count++;
				
        #ifdef MODULE_SOUND
				tone2(1680, 300);
        #endif

        // *** wait time_ignore OR recognise distance > max_distance
        no_valid_distance = 0;
				time_start = millis();
        while ((millis() - time_start) <= time_ignore) {
          if (measure_distance() >= max_distance) {
            no_valid_distance++;
            delay(10);
            if (no_valid_distance == 20)
              break;
          }
					if ((millis() - time_start) >= time_max_wait) {
						US_last_seen_gesture = count;
						return;
					}
        }
				// ***
				
				no_valid_distance = 0;
				time_start = millis();
			}
    }
	}
	US_last_seen_gesture = count;
}


/*
 * ULTRASOUND
 */

/**
 *	Wait for touch gesture input (bumps / hold time)
 *  Result is stored in global uint16_t TOUCH_last_seen_gesture
 * 
 *	@param sensor - pin of sensor (TOUCH1 OR TOUCH2)
 *	@param time_ignore - time between pushes
 * 					(if button is not released in time_ignore, another bump is recorded)
 * 	@param time_max_wait - no push / release for more than this exits gesture recognition
 */
void touch_gesture_record(uint8_t sensor, uint16_t time_ignore, uint16_t time_max_wait)
{
	uint16_t count = 0;
	uint8_t no_valid_gesture = 0;
	uint32_t time_start = millis();

	while ((millis() - time_start) <= time_max_wait) {
		if (digitalRead(sensor)) {
      no_valid_gesture++;
      delay(10);
      if (no_valid_gesture == 20) {
				count++;
				
        #ifdef MODULE_SOUND
				tone2(1680, 300);
        #endif

        // *** wait time_ignore OR recognise button release
        no_valid_gesture = 0;
				time_start = millis();
        while ((millis() - time_start) <= time_ignore) {
          if (!digitalRead(sensor)) {
            no_valid_gesture++;
            delay(10);
            if (no_valid_gesture == 20)
              break;
          }
					if ((millis() - time_start) >= time_max_wait) {
						TOUCH_last_seen_gesture = count;
						return;
					}
        }
				// ***
        
        no_valid_gesture = 0;
				time_start = millis();
			}
    }
	}
	TOUCH_last_seen_gesture = count;
}