
// SERIAL BLOCKS GENERATOR

Blockly.OttoProcedural['serial_println'] = function(block) {
  let message = Blockly.OttoProcedural.valueToCode(block, 'MESSAGE', Blockly.OttoProcedural.ORDER_ATOMIC);
  return 'Serial.println(' + message + ');\n';
};

Blockly.OttoProcedural['serial_get_string'] = function() {
  let code = 'serial_read_str()';
  return [code, Blockly.OttoProcedural.ORDER_ATOMIC];
};

Blockly.OttoProcedural['serial_get_number'] = function() {
  let code = 'serial_read_num()';
  return [code, Blockly.OttoProcedural.ORDER_ATOMIC];
};
