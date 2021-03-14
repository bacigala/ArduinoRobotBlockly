Blockly.defineBlocksWithJsonArray([{
  "type": "motor_move",
  "message0": "Wait %1 ms, move %2 %3 to position %4 %5",
  "args0": [
    {
      "type": "field_number",
      "name": "WAIT_TIME",
      "value": 1000,
      "min": 0,
      "max": 10000,
      "precision": 1
    },
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
{
  "type": "comment_block",
  "message0": "%1",
  "args0": [
    {
      "type": "field_input",
      "name": "COMMENT",
      "text": "comment"
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 60,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "block_dance_total_time",
  "message0": "End dance in %1 seconds",
  "args0": [
    {
      "type": "field_number",
      "name": "DURATION",
      "value": 10,
      "min": 0,
      "max": 1000,
      "precision": 1
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 0,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "block_jump_to_line",
  "message0": "Jump to line %1",
  "args0": [
    {
      "type": "field_number",
      "name": "LINE_NO",
      "value": 1,
      "min": 0,
      "max": 1000,
      "precision": 1
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 60,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "block_set_slowdown",
  "message0": "Set slowdown to %1",
  "args0": [
    {
      "type": "field_number",
      "name": "SLOWDOWN",
      "value": 6,
      "min": 0,
      "max": 6,
      "precision": 1
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 0,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "block_play_melody",
  "message0": "Play melody number %1",
  "args0": [
    {
      "type": "field_number",
      "name": "MELODY_NO",
      "value": 1,
      "min": 1,
      "max": 6,
      "precision": 1
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 285,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "block_stop_melody",
  "message0": "Stop playing melody.",
  "previousStatement": null,
  "nextStatement": null,
  "colour": 285,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "block_play_sound_effect",
  "message0": "Play sound effect number %1",
  "args0": [
    {
      "type": "field_number",
      "name": "EFFECT_NO",
      "value": 1,
      "min": 1,
      "max": 16,
      "precision": 1
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 285,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "block_toogle_speaker",
  "message0": "Toogle speaker on/off.",
  "previousStatement": null,
  "nextStatement": null,
  "colour": 230,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "otto_basic_loop",
  "message0": "Program %1 %2",
  "args0": [
    {
      "type": "input_dummy"
    },
    {
      "type": "input_statement",
      "name": "PROGRAM"
    }
  ],
  "colour": 180,
  "tooltip": "",
  "helpUrl": ""
}
]);