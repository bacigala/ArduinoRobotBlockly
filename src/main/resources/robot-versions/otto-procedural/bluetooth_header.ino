// MODULE BLUETOOTH - HEADER

#define MODULE_BLUETOOTH 1

#define BT_RX   12
#define BT_TX   4

#define BLUETOOTH_STATE_IDLE      0
#define BLUETOOTH_STATE_RECEIVING 1
#define BLUETOOTH_BUFFER_LENGTH   20

static volatile uint8_t bluetooth_state;
static uint8_t bluetooth_buffer[BLUETOOTH_BUFFER_LENGTH];
static volatile uint8_t bluetooth_buf_wp, bluetooth_buf_rp;

static volatile uint8_t receiving_byte;

static volatile uint32_t time_startbit_noticed;
static volatile uint8_t next_bit_order;
static volatile uint8_t waiting_stop_bit;
static uint16_t one_byte_duration;
static uint16_t one_bit_duration;
static uint16_t one_bit_write_duration;
static uint16_t half_of_one_bit_duration;