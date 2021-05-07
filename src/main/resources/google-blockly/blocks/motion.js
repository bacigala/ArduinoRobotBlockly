
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
      "type": "input_value",
      "name": "MOTOR_POSITION",
      "check": "Number"
    }
  ],
  "inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 60,
  "tooltip": "",
  "helpUrl": ""
},
// reset position of motors
{
  "type": "reset_motors",
  "message0": "Reset motor position.",
  "previousStatement": null,
  "nextStatement": null,
  "colour": 60,
  "tooltip": "",
  "helpUrl": ""
},
// coplex move forward / backward
{
  "type": "move_complex",
  "message0": "Go %1",
  "args0": [
    {
      "type": "field_dropdown",
      "name": "DIRECTION",
      "options": [
        [
          "forward",
          "FWD"
        ],
        [
          "backward",
          "BWD"
        ]
      ]
    }
  ],
  "inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 60,
  "tooltip": "",
  "helpUrl": ""
},
// set slowdown
{
  "type": "set_slowdown",
  "message0": "Set slowdown to %1",
  "args0": [
    {
      "type": "input_value",
      "name": "SLOWDOWN_LEVEL",
      "check": "Number"
    }
  ],
  "inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 60,
  "tooltip": "",
  "helpUrl": ""
},
// complex turn left / right
{
  "type": "turn_complex",
  "message0": "Turn %1",
  "args0": [
    {
      "type": "field_dropdown",
      "name": "DIRECTION",
      "options": [
        [
          "left",
          "LEFT"
        ],
        [
          "rigth",
          "RIGHT"
        ]
      ]
    }
  ],
  "inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 60,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "tiptoes",
  "message0": "Stand on tiptoes",
  "inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 60,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "heels",
  "message0": "Stand on heels",
  "inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 60,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "wave_hand",
  "message0": "Wave hand",
  "inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 60,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "wave_hand2",
  "message0": "Wave hand 2",
  "inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 60,
  "tooltip": "",
  "helpUrl": ""
}
]);