
// MOTION BLOCKS GENERATOR

Blockly.OttoProcedural['otto_procedural_motor_move'] = function(block) {
	var motorNo = block.getFieldValue('MOTOR_NUMBER');
	var motorPosition = Blockly.OttoProcedural.valueToCode(block, 'MOTOR_POSITION', Blockly.OttoProcedural.ORDER_ATOMIC);
	var code = 'nastav_koncatinu(' + motorNo + ', ' + motorPosition + ");\n";
	return code;
};

Blockly.OttoProcedural['reset_motors'] = function(block) {
  return 'reset_motors();\n';
};

Blockly.OttoProcedural['move_complex'] = function(block) {
  var dropdown_direction = block.getFieldValue('DIRECTION');
  switch (dropdown_direction) {
		case "FWD":
			return 'chod_smerom(1);\n';
		case "BWD":
			return 'chod_smerom(2);\n';
	}
};

Blockly.OttoProcedural['turn_complex'] = function(block) {
  var dropdown_direction = block.getFieldValue('DIRECTION');
  switch (dropdown_direction) {
		case "LEFT":
			return 'chod_smerom(4);\n';
		case "RIGHT":
			return 'chod_smerom(3);\n';
	}
};

Blockly.OttoProcedural['tiptoes'] = function(block) {
  var code = 'to_tiptoes();\n';
  return code;
};

Blockly.OttoProcedural['heels'] = function(block) {
  var code = 'to_heels();\n';
  return code;
};

Blockly.OttoProcedural['wave_hand'] = function(block) {
  var code = 'wave_hand();\n';
  return code;
};

