Blockly.Dart['myblock'] = function(block) {
  var dropdown_name = block.getFieldValue('NAME');
  var value_name = Blockly.Dart.valueToCode(block, 'NAME', Blockly.Dart.ORDER_ATOMIC);
  // TODO: Assemble Dart into code variable.
  var code = '...;\n';
  return code;
};