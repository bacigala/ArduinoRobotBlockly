
// CURRENT TIME
Blockly.OttoProcedural['get_time'] = function(block) {
  var code = 'millis()';
  return [code, Blockly.OttoProcedural.ORDER_NONE];
};

// TIMER RESET
Blockly.OttoProcedural['reset_timer'] = function(block) {
  var dropdown_timer_name = block.getFieldValue('TIMER_NAME');
  var code = dropdown_timer_name + ' = millis();\n';
  return code;
};

Blockly.OttoProcedural['get_timer'] = function(block) {
  var dropdown_timer_name = block.getFieldValue('TIMER_NAME');
  var code = 'millis() - ' + dropdown_timer_name;
  return [code, Blockly.OttoProcedural.ORDER_NONE];
};
