
// MOTION BLOCKS GENERATOR

Blockly.OttoProcedural['otto_procedural_motor_move'] = function(block) {
	var motorNo = block.getFieldValue('MOTOR_NUMBER');
	var motorPosition = block.getFieldValue('MOTOR_POSITION');
	var code = 'nastav_koncatinu(' + motorNo + ', ' + motorPosition + ");\n";
	return code;
};

Blockly.OttoProcedural['reset_motors'] = function(block) {
  return 'reset_motors();\n';
};
