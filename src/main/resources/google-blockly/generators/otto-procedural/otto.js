Blockly.OttoProcedural['otto_basic_loop'] = function(block) {
  var program = Blockly.OttoProcedural.statementToCode(block, 'PROGRAM');
  return program;
};

Blockly.OttoProcedural['comment_block'] = function(block) {
  var text_comment = block.getFieldValue('COMMENT');
  var code =  '//' + text_comment + '\n';
  return code;
};
