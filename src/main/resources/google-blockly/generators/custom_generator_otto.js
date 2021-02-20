Blockly.defineBlocksWithJsonArray([{
  "type": "motor_move",
  "message0": "Move %1 to position %2 %3",
  "args0": [
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
	var motorNo = block.getFieldValue('MOTOR_NUMBER');
	var motorPosition = block.getFieldValue('MOTOR_POSITION');
	var code = "1000 " + motorNo + " " + motorPosition + "\n";
	return code;
};

Blockly.basicOttoGenerator.scrub_ = function(block, code, opt_thisOnly) {
  const nextBlock =
      block.nextConnection && block.nextConnection.targetBlock();
  const nextCode =
      opt_thisOnly ? '' : Blockly.basicOttoGenerator.blockToCode(nextBlock);
  return code +  nextCode;
};

