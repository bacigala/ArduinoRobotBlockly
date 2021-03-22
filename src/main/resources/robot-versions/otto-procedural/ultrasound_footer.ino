
// ULTRASOUND FOOTER

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
