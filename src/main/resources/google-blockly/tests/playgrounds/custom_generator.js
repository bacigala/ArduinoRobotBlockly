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
          "6"
        ],
        [
          "Right arm",
          "5"
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


var codelabToolbox = `
<xml id="toolbox" style="display: none">
<block type="start_block"></block>
<block type="end_block"></block>
<block type="motor_move"/>
</xml>
`

const basicOttoGenerator = new Blockly.Generator('JSON');
basicOttoGenerator.PRECEDENCE = 0;

basicOttoGenerator['start_block'] = function(block) {
	return "@";
};

basicOttoGenerator['end_block'] = function(block) {
	return "0 0 0";
};

basicOttoGenerator['motor_move'] = function(block) {
	var motorNo = block.getFieldValue('MOTOR_NUMBER');
	var motorPosition = block.getFieldValue('MOTOR_POSITION');
	var code = "1000 " + motorNo + " " + motorPosition + "\n";
	return code;
};

basicOttoGenerator['logic_null'] = function(block) {
  return ['null', basicOttoGenerator.PRECEDENCE];
};

basicOttoGenerator['text'] = function(block) {
  var textValue = block.getFieldValue('TEXT');
  var code = '"' + textValue + '"';
  return [code, basicOttoGenerator.PRECEDENCE];
};

basicOttoGenerator['math_number'] = function(block) {
  const code = Number(block.getFieldValue('NUM'));
  return [code, basicOttoGenerator.PRECEDENCE];
};

basicOttoGenerator['logic_boolean'] = function(block) {
  const code = (block.getFieldValue('BOOL') == 'TRUE') ? 'true' : 'false';
  return [code, basicOttoGenerator.PRECEDENCE];
};

basicOttoGenerator['member'] = function(block) {
  const name = block.getFieldValue('MEMBER_NAME');
  const value = basicOttoGenerator.valueToCode(block, 'MEMBER_VALUE',
      basicOttoGenerator.PRECEDENCE);
  const code = '"' + name + '" : ' + value + ',\n';
  return code;
};

basicOttoGenerator['lists_create_with'] = function(block) {
  const values = [];
  for (var i = 0; i < block.itemCount_; i++) {
    let valueCode = basicOttoGenerator.valueToCode(block, 'ADD' + i,
        basicOttoGenerator.PRECEDENCE);
    if (valueCode) {
      values.push(valueCode);
    }
  }
  const valueString = values.join(',\n');
  const indentedValueString =
      basicOttoGenerator.prefixLines(valueString, basicOttoGenerator.INDENT);
  const codeString = '[\n' + indentedValueString + '\n]';
  return [codeString, basicOttoGenerator.PRECEDENCE];
};

basicOttoGenerator['object'] = function(block) {
  const statement_members =
      basicOttoGenerator.statementToCode(block, 'MEMBERS');
  const code = '{\n' + statement_members + '}';
  return [code, basicOttoGenerator.PRECEDENCE];
};

basicOttoGenerator.scrub_ = function(block, code, opt_thisOnly) {
  const nextBlock =
      block.nextConnection && block.nextConnection.targetBlock();
  const nextCode =
      opt_thisOnly ? '' : basicOttoGenerator.blockToCode(nextBlock);
  return code +  nextCode;
};

