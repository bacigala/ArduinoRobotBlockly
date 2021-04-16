
// ULTRASOUND FOOTER

void init_ultrasonic()
{
  pinMode(US_ECHO, INPUT);
  pinMode(US_TRIG, OUTPUT);

  PCMSK0 |= 1; //PCINT0;
  PCIFR &= ~1; //PCIF0;
  PCICR |= 1; // PCIE0;
	
	US_last_seen_gesture = 0;
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

// ultrasound - recognize gesture
void ultrasonic_gesture_record(uint16_t max_distance, uint16_t max_time, uint16_t max_wait_time)
{
	uint16_t count = 0;
	uint32_t start_time = millis();
	int16_t distance = measure_distance();
	int no_valid_gesture_distance = 0;
	
	// wait for innitial gesture
	while (true) {
		if (distance < max_distance) {
      no_valid_gesture_distance++;
      delay(10);
      if (no_valid_gesture_distance == 20) {
				count++;
				no_valid_gesture_distance = 0;
#ifdef MODULE_SOUND
				tone2(1680, 300);
#endif
				delay(1000);
				break;
			}
    }
		distance = measure_distance();		
		if ((millis() - start_time) >= max_wait_time) {
			US_last_seen_gesture = count;
			return;
		}
	}
		
	start_time = millis();
	
	// count
	while ((millis() - start_time) <= max_time) {
		if (distance < max_distance) {
      no_valid_gesture_distance++;
      delay(10);
      if (no_valid_gesture_distance == 20) {
				count++;
				start_time = millis();
				no_valid_gesture_distance = 0;
#ifdef MODULE_SOUND
				tone2(1680, 300);
#endif
				delay(1000);
			}
    }
		distance = measure_distance();
	}
	US_last_seen_gesture = count;
}



