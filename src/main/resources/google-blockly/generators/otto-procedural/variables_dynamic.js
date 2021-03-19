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


// OttoProcedural is dynamically typed.
Blockly.OttoProcedural['variables_get_dynamic'] =
    Blockly.OttoProcedural['variables_get'];
Blockly.OttoProcedural['variables_set_dynamic'] =
    Blockly.OttoProcedural['variables_set'];
