
/**
 * This file was created by modification of the original file described below.
 * File was modified to support variable types.
 *
 * ORIGINAL FILE LICENSE AND AUTHOR:
 *
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * @fileoverview Utility functions for handling typed variables.
 *
 * @author duzc2dtw@gmail.com (Du Tian Wei)
 */
'use strict';

goog.provide('Blockly.VariablesDynamic');

goog.require('Blockly.Variables');
goog.require('Blockly.Blocks');
goog.require('Blockly.Msg');
goog.require('Blockly.utils.xml');
goog.require('Blockly.VariableModel');


Blockly.VariablesDynamic.onCreateVariableButtonClick_String = function(button) {
  Blockly.Variables.createVariableButtonHandler(button.getTargetWorkspace(),
      undefined, 'String');
};
Blockly.VariablesDynamic.onCreateVariableButtonClick_Number = function(button) {
  Blockly.Variables.createVariableButtonHandler(button.getTargetWorkspace(),
      undefined, 'Number');
};
Blockly.VariablesDynamic.onCreateVariableButtonClick_Boolean = function(button) {
  Blockly.Variables.createVariableButtonHandler(button.getTargetWorkspace(),
      undefined, 'Boolean');
};
/**
 * Construct the elements (blocks and button) required by the flyout for the
 * variable category.
 * @param {!Blockly.Workspace} workspace The workspace containing variables.
 * @return {!Array.<!Element>} Array of XML elements.
 */
Blockly.VariablesDynamic.flyoutCategory = function(workspace) {
  var xmlList = [];
  
	// Number variables
	var label = document.createElement('label');
	label.setAttribute('text', 'Number variables');
	xmlList.push(label);

	var button = document.createElement('button');
  button.setAttribute('text', 'Create number variable');
  button.setAttribute('callbackKey', 'CREATE_VARIABLE_NUMBER');
  xmlList.push(button);
	workspace.registerButtonCallback('CREATE_VARIABLE_NUMBER',
      Blockly.VariablesDynamic.onCreateVariableButtonClick_Number);
	
  var blockList = Blockly.VariablesDynamic.flyoutCategoryBlocks(workspace, 'Number');
  xmlList = xmlList.concat(blockList);
	
	// Boolean variables
	label = document.createElement('label');
	xmlList.push(label);
	label = document.createElement('label');
	label.setAttribute('text', 'Boolean variables');
	xmlList.push(label);

	var button = document.createElement('button');
  button.setAttribute('text', 'Create boolean variable');
  button.setAttribute('callbackKey', 'CREATE_VARIABLE_BOOLEAN');
  xmlList.push(button);
	workspace.registerButtonCallback('CREATE_VARIABLE_BOOLEAN',
      Blockly.VariablesDynamic.onCreateVariableButtonClick_Boolean);
	
  var blockList = Blockly.VariablesDynamic.flyoutCategoryBlocks(workspace, 'Boolean');
  xmlList = xmlList.concat(blockList);
	
	// String variables
	label = document.createElement('label');
	xmlList.push(label);
	label = document.createElement('label');
	label.setAttribute('text', 'String variables');
	xmlList.push(label);
	
	button = document.createElement('button');
  button.setAttribute('text', 'Create string variable');
  button.setAttribute('callbackKey', 'CREATE_VARIABLE_STRING');
	xmlList.push(button);
	workspace.registerButtonCallback('CREATE_VARIABLE_STRING',
      Blockly.VariablesDynamic.onCreateVariableButtonClick_String);

	var blockList = Blockly.VariablesDynamic.flyoutCategoryBlocks(workspace, 'String');
  xmlList = xmlList.concat(blockList);
	
  return xmlList;
};

/**
 * Construct the blocks required by the flyout for the variable category.
 * @param {!Blockly.Workspace} workspace The workspace containing variables.
 * @return {!Array.<!Element>} Array of XML block elements.
 */
Blockly.VariablesDynamic.flyoutCategoryBlocks = function(workspace, type) {
  var variableModelList = workspace.getVariablesOfType(type);

  var xmlList = [];
  if (variableModelList.length > 0) {
    if (Blockly.Blocks['variables_set_dynamic']) {
      var firstVariable = variableModelList[variableModelList.length - 1];
      var block = Blockly.utils.xml.createElement('block');
      block.setAttribute('type', 'variables_set_dynamic_' + type);
      block.setAttribute('gap', 24);
      block.appendChild(
          Blockly.Variables.generateVariableFieldDom(firstVariable));
      xmlList.push(block);
    }
		
    if (Blockly.Blocks['variables_get_dynamic']) {
      variableModelList.sort(Blockly.VariableModel.compareByName);
      for (var i = 0, variable; (variable = variableModelList[i]); i++) {
				var block = Blockly.utils.xml.createElement('block');
				block.setAttribute('type', 'variables_get_dynamic_' + type);
				block.setAttribute('gap', 8);
				block.appendChild(Blockly.Variables.generateVariableFieldDom(variable));
				xmlList.push(block);
      }
    }
		
  }
  return xmlList;
};
