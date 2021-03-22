//VITAL HEADER

#include <avr/pgmspace.h>

static uint8_t ignore_batteries = 1;

// interrupts
static uint8_t old_pinb, new_pinb;
