
// BLUETOOTH BLOCKS DEFINITIONS

Blockly.defineBlocksWithJsonArray([
// Send line of text.
{
  "type": "bluetooth_print_str",
  "message0": "BT send String %1",
  "args0": [
    {
      "type": "input_value",
      "name": "MESSAGE",
      "check": ["String"],
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 240,
  "tooltip": "Sends specified text over bluetooth serial connection, followed by a newline symbol.",
  "helpUrl": ""
},
// Send number.
{
  "type": "bluetooth_print_num",
  "message0": "BT send number %1",
  "args0": [
    {
      "type": "input_value",
      "name": "MESSAGE",
      "check": ["Number"],
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 240,
  "tooltip": "Sends specified number over bluetooth serial connection, followed by a newline symbol.",
  "helpUrl": ""
},
// Read String from serial.
{
  "type": "bluetooth_get_string",
  "message0": "BT read String",
  "output": "String",
  "colour": 240,
  "tooltip": "Reads one line of text from bluetooth serial connection.",
  "helpUrl": ""
},
// Read integer from serial.
{
  "type": "bluetooth_get_number",
  "message0": "BT read number",
  "output": "Number",
  "colour": 240,
  "tooltip": "Reads one number from bluetooth serial connection.",
  "helpUrl": ""
}
]);