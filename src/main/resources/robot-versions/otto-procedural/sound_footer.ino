// MODULE SOUND - FOOTER

/*
 * SOUND EFFECTS
 */

void sound_effect1() {
  uint16_t fre = 100;
  for (int i = 0; i < 200; i++) {
    tone2(fre, 2);
    fre += 10;
    delay(2);
  }
}

void sound_effect2() {
  uint16_t fre = 2060;
  for (int i = 0; i < 200; i++) {
    tone2(fre, 2);
    fre -= 10;
    delay(2);
  }
}

void sound_effect3() {
  uint16_t fre;
  for (int i = 0; i < 200; i++) {
    fre = random(3000) + 20;
    tone2(fre, 2);
    delay(2);
  }
}

void sound_effect4() {
  uint16_t fre = 100;
  for (int i = 0; i < 200; i++) {
    (i & 1) ? tone2(fre, 2) : tone2(2100 - fre, 2);
    fre += 10;
    delay(2);
  }
}

void sound_effect5() {
  uint16_t fre = 100;
  for (int i = 0; i < 100; i++) {
    (i & 1) ? tone2(fre, 4) : tone2(2100 - fre, 4);
    fre += 20;
    delay(4);
  }
}

void sound_effect6() {
  uint16_t d = 12;
  for (int i = 0; i < 20; i++) {
    tone2(1760, 10);
    delay(d);
    d += 3;
  }
}

void sound_effect7() {
  for (int i = 0; i < 40; i++) {
    if (i & 1) tone2(1760, 10);
    delay(10);
  }
}

void sound_effect8() {
  uint16_t d = 72;
  for (int i = 0; i < 20; i++) {
    tone2(1760, 10);
    delay(d);
    d -= 3;
  }
}

void sound_effect9() {
  float fre = 3500;
  while (fre > 30) {
    tone2((uint16_t)fre, 2);
    fre *= 0.97;
    delay(2);
  }
}

void sound_effect10() {
  float fre = 30;
  while (fre < 3000) {
    tone2((uint16_t)fre, 2);
    fre *= 1.03;
    delay(2);
  }
}

void sound_effect11() {
  uint16_t fre = 3500;
  uint16_t d = 42;
  for (int i = 0; i < 20; i++) {
    tone2(fre, d);
    fre -= 165;
    delay(d);
    d -= 2;
  }
}

void sound_effect12() {
  float fre = 110;
  uint8_t d = 42;
  for (int i = 0; i < 20; i++) {
    tone2(fre, d);
    fre += 165;
    delay(d);
    d -= 2;
  }
}

void sound_effect13() {
  uint16_t fre = 3400;
  uint8_t d = 200;
  uint8_t delta = 1;
  for (int i = 0; i < 20; i++) {
    tone2(fre, d);
    fre -= 165;
    delay(d);
    d -= delta;
    delta++;
  }
  tone2(110, 1000);
  delay(1000);
}

void sound_effect14() {
  uint16_t fre = 880;
  int16_t d = 20;
  for (int i = 0; i < 20; i++) {
    tone2(fre, d);
    delay(d);
    fre += random(50) - 25;
    d += random(10) - 5;
    if (d < 0) d = 1;
  }
}

void sound_effect15() {
  uint16_t fre = 440;
  int16_t d = 20;
  for (int i = 0; i < 20; i++) {
    tone2(fre, d);
    delay(d);
    fre += random(25) - 12;
    d += random(10) - 5;
    if (d < 0) d = 1;
  }
}

void sound_effect16() {
  uint16_t fre = 1760;
  int16_t d = 20;
  for (int i = 0; i < 20; i++) {
    tone2(fre, d);
    delay(d);
    fre += random(100) - 50;
    d += random(10) - 5;
    if (d < 0) d = 1;
  }
}

void beep() {
  tone2(1568, 50);
  delay(100);
  tone2(1357, 50);
}


/*
 * MELODIES
 */

// used when battery level is too low (MODULE VITAL)
void SOS()
{
  pinMode(SIREN, OUTPUT);
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

void melody_jedna_druhej()
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

#define SIREN_PORT  PORTB
#define SIREN_DDR   DDRB
#define SIREN_PIN   5

#define FIS3 2960
#define G3 3136

float octave_4[] = { 2093.00, 2217.46, 2349.32, 2489.02, 2637.02, 2793.83, 2959.96, 3135.96, 3322.44, 3520.00, 3729.31, 3951.07 };
uint16_t melody_length[] = {0, 386, 26, 281, 217, 36, 234 };

//popcorn
const uint8_t melody1[] PROGMEM = { 252, 50, 149,  49,
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
const uint8_t melody2[] PROGMEM = { 252, 150, 119, 121, 173, 174, 124, 124, 124, 123, 171, 173, 123, 123, 123, 121, 169, 171, 121, 121, 121, 123, 171, 169, 119, 119 };

//kankan
const uint8_t melody3[] PROGMEM = { 252, 100,
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
const uint8_t melody4[] PROGMEM = {
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
const uint8_t melody5[] PROGMEM = { 252, 200, 26, 26, 76, 26, 253, 78, 73, 76, 76, 31, 253, 83, 35, 85, 253, 85,
                                     83, 83, 81, 131, 35, 253, 85, 86, 35, 35, 85, 83, 99, 35, 33, 83, 81, 181 
};

// koleda
const uint8_t melody6[] PROGMEM = { 252, 50, 121, 130, 128, 126, 171, 121, 71, 71, 121, 130, 128, 126, 173, 123,
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
volatile const uint8_t *current_note;
volatile uint16_t notes_remaining;
uint8_t dotted_note = 0;

void melody_play(uint8_t melody_number)
{
  if (melody_number == 1) current_note = melody1;
  else if (melody_number == 2) current_note = melody2;
  else if (melody_number == 3) current_note = melody3;
  else if (melody_number == 4) current_note = melody4;
  else if (melody_number == 5) current_note = melody5;
  else if (melody_number == 6) current_note = melody6;
	else if (melody_number == 7) {
		melody_jedna_druhej();
		return;
	}
  notes_remaining = melody_length[melody_number];
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
  SIREN_DDR |= (1 << SIREN_PIN);
}

ISR(TIMER2_COMPA_vect)
{
  if (!tone2_pause)
  {
    if (tone2_state)
    {
      SIREN_PORT |= (1 << SIREN_PIN);
      tone2_state = 0;
    }
    else
    {
      SIREN_PORT &= ~(1 << SIREN_PIN);
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

void melody_stop()
{
  notes_remaining = 0;
}


/*
 * MP3 - dfplayer mini
 */

// volume 0-30
void mp3_set_volume(uint8_t volume)
{
  if (volume > 30) volume = 30;
  mp3_volume = volume;
  mp3_send_packet(0x06, volume);
}

void mp3_play(uint8_t song_number)
{
  mp3_send_packet(0x03, song_number);
}

void mp3_playback()
{
  mp3_send_packet(0x0D, 0x00);
}

void mp3_pause()
{
  mp3_send_packet(0x0E, 0x00);
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