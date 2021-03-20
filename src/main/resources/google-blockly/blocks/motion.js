
// MOTION BLOCKS DEFINITION

Blockly.defineBlocksWithJsonArray([
{
  "type": "otto_procedural_motor_move",
  "message0": "Move %1 %2 to position %3 %4",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "field_dropdown",
      "name": "MOTOR_NUMBER",
      "options": [
        [
          "Left arm",
          "5"
        ],
        [
          "Right arm",
          "6"
        ],
        [
          "Left leg",
          "4"
        ],
        [
          "Right leg",
          "3"
        ],
        [
          "Left foot",
          "2"
        ],
        [
          "Right foot",
          "1"
        ]
      ]
    },
    {
      "type": "input_dummy"
    },
    {
      "type": "field_number",
      "name": "MOTOR_POSITION",
      "value": 0,
      "min": 0,
      "max": 180,
      "precision": 1
    }
  ],
  "inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 120,
  "tooltip": "",
  "helpUrl": ""
},
// reset position of motors
{
  "type": "reset_motors",
  "message0": "Reset motor position.",
  "previousStatement": null,
  "nextStatement": null,
  "colour": 120,
  "tooltip": "",
  "helpUrl": ""
}
]);