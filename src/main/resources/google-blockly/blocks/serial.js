
// SERIAL BLOCKS DEFINITION

Blockly.defineBlocksWithJsonArray([
// Send line of text.
{
  "type": "serial_println",
  "message0": "SERIAL send %1",
  "args0": [
    {
      "type": "input_value",
      "name": "MESSAGE",
      "check": ["String", "Number"],
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 180,
  "tooltip": "Sends specified text over serial connection, followed by a newline symbol.",
  "helpUrl": ""
},
// Read String from serial.
{
  "type": "serial_get_string",
  "message0": "SERIAL read String",
  "output": "String",
  "colour": 180,
  "tooltip": "Reads one line of text from serial connection.",
  "helpUrl": ""
},
// Read integer from serial.
{
  "type": "serial_get_number",
  "message0": "SERIAL read number",
  "output": "Number",
  "colour": 180,
  "tooltip": "Reads one number from serial connection.",
  "helpUrl": ""
}
]);