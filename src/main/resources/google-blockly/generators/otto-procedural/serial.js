
// SERIAL BLOCKS GENERATOR

Blockly.OttoProcedural['serial_println'] = function(block) {
  var message = Blockly.OttoProcedural.valueToCode(block, 'MESSAGE', Blockly.OttoProcedural.ORDER_ATOMIC);
  var code = 'Serial.println(' + message + ');\n';
  return code;
};

Blockly.OttoProcedural['serial_get_string'] = function(block) {
  var code = 'Serial.readStringUntil(10)';
  return [code, Blockly.OttoProcedural.ORDER_NONE];
};

Blockly.OttoProcedural['serial_get_number'] = function(block) {
  var code = 'Serial.parseInt()';
  return [code, Blockly.OttoProcedural.ORDER_NONE];
};
