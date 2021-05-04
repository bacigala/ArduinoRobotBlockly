/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * @fileoverview Generating OttoProcedural for dynamic variable blocks.
 * @author fenichel@google.com (Rachel Fenichel)
 */
'use strict';

goog.provide('Blockly.OttoProcedural.variablesDynamic');

goog.require('Blockly.OttoProcedural');
goog.require('Blockly.OttoProcedural.variables');


// VARIABLE GETTER BLOCK
Blockly.OttoProcedural['variables_get_dynamic'] = function(block) {
  // Variable getter.
  var code = Blockly.OttoProcedural.variableDB_.getName(block.getFieldValue('VAR'),
      Blockly.VARIABLE_CATEGORY_NAME);
  return [code, Blockly.OttoProcedural.ORDER_ATOMIC];
};

Blockly.OttoProcedural['variables_get_dynamic_String'] = Blockly.OttoProcedural['variables_get_dynamic'];
Blockly.OttoProcedural['variables_get_dynamic_Number'] = Blockly.OttoProcedural['variables_get_dynamic'];
Blockly.OttoProcedural['variables_get_dynamic_Boolean'] = Blockly.OttoProcedural['variables_get_dynamic'];


// VARIABLE SETTER BLOCK
function create_variables_set_dynamic_function(defaultValue) {
	return function(block) {
			// Variable setter.
			var argument0 = Blockly.OttoProcedural.valueToCode(block, 'VALUE',
					Blockly.OttoProcedural.ORDER_ASSIGNMENT) || defaultValue;
			var varName = Blockly.OttoProcedural.variableDB_.getName(
					block.getFieldValue('VAR'), Blockly.VARIABLE_CATEGORY_NAME);
			return varName + ' = ' + argument0 + ';\n';
		}
}

Blockly.OttoProcedural['variables_set_dynamic'] = create_variables_set_dynamic_function('0');

// different type setters require different default value (if no value block is connected)
Blockly.OttoProcedural['variables_set_dynamic_String'] = create_variables_set_dynamic_function('""');
Blockly.OttoProcedural['variables_set_dynamic_Number'] = create_variables_set_dynamic_function('0');
Blockly.OttoProcedural['variables_set_dynamic_Boolean'] = create_variables_set_dynamic_function('false');