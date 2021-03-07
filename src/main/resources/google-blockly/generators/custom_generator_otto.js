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
  "colour": 240,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "start_block",
  "message0": "START",
  "nextStatement": null,
  "colour": 120,
  "tooltip": "",
  "helpUrl": ""
},
{
  "type": "end_block",
  "message0": "END",
  "previousStatement": null,
  "colour": 0,
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
  "colour": 120,
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
}
]);

Blockly.basicOttoGenerator = new Blockly.Generator('basicOttoGenerator');
Blockly.basicOttoGenerator.PRECEDENCE = 0;

Blockly.basicOttoGenerator['start_block'] = function(block) {
	return "@";
};

Blockly.basicOttoGenerator['end_block'] = function(block) {
	return "0 0 0";
};

Blockly.basicOttoGenerator['motor_move'] = function(block) {
	var waitTime = block.getFieldValue('WAIT_TIME');
	var motorNo = block.getFieldValue('MOTOR_NUMBER');
	var motorPosition = block.getFieldValue('MOTOR_POSITION');
	var code = waitTime + " " + motorNo + " " + motorPosition + "\n";
	return code;
};

Blockly.basicOttoGenerator['comment_block'] = function(block) {
  var comment = block.getFieldValue('COMMENT');
  var code = '# ' + comment + '\n\r';
  return code;
};

Blockly.basicOttoGenerator['block_dance_total_time'] = function(block) {
  var duration = block.getFieldValue('DURATION');
  var code = '1 8 ' + duration + '\n';
  return code;
};

Blockly.basicOttoGenerator['block_jump_to_line'] = function(block) {
  var lineNo = block.getFieldValue('LINE_NO');
  var code = '1 9 ' + lineNo + '\n';
  return code;
};

Blockly.basicOttoGenerator['block_set_slowdown'] = function(block) {
  var slowdown = block.getFieldValue('SLOWDOWN');
  var code = '1 10 ' + slowdown + '\n';
  return code;
};

Blockly.basicOttoGenerator['block_play_melody'] = function(block) {
  var melodyNo = block.getFieldValue('MELODY_NO');
  var code = '1 11 ' + melodyNo + '\n';
  return code;
};

Blockly.basicOttoGenerator['block_stop_melody'] = function(block) {
  var code = '1 13 0\n';
  return code;
};

Blockly.basicOttoGenerator['block_play_sound_effect'] = function(block) {
  var effectNo = block.getFieldValue('EFFECT_NO');
  var code = '1 12 ' + effectNo + '\n';
  return code;
};

Blockly.basicOttoGenerator['block_toogle_speaker'] = function(block) {
  var code = '1 15 0\n';
  return code;
};

Blockly.basicOttoGenerator.scrub_ = function(block, code, opt_thisOnly) {
  const nextBlock =
      block.nextConnection && block.nextConnection.targetBlock();
  const nextCode =
      opt_thisOnly ? '' : Blockly.basicOttoGenerator.blockToCode(nextBlock);
  return code +  nextCode;
};

