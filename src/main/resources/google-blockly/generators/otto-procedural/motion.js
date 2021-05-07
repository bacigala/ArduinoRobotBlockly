
// MOTION BLOCKS GENERATOR

Blockly.OttoProcedural['otto_procedural_motor_move'] = function(block) {
	let motorNo = block.getFieldValue('MOTOR_NUMBER');
	let motorPosition = Blockly.OttoProcedural.valueToCode(
		block, 'MOTOR_POSITION', Blockly.OttoProcedural.ORDER_ATOMIC);
	return 'motor_set(' + motorNo + ', ' + motorPosition + ");\n";
};

Blockly.OttoProcedural['reset_motors'] = function(block) {
  return 'reset_motors();\n';
};

Blockly.OttoProcedural['move_complex'] = function(block) {
  let dropdown_direction = block.getFieldValue('DIRECTION');
  switch (dropdown_direction) {
		case "FWD":
			return 'go_forward();\n';
		case "BWD":
			return 'go_backward();\n';
	}
};

Blockly.OttoProcedural['turn_complex'] = function(block) {
  let dropdown_direction = block.getFieldValue('DIRECTION');
  switch (dropdown_direction) {
		case "LEFT":
			return 'turn_left();\n';
		case "RIGHT":
			return 'turn_right();\n';
	}
};

Blockly.OttoProcedural['set_slowdown'] = function(block) {
	let level = Blockly.OttoProcedural.valueToCode(block, 'SLOWDOWN_LEVEL', Blockly.OttoProcedural.ORDER_ATOMIC);
	return 'slowdown = ' + level + ';\n';
};

Blockly.OttoProcedural['tiptoes'] = function(block) {
	return 'stand_on_tiptoes();\n';
};

Blockly.OttoProcedural['heels'] = function(block) {
	return 'stand_on_heels();\n';
};

Blockly.OttoProcedural['wave_hand'] = function(block) {
	return 'wave_hand();\n';
};

Blockly.OttoProcedural['wave_hand2'] = function(block) {
	return 'wave_hand2();\n';
};
