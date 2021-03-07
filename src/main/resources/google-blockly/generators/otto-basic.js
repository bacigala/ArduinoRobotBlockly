
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

