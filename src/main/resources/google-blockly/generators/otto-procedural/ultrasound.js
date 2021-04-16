
// ULTRASOUND GENERATOR

Blockly.OttoProcedural['otto_procedural_ultrasound_get_distance'] = function(block) {
	return ['measure_distance()', Blockly.OttoProcedural.ORDER_NONE];
};

Blockly.OttoProcedural['wait_ultrasound'] = function(block) {
  var dropdown_relation = block.getFieldValue('RELATION');
  var value_distance = Blockly.OttoProcedural.valueToCode(block, 'DISTANCE', Blockly.OttoProcedural.ORDER_ATOMIC);
  var code = 'while (measure_distance() ';
	if (dropdown_relation == 'LESS') code += '>';
	if (dropdown_relation == 'EQUAL') code += '!=';
	if (dropdown_relation == 'MORE') code += '<';
	code += value_distance;
	code += ') {\n';
	code += 'delay(300);\n';
	code += 'check_battery();\n';
	code += '}\n';
  return code;
};

// TOUCH

Blockly.OttoProcedural['button_pressed'] = function(block) {
  var dropdown_button = block.getFieldValue('BUTTON');
  if (dropdown_button == 'RIGHT') code = 'digitalRead(TOUCH1)';
  if (dropdown_button == 'LEFT') code = 'digitalRead(TOUCH2)';
  return [code, Blockly.OttoProcedural.ORDER_NONE];
};

Blockly.OttoProcedural['wait_touch'] = function(block) {
  var dropdown_button = block.getFieldValue('BUTTON');
	var code = 'while (! ';
	if (dropdown_button == 'RIGHT') code += 'digitalRead(TOUCH1)';
	if (dropdown_button == 'LEFT') code += 'digitalRead(TOUCH2)';
	code += ') {\n';
	code += 'delay(300);\n';
	code += 'check_battery();\n';
	code += '}\n';
  return code;
};

Blockly.OttoProcedural['ulrasonic_gesture_record'] = function(block) {
  var code = 'ultrasonic_gesture_record(50, 2000, 3000);\n';
  return code;
};

Blockly.OttoProcedural['ulrasonic_gesture_last'] = function(block) {
  var code = 'US_last_seen_gesture';
  return [code, Blockly.OttoProcedural.ORDER_NONE];
};


