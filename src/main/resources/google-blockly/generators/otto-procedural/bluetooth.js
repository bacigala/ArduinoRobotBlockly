
// BLUETOOTH BLOCKS GENERATORS

Blockly.OttoProcedural['bluetooth_print_str'] = function(block) {
  let message = Blockly.OttoProcedural.valueToCode(block, 'MESSAGE', Blockly.OttoProcedural.ORDER_ATOMIC);
  return 'bluetooth_print_str(' + message + ');\n';
};

Blockly.OttoProcedural['bluetooth_print_num'] = function(block) {
  let message = Blockly.OttoProcedural.valueToCode(block, 'MESSAGE', Blockly.OttoProcedural.ORDER_ATOMIC);
  return 'bluetooth_print_num(' + message + ');\n';
};

Blockly.OttoProcedural['bluetooth_get_string'] = function() {
  let code = 'bluetooth_read_str()';
  return [code, Blockly.OttoProcedural.ORDER_ATOMIC];
};

Blockly.OttoProcedural['bluetooth_get_number'] = function() {
  let code = 'bluetooth_read_num()';
  return [code, Blockly.OttoProcedural.ORDER_ATOMIC];
};
