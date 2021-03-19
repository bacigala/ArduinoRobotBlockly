/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * @fileoverview Generating OttoProcedural for procedure blocks.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

goog.provide('Blockly.OttoProcedural.procedures');

goog.require('Blockly.OttoProcedural');


Blockly.OttoProcedural['procedures_defreturn'] = function(block) {
  // Define a procedure with a return value.
  var funcName = Blockly.OttoProcedural.variableDB_.getName(
      block.getFieldValue('NAME'), Blockly.PROCEDURE_CATEGORY_NAME);
  var xfix1 = '';
  if (Blockly.OttoProcedural.STATEMENT_PREFIX) {
    xfix1 += Blockly.OttoProcedural.injectId(Blockly.OttoProcedural.STATEMENT_PREFIX,
        block);
  }
  if (Blockly.OttoProcedural.STATEMENT_SUFFIX) {
    xfix1 += Blockly.OttoProcedural.injectId(Blockly.OttoProcedural.STATEMENT_SUFFIX,
        block);
  }
  if (xfix1) {
    xfix1 = Blockly.OttoProcedural.prefixLines(xfix1, Blockly.OttoProcedural.INDENT);
  }
  var loopTrap = '';
  if (Blockly.OttoProcedural.INFINITE_LOOP_TRAP) {
    loopTrap = Blockly.OttoProcedural.prefixLines(
        Blockly.OttoProcedural.injectId(Blockly.OttoProcedural.INFINITE_LOOP_TRAP,
        block), Blockly.OttoProcedural.INDENT);
  }
  var branch = Blockly.OttoProcedural.statementToCode(block, 'STACK');
  var returnValue = Blockly.OttoProcedural.valueToCode(block, 'RETURN',
      Blockly.OttoProcedural.ORDER_NONE) || '';
  var xfix2 = '';
  if (branch && returnValue) {
    // After executing the function body, revisit this block for the return.
    xfix2 = xfix1;
  }
  if (returnValue) {
    returnValue = Blockly.OttoProcedural.INDENT + 'return ' + returnValue + ';\n';
  }
  var args = [];
  var variables = block.getVars();
  for (var i = 0; i < variables.length; i++) {
    args[i] = Blockly.OttoProcedural.variableDB_.getName(variables[i],
        Blockly.VARIABLE_CATEGORY_NAME);
  }
	
	if (args.length > 0) args = 'int16_t ' + args.join(', int16_t ');
	
  var code = 'int16_t ' + funcName + '(' + args + ') {\n' +
      xfix1 + loopTrap + branch + xfix2 + returnValue + '}';
  code = Blockly.OttoProcedural.scrub_(block, code);
  // Add % so as not to collide with helper functions in definitions list.
  Blockly.OttoProcedural.definitions_['%' + funcName] = code;
  return null;
};

// Defining a procedure without a return value uses the same generator as
// a procedure with a return value.
Blockly.OttoProcedural['procedures_defnoreturn'] = function(block) {
  // Define a procedure with a return value.
  var funcName = Blockly.OttoProcedural.variableDB_.getName(
      block.getFieldValue('NAME'), Blockly.PROCEDURE_CATEGORY_NAME);
  var xfix1 = '';
  if (Blockly.OttoProcedural.STATEMENT_PREFIX) {
    xfix1 += Blockly.OttoProcedural.injectId(Blockly.OttoProcedural.STATEMENT_PREFIX,
        block);
  }
  if (Blockly.OttoProcedural.STATEMENT_SUFFIX) {
    xfix1 += Blockly.OttoProcedural.injectId(Blockly.OttoProcedural.STATEMENT_SUFFIX,
        block);
  }
  if (xfix1) {
    xfix1 = Blockly.OttoProcedural.prefixLines(xfix1, Blockly.OttoProcedural.INDENT);
  }
  var loopTrap = '';
  if (Blockly.OttoProcedural.INFINITE_LOOP_TRAP) {
    loopTrap = Blockly.OttoProcedural.prefixLines(
        Blockly.OttoProcedural.injectId(Blockly.OttoProcedural.INFINITE_LOOP_TRAP,
        block), Blockly.OttoProcedural.INDENT);
  }
  var branch = Blockly.OttoProcedural.statementToCode(block, 'STACK');
  var returnValue = Blockly.OttoProcedural.valueToCode(block, 'RETURN',
      Blockly.OttoProcedural.ORDER_NONE) || '';
  var xfix2 = '';
  if (branch && returnValue) {
    // After executing the function body, revisit this block for the return.
    xfix2 = xfix1;
  }
  if (returnValue) {
    returnValue = Blockly.OttoProcedural.INDENT + 'return ' + returnValue + ';\n';
  }
  var args = [];
  var variables = block.getVars();
  for (var i = 0; i < variables.length; i++) {
    args[i] = Blockly.OttoProcedural.variableDB_.getName(variables[i],
        Blockly.VARIABLE_CATEGORY_NAME);
  }
	
	// CREATE ARGUMENT LIST
	
	if (args.length > 0) args = 'int16_t ' + args.join(', int16_t ');
	
	
  var code = 'void ' + funcName + '(' + args + ') {\n' +
      xfix1 + loopTrap + branch + xfix2 + returnValue + '}';
  code = Blockly.OttoProcedural.scrub_(block, code);
  // Add % so as not to collide with helper functions in definitions list.
  Blockly.OttoProcedural.definitions_['%' + funcName] = code;
  return null;
};

Blockly.OttoProcedural['procedures_callreturn'] = function(block) {
  // Call a procedure with a return value.
  var funcName = Blockly.OttoProcedural.variableDB_.getName(
      block.getFieldValue('NAME'), Blockly.PROCEDURE_CATEGORY_NAME);
  var args = [];
  var variables = block.getVars();
  for (var i = 0; i < variables.length; i++) {
    args[i] = Blockly.OttoProcedural.valueToCode(block, 'ARG' + i,
        Blockly.OttoProcedural.ORDER_COMMA) || 'null';
  }
  var code = funcName + '(' + args.join(', ') + ')';
  return [code, Blockly.OttoProcedural.ORDER_FUNCTION_CALL];
};

Blockly.OttoProcedural['procedures_callnoreturn'] = function(block) {
  // Call a procedure with no return value.
  // Generated code is for a function call as a statement is the same as a
  // function call as a value, with the addition of line ending.
  var tuple = Blockly.OttoProcedural['procedures_callreturn'](block);
  return tuple[0] + ';\n';
};

Blockly.OttoProcedural['procedures_ifreturn'] = function(block) {
  // Conditionally return value from a procedure.
  var condition = Blockly.OttoProcedural.valueToCode(block, 'CONDITION',
      Blockly.OttoProcedural.ORDER_NONE) || 'false';
  var code = 'if (' + condition + ') {\n';
  if (Blockly.OttoProcedural.STATEMENT_SUFFIX) {
    // Inject any statement suffix here since the regular one at the end
    // will not get executed if the return is triggered.
    code += Blockly.OttoProcedural.prefixLines(
        Blockly.OttoProcedural.injectId(Blockly.OttoProcedural.STATEMENT_SUFFIX, block),
        Blockly.OttoProcedural.INDENT);
  }
  if (block.hasReturnValue_) {
    var value = Blockly.OttoProcedural.valueToCode(block, 'VALUE',
        Blockly.OttoProcedural.ORDER_NONE) || 'null';
    code += Blockly.OttoProcedural.INDENT + 'return ' + value + ';\n';
  } else {
    code += Blockly.OttoProcedural.INDENT + 'return;\n';
  }
  code += '}\n';
  return code;
};
