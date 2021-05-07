// MODULE SERIAL - FOOTER

String serial_read_str()
{
  serial_dump_and_wait();
  return Serial.readStringUntil(10);
}

int serial_read_num()
{
  serial_dump_and_wait();
  return Serial.parseInt();
}

void serial_dump_and_wait()
{
  while (Serial.available())
    Serial.read();
  while (!Serial.available()) {
    delay(50);
    check_battery();
  }
}