
// ULTRASOUND HEADER

#define MODULE_ULTRASOUND 1
#define US_TRIG  7
#define US_ECHO  8

volatile int16_t distance;

static volatile uint32_t pulse_start;
static volatile uint8_t new_distance;

// TOUCH HEADER

#define TOUCH1   16    // A2
#define TOUCH2   17    // A3
