
// ULTRASOUD BLOCK DEFINITION

Blockly.defineBlocksWithJsonArray([
{
  "type": "otto_procedural_ultrasound_get_distance",
  "message0": "Ultrasound distance",
  "output": "Number",
  "colour": 90,
  "tooltip": "",
  "helpUrl": ""
},

// WAIT iltrasound
{
  "type": "wait_ultrasound",
  "message0": "Wait till distance is %1 %2 %3 cm.",
  "args0": [
    {
      "type": "field_dropdown",
      "name": "RELATION",
      "options": [
        [
          "more than",
          "MORE"
        ],
        [
          "less than",
          "LESS"
        ],
        [
          "equal to",
          "EQUAL"
        ]
      ]
    },
    {
      "type": "input_dummy"
    },
    {
      "type": "input_value",
      "name": "DISTANCE",
      "check": "Number"
    }
  ],
  "inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 90,
  "tooltip": "",
  "helpUrl": ""
},

// button state (boolean)
{
  "type": "button_pressed",
  "message0": "%1 button is pressed",
  "args0": [
    {
      "type": "field_dropdown",
      "name": "BUTTON",
      "options": [
        [
          "Left",
          "LEFT"
        ],
        [
          "Right",
          "RIGHT"
        ]
      ]
    }
  ],
  "inputsInline": true,
  "output": "Boolean",
  "colour": 90,
  "tooltip": "",
  "helpUrl": ""
},

// WAIT TOUCH
{
  "type": "wait_touch",
  "message0": "Wait till %1 button is pressed.",
  "args0": [
    {
      "type": "field_dropdown",
      "name": "BUTTON",
      "options": [
        [
          "left",
          "LEFT"
        ],
        [
          "right",
          "RIGHT"
        ]
      ]
    }
  ],
  "inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 90,
  "tooltip": "",
  "helpUrl": ""
}
]);