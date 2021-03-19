/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * @fileoverview Generating OttoProcedural for variable blocks.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

goog.provide('Blockly.OttoProcedural.variables');

goog.require('Blockly.OttoProcedural');


Blockly.OttoProcedural['variables_get'] = function(block) {
  // Variable getter.
  var code = Blockly.OttoProcedural.variableDB_.getName(block.getFieldValue('VAR'),
      Blockly.VARIABLE_CATEGORY_NAME);
  return [code, Blockly.OttoProcedural.ORDER_ATOMIC];
};

Blockly.OttoProcedural['variables_set'] = function(block) {
  // Variable setter.
  var argument0 = Blockly.OttoProcedural.valueToCode(block, 'VALUE',
      Blockly.OttoProcedural.ORDER_ASSIGNMENT) || '0';
  var varName = Blockly.OttoProcedural.variableDB_.getName(
      block.getFieldValue('VAR'), Blockly.VARIABLE_CATEGORY_NAME);
  return varName + ' = ' + argument0 + ';\n';
};
