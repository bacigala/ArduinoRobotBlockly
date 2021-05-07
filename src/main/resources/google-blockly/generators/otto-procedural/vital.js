
// OTTO ARDUINO - RELATED BLOCKS GENERATORS

Blockly.OttoProcedural['get_battery_level'] = function(block) {
  let code = 'measure_battery()';
  return [code, Blockly.OttoProcedural.ORDER_NONE]
};
