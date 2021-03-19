/**
 * Modified file, original license:
 *
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 *
 * @fileoverview Generating OttoProcedural for logic blocks.
 * @author q.neutron@gmail.com (Quynh Neutron)
 */
'use strict';

goog.provide('Blockly.OttoProcedural.logic');

goog.require('Blockly.OttoProcedural');


Blockly.OttoProcedural['controls_if'] = function(block) {
  // If/elseif/else condition.
  var n = 0;
  var code = '', branchCode, conditionCode;
  if (Blockly.OttoProcedural.STATEMENT_PREFIX) {
    // Automatic prefix insertion is switched off for this block.  Add manually.
    code += Blockly.OttoProcedural.injectId(Blockly.OttoProcedural.STATEMENT_PREFIX,
        block);
  }
  do {
    conditionCode = Blockly.OttoProcedural.valueToCode(block, 'IF' + n,
        Blockly.OttoProcedural.ORDER_NONE) || 'false';
    branchCode = Blockly.OttoProcedural.statementToCode(block, 'DO' + n);
    if (Blockly.OttoProcedural.STATEMENT_SUFFIX) {
      branchCode = Blockly.OttoProcedural.prefixLines(
          Blockly.OttoProcedural.injectId(Blockly.OttoProcedural.STATEMENT_SUFFIX,
          block), Blockly.OttoProcedural.INDENT) + branchCode;
    }
    code += (n > 0 ? ' else ' : '') +
        'if (' + conditionCode + ') {\n' + branchCode + '}';
    ++n;
  } while (block.getInput('IF' + n));

  if (block.getInput('ELSE') || Blockly.OttoProcedural.STATEMENT_SUFFIX) {
    branchCode = Blockly.OttoProcedural.statementToCode(block, 'ELSE');
    if (Blockly.OttoProcedural.STATEMENT_SUFFIX) {
      branchCode = Blockly.OttoProcedural.prefixLines(
          Blockly.OttoProcedural.injectId(Blockly.OttoProcedural.STATEMENT_SUFFIX,
          block), Blockly.OttoProcedural.INDENT) + branchCode;
    }
    code += ' else {\n' + branchCode + '}';
  }
  return code + '\n';
};

Blockly.OttoProcedural['controls_ifelse'] = Blockly.OttoProcedural['controls_if'];

Blockly.OttoProcedural['logic_compare'] = function(block) {
  // Comparison operator.
  var OPERATORS = {
    'EQ': '==',
    'NEQ': '!=',
    'LT': '<',
    'LTE': '<=',
    'GT': '>',
    'GTE': '>='
  };
  var operator = OPERATORS[block.getFieldValue('OP')];
  var order = (operator == '==' || operator == '!=') ?
      Blockly.OttoProcedural.ORDER_EQUALITY : Blockly.OttoProcedural.ORDER_RELATIONAL;
  var argument0 = Blockly.OttoProcedural.valueToCode(block, 'A', order) || '0';
  var argument1 = Blockly.OttoProcedural.valueToCode(block, 'B', order) || '0';
  var code = argument0 + ' ' + operator + ' ' + argument1;
  return [code, order];
};

Blockly.OttoProcedural['logic_operation'] = function(block) {
  // Operations 'and', 'or'.
  var operator = (block.getFieldValue('OP') == 'AND') ? '&&' : '||';
  var order = (operator == '&&') ? Blockly.OttoProcedural.ORDER_LOGICAL_AND :
      Blockly.OttoProcedural.ORDER_LOGICAL_OR;
  var argument0 = Blockly.OttoProcedural.valueToCode(block, 'A', order);
  var argument1 = Blockly.OttoProcedural.valueToCode(block, 'B', order);
  if (!argument0 && !argument1) {
    // If there are no arguments, then the return value is false.
    argument0 = 'false';
    argument1 = 'false';
  } else {
    // Single missing arguments have no effect on the return value.
    var defaultArgument = (operator == '&&') ? 'true' : 'false';
    if (!argument0) {
      argument0 = defaultArgument;
    }
    if (!argument1) {
      argument1 = defaultArgument;
    }
  }
  var code = argument0 + ' ' + operator + ' ' + argument1;
  return [code, order];
};

Blockly.OttoProcedural['logic_negate'] = function(block) {
  // Negation.
  var order = Blockly.OttoProcedural.ORDER_LOGICAL_NOT;
  var argument0 = Blockly.OttoProcedural.valueToCode(block, 'BOOL', order) ||
      'true';
  var code = '!' + argument0;
  return [code, order];
};

Blockly.OttoProcedural['logic_boolean'] = function(block) {
  // Boolean values true and false.
  var code = (block.getFieldValue('BOOL') == 'TRUE') ? 'true' : 'false';
  return [code, Blockly.OttoProcedural.ORDER_ATOMIC];
};

Blockly.OttoProcedural['logic_ternary'] = function(block) {
  // Ternary operator.
  var value_if = Blockly.OttoProcedural.valueToCode(block, 'IF',
      Blockly.OttoProcedural.ORDER_CONDITIONAL) || 'false';
  var value_then = Blockly.OttoProcedural.valueToCode(block, 'THEN',
      Blockly.OttoProcedural.ORDER_CONDITIONAL) || 'null';
  var value_else = Blockly.OttoProcedural.valueToCode(block, 'ELSE',
      Blockly.OttoProcedural.ORDER_CONDITIONAL) || 'null';
  var code = value_if + ' ? ' + value_then + ' : ' + value_else;
  return [code, Blockly.OttoProcedural.ORDER_CONDITIONAL];
};
