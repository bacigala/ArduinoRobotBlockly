
// BLOCK DEFINITIONS - SENSOR

Blockly.defineBlocksWithJsonArray([

// ULTRASOUND - measure and return distance
{
  "type": "otto_procedural_ultrasound_get_distance",
  "message0": "Ultrasound distance",
  "output": "Number",
  "colour": 85,
  "tooltip": "Measures current distance.",
  "helpUrl": ""
},

// ULTRASOUND - wait till distance is > OR < than specified Number
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
  "colour": 85,
  "tooltip": "Waits till measured distance is more than/less than/equal to specified value.",
  "helpUrl": ""
},

// ULTRASOUND - record gesture
{
  "type": "ulrasonic_gesture_record",
  "message0": "Ultrasound - record gesture.",
  "previousStatement": null,
  "nextStatement": null,
  "colour": 85,
  "tooltip": "Record gesture - how many times specified distance is measured.",
  "helpUrl": ""
},

// ULTRASOUND - get last seen gesture
{
  "type": "ulrasonic_gesture_last",
  "message0": "Ultrasound - last seen gesture",
  "output": "Number",
  "colour": 85,
  "tooltip": "Returns last seen gesture (Number).",
  "helpUrl": ""
},

// TOUCH - read button state (Boolean)
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
  "colour": 95,
  "tooltip": "Returns true if button is currently pressed.",
  "helpUrl": ""
},

// TOUCH - wait for button press
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
  "colour": 95,
  "tooltip": "Waits till button is pressed.",
  "helpUrl": ""
},

// TOUCH - record gesture
{
  "type": "touch_gesture_record",
  "message0": "Touch - record gesture on %1 button.",
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
  "colour": 95,
  "tooltip": "Record gesture - how many times button is pressed.",
  "helpUrl": ""
},

// TOUCH - get last seen gesture
{
  "type": "touch_gesture_last",
  "message0": "Touch - last seen gesture",
  "output": "Number",
  "colour": 95,
  "tooltip": "Returns last seen gesture (Number).",
  "helpUrl": ""
}

]);
