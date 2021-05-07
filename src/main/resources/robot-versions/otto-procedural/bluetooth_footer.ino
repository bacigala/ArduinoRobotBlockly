// MODULE BLUETOOTH - FOOTER

void init_bluetooth(uint32_t baud_rate)
{
  pinMode(BT_RX, INPUT);
  pinMode(BT_TX, OUTPUT);

  bluetooth_state = BLUETOOTH_STATE_IDLE;

  one_byte_duration = 9500000 / baud_rate;
  one_bit_duration = 1000000 / baud_rate;
  one_bit_write_duration = one_bit_duration - 1;
  half_of_one_bit_duration = 500000 / baud_rate;

  PCMSK0 |= 16; //PCINT4;
  PCIFR &= ~1; //PCIF0;
  PCICR |= 1; // PCIE0;
}

uint8_t bluetooth_available()
{
  cli();
  if (bluetooth_buf_rp != bluetooth_buf_wp) {
    sei();
    return 1;
  }
  if (bluetooth_state == BLUETOOTH_STATE_RECEIVING) {
    uint32_t tm = micros();
    if (tm - time_startbit_noticed > one_byte_duration) {
      bluetooth_state = BLUETOOTH_STATE_IDLE;
      bluetooth_buffer[bluetooth_buf_wp] = receiving_byte;
      bluetooth_buf_wp++;
      if (bluetooth_buf_wp == BLUETOOTH_BUFFER_LENGTH) bluetooth_buf_wp = 0;
      sei();
      return 1;
    }
  }
  sei();
  return 0;
}

int16_t bluetooth_read()
{
  cli();
  if (bluetooth_buf_rp != bluetooth_buf_wp) {
    uint8_t ch = bluetooth_buffer[bluetooth_buf_rp];
    bluetooth_buf_rp++;
    if (bluetooth_buf_rp == BLUETOOTH_BUFFER_LENGTH) bluetooth_buf_rp = 0;
    sei();
    return ch;
  }

  if (bluetooth_state == BLUETOOTH_STATE_RECEIVING) {
    uint32_t tm = micros();
    if (tm - time_startbit_noticed > one_byte_duration) {
      uint8_t ch = receiving_byte;
      bluetooth_state = BLUETOOTH_STATE_IDLE;
      sei();
      return ch;
    }
  }
  sei();
  return -1;
}

void bluetooth_write(uint8_t ch)
{
  PORTD &= ~16;
  delayMicroseconds(one_bit_write_duration);
  for (uint8_t i = 0; i < 8; i++) {
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

String bluetooth_read_str()
{
  bluetooth_dump_and_wait();
  char line[40];
  uint16_t length_left = 40;
  uint16_t length = 0;
  int16_t ch;
  do {
    ch = bluetooth_read();
  } while ((ch == 13) || (ch == 10) || (ch == -1));

  while ((ch != 10) && length_left) {
    if ((ch != 13) && (ch != 10) && (ch != -1)) {
      line[length] = ch;
      length_left--;
      length++;
    }
    ch = bluetooth_read();
  }

  line[length] = 0;
  return String(line);
}

void bluetooth_print_num(int32_t number)
{
  if (number < 0) {
    bluetooth_write('-');
    number = -number;
  }
  int32_t rad = 1;
  while (number / rad) rad *= 10;
  if (number > 0) rad /= 10;
  while (rad) {
    bluetooth_write((char)('0' + (number / rad)));
    number -= (number / rad) * rad;
    rad /= 10;
  }
  bluetooth_write(13);
  bluetooth_write(10);
}

void bluetooth_print_char(char ch)
{
  bluetooth_write(ch);
}

void bluetooth_print(const char *str)
{
  while (*str) bluetooth_write(*(str++));
}

void bluetooth_println(const char *str)
{
  bluetooth_print(str);
  bluetooth_write(13);
  bluetooth_write(10);
}

void bluetooth_print_str(String s) {
  bluetooth_println(s.c_str());
}

void bluetooth_print_flash(const char *str)
{
  int ln = strlen_P(str);
  for (int i = 0; i < ln; i++)
    bluetooth_write(pgm_read_byte(str + i));
}

void bluetooth_println_flash(const char *str)
{
  bluetooth_print_flash(str);
  bluetooth_write(13);
  bluetooth_write(10);
}

void bluetooth_println_num(int32_t number)
{
  bluetooth_print_num(number);
  bluetooth_write(13);
  bluetooth_write(10);
}

void bluetooth_println_char(char ch)
{
  bluetooth_write(ch);
  bluetooth_write(13);
  bluetooth_write(10);
}

char read_char()
{
  while (!bluetooth_available());
  return bluetooth_read();
}

int bluetooth_read_num()
{
  bluetooth_dump_and_wait();
  int num = 0;
  int z;
  do {
    z = read_char();
  } while ((z < '0') || (z > '9'));
  while ((z >= '0') && (z <= '9')) {
    num *= 10;
    num += (z - '0');
    do {
      z = read_char();
      if (z == -1) delayMicroseconds(10);
    } while (z < 0);
  }
  return num;
}

void bluetooth_dump_and_wait() {
  while (bluetooth_available())
    bluetooth_read();
  while (!bluetooth_available()) {
    delay(50);
    check_battery();
  }
}