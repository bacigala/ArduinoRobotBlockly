/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * @fileoverview Helper functions for generating MyGen for blocks.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

goog.provide('Blockly.MyGen');

goog.require('Blockly.Generator');
goog.require('Blockly.utils.global');
goog.require('Blockly.utils.string');


/**
 * MyGen code generator.
 * @type {!Blockly.Generator}
 */
Blockly.MyGen = new Blockly.Generator('MyGen');

/**
 * List of illegal variable names.
 * This is not intended to be a security feature.  Blockly is 100% client-side,
 * so bypassing this list is trivial.  This is intended to prevent users from
 * accidentally clobbering a built-in object or function.
 * @private
 */
Blockly.MyGen.addReservedWords(
    // https://developer.mozilla.org/en-US/docs/Web/MyGen/Reference/Lexical_grammar#Keywords
    'break,case,catch,class,const,continue,debugger,default,delete,do,else,export,extends,finally,for,function,if,import,in,instanceof,new,return,super,switch,this,throw,try,typeof,var,void,while,with,yield,' +
    'enum,' +
    'implements,interface,let,package,private,protected,public,static,' +
    'await,' +
    'null,true,false,' +
    // Magic variable.
    'arguments,' +
    // Everything in the current environment (835 items in Chrome, 104 in Node).
    Object.getOwnPropertyNames(Blockly.utils.global).join(','));

/**
 * Order of operation ENUMs.
 * https://developer.mozilla.org/en/MyGen/Reference/Operators/Operator_Precedence
 */
Blockly.MyGen.ORDER_ATOMIC = 0;           // 0 "" ...
Blockly.MyGen.ORDER_NEW = 1.1;            // new
Blockly.MyGen.ORDER_MEMBER = 1.2;         // . []
Blockly.MyGen.ORDER_FUNCTION_CALL = 2;    // ()
Blockly.MyGen.ORDER_INCREMENT = 3;        // ++
Blockly.MyGen.ORDER_DECREMENT = 3;        // --
Blockly.MyGen.ORDER_BITWISE_NOT = 4.1;    // ~
Blockly.MyGen.ORDER_UNARY_PLUS = 4.2;     // +
Blockly.MyGen.ORDER_UNARY_NEGATION = 4.3; // -
Blockly.MyGen.ORDER_LOGICAL_NOT = 4.4;    // !
Blockly.MyGen.ORDER_TYPEOF = 4.5;         // typeof
Blockly.MyGen.ORDER_VOID = 4.6;           // void
Blockly.MyGen.ORDER_DELETE = 4.7;         // delete
Blockly.MyGen.ORDER_AWAIT = 4.8;          // await
Blockly.MyGen.ORDER_EXPONENTIATION = 5.0; // **
Blockly.MyGen.ORDER_MULTIPLICATION = 5.1; // *
Blockly.MyGen.ORDER_DIVISION = 5.2;       // /
Blockly.MyGen.ORDER_MODULUS = 5.3;        // %
Blockly.MyGen.ORDER_SUBTRACTION = 6.1;    // -
Blockly.MyGen.ORDER_ADDITION = 6.2;       // +
Blockly.MyGen.ORDER_BITWISE_SHIFT = 7;    // << >> >>>
Blockly.MyGen.ORDER_RELATIONAL = 8;       // < <= > >=
Blockly.MyGen.ORDER_IN = 8;               // in
Blockly.MyGen.ORDER_INSTANCEOF = 8;       // instanceof
Blockly.MyGen.ORDER_EQUALITY = 9;         // == != === !==
Blockly.MyGen.ORDER_BITWISE_AND = 10;     // &
Blockly.MyGen.ORDER_BITWISE_XOR = 11;     // ^
Blockly.MyGen.ORDER_BITWISE_OR = 12;      // |
Blockly.MyGen.ORDER_LOGICAL_AND = 13;     // &&
Blockly.MyGen.ORDER_LOGICAL_OR = 14;      // ||
Blockly.MyGen.ORDER_CONDITIONAL = 15;     // ?:
Blockly.MyGen.ORDER_ASSIGNMENT = 16;      // = += -= **= *= /= %= <<= >>= ...
Blockly.MyGen.ORDER_YIELD = 17;           // yield
Blockly.MyGen.ORDER_COMMA = 18;           // ,
Blockly.MyGen.ORDER_NONE = 99;            // (...)

/**
 * List of outer-inner pairings that do NOT require parentheses.
 * @type {!Array.<!Array.<number>>}
 */
Blockly.MyGen.ORDER_OVERRIDES = [
  // (foo()).bar -> foo().bar
  // (foo())[0] -> foo()[0]
  [Blockly.MyGen.ORDER_FUNCTION_CALL, Blockly.MyGen.ORDER_MEMBER],
  // (foo())() -> foo()()
  [Blockly.MyGen.ORDER_FUNCTION_CALL, Blockly.MyGen.ORDER_FUNCTION_CALL],
  // (foo.bar).baz -> foo.bar.baz
  // (foo.bar)[0] -> foo.bar[0]
  // (foo[0]).bar -> foo[0].bar
  // (foo[0])[1] -> foo[0][1]
  [Blockly.MyGen.ORDER_MEMBER, Blockly.MyGen.ORDER_MEMBER],
  // (foo.bar)() -> foo.bar()
  // (foo[0])() -> foo[0]()
  [Blockly.MyGen.ORDER_MEMBER, Blockly.MyGen.ORDER_FUNCTION_CALL],

  // !(!foo) -> !!foo
  [Blockly.MyGen.ORDER_LOGICAL_NOT, Blockly.MyGen.ORDER_LOGICAL_NOT],
  // a * (b * c) -> a * b * c
  [Blockly.MyGen.ORDER_MULTIPLICATION, Blockly.MyGen.ORDER_MULTIPLICATION],
  // a + (b + c) -> a + b + c
  [Blockly.MyGen.ORDER_ADDITION, Blockly.MyGen.ORDER_ADDITION],
  // a && (b && c) -> a && b && c
  [Blockly.MyGen.ORDER_LOGICAL_AND, Blockly.MyGen.ORDER_LOGICAL_AND],
  // a || (b || c) -> a || b || c
  [Blockly.MyGen.ORDER_LOGICAL_OR, Blockly.MyGen.ORDER_LOGICAL_OR]
];

/**
 * Initialise the database of variable names.
 * @param {!Blockly.Workspace} workspace Workspace to generate code from.
 */
Blockly.MyGen.init = function(workspace) {
  // Create a dictionary of definitions to be printed before the code.
  Blockly.MyGen.definitions_ = Object.create(null);
  // Create a dictionary mapping desired function names in definitions_
  // to actual function names (to avoid collisions with user functions).
  Blockly.MyGen.functionNames_ = Object.create(null);

  if (!Blockly.MyGen.variableDB_) {
    Blockly.MyGen.variableDB_ =
        new Blockly.Names(Blockly.MyGen.RESERVED_WORDS_);
  } else {
    Blockly.MyGen.variableDB_.reset();
  }

  Blockly.MyGen.variableDB_.setVariableMap(workspace.getVariableMap());

  var defvars = [];
  // Add developer variables (not created or named by the user).
  var devVarList = Blockly.Variables.allDeveloperVariables(workspace);
  for (var i = 0; i < devVarList.length; i++) {
    defvars.push(Blockly.MyGen.variableDB_.getName(devVarList[i],
        Blockly.Names.DEVELOPER_VARIABLE_TYPE));
  }

  // Add user variables, but only ones that are being used.
  var variables = Blockly.Variables.allUsedVarModels(workspace);
  for (var i = 0; i < variables.length; i++) {
    defvars.push(Blockly.MyGen.variableDB_.getName(variables[i].getId(),
        Blockly.VARIABLE_CATEGORY_NAME));
  }

  // Declare all of the variables.
  if (defvars.length) {
    Blockly.MyGen.definitions_['variables'] =
        'var ' + defvars.join(', ') + ';';
  }
};

/**
 * Prepend the generated code with the variable definitions.
 * @param {string} code Generated code.
 * @return {string} Completed code.
 */
Blockly.MyGen.finish = function(code) {
  // Convert the definitions dictionary into a list.
  var definitions = [];
  for (var name in Blockly.MyGen.definitions_) {
    definitions.push(Blockly.MyGen.definitions_[name]);
  }
  // Clean up temporary data.
  delete Blockly.MyGen.definitions_;
  delete Blockly.MyGen.functionNames_;
  Blockly.MyGen.variableDB_.reset();
  return definitions.join('\n\n') + '\n\n\n' + code;
};

/**
 * Naked values are top-level blocks with outputs that aren't plugged into
 * anything.  A trailing semicolon is needed to make this legal.
 * @param {string} line Line of generated code.
 * @return {string} Legal line of code.
 */
Blockly.MyGen.scrubNakedValue = function(line) {
  return line + ';\n';
};

/**
 * Encode a string as a properly escaped MyGen string, complete with
 * quotes.
 * @param {string} string Text to encode.
 * @return {string} MyGen string.
 * @private
 */
Blockly.MyGen.quote_ = function(string) {
  // Can't use goog.string.quote since Google's style guide recommends
  // JS string literals use single quotes.
  string = string.replace(/\\/g, '\\\\')
                 .replace(/\n/g, '\\\n')
                 .replace(/'/g, '\\\'');
  return '\'' + string + '\'';
};

/**
 * Encode a string as a properly escaped multiline MyGen string, complete
 * with quotes.
 * @param {string} string Text to encode.
 * @return {string} MyGen string.
 * @private
 */
Blockly.MyGen.multiline_quote_ = function(string) {
  // Can't use goog.string.quote since Google's style guide recommends
  // JS string literals use single quotes.
  var lines = string.split(/\n/g).map(Blockly.MyGen.quote_);
  return lines.join(' + \'\\n\' +\n');
};

/**
 * Common tasks for generating MyGen from blocks.
 * Handles comments for the specified block and any connected value blocks.
 * Calls any statements following this block.
 * @param {!Blockly.Block} block The current block.
 * @param {string} code The MyGen code created for this block.
 * @param {boolean=} opt_thisOnly True to generate code for only this statement.
 * @return {string} MyGen code with comments and subsequent blocks added.
 * @private
 */
Blockly.MyGen.scrub_ = function(block, code, opt_thisOnly) {
  var commentCode = '';
  // Only collect comments for blocks that aren't inline.
  if (!block.outputConnection || !block.outputConnection.targetConnection) {
    // Collect comment for this block.
    var comment = block.getCommentText();
    if (comment) {
      comment = Blockly.utils.string.wrap(comment,
          Blockly.MyGen.COMMENT_WRAP - 3);
      commentCode += Blockly.MyGen.prefixLines(comment + '\n', '// ');
    }
    // Collect comments for all value arguments.
    // Don't collect comments for nested statements.
    for (var i = 0; i < block.inputList.length; i++) {
      if (block.inputList[i].type == Blockly.INPUT_VALUE) {
        var childBlock = block.inputList[i].connection.targetBlock();
        if (childBlock) {
          comment = Blockly.MyGen.allNestedComments(childBlock);
          if (comment) {
            commentCode += Blockly.MyGen.prefixLines(comment, '// ');
          }
        }
      }
    }
  }
  var nextBlock = block.nextConnection && block.nextConnection.targetBlock();
  var nextCode = opt_thisOnly ? '' : Blockly.MyGen.blockToCode(nextBlock);
  return commentCode + code + nextCode;
};

/**
 * Gets a property and adjusts the value while taking into account indexing.
 * @param {!Blockly.Block} block The block.
 * @param {string} atId The property ID of the element to get.
 * @param {number=} opt_delta Value to add.
 * @param {boolean=} opt_negate Whether to negate the value.
 * @param {number=} opt_order The highest order acting on this value.
 * @return {string|number}
 */
Blockly.MyGen.getAdjusted = function(block, atId, opt_delta, opt_negate,
    opt_order) {
  var delta = opt_delta || 0;
  var order = opt_order || Blockly.MyGen.ORDER_NONE;
  if (block.workspace.options.oneBasedIndex) {
    delta--;
  }
  var defaultAtIndex = block.workspace.options.oneBasedIndex ? '1' : '0';
  if (delta > 0) {
    var at = Blockly.MyGen.valueToCode(block, atId,
        Blockly.MyGen.ORDER_ADDITION) || defaultAtIndex;
  } else if (delta < 0) {
    var at = Blockly.MyGen.valueToCode(block, atId,
        Blockly.MyGen.ORDER_SUBTRACTION) || defaultAtIndex;
  } else if (opt_negate) {
    var at = Blockly.MyGen.valueToCode(block, atId,
        Blockly.MyGen.ORDER_UNARY_NEGATION) || defaultAtIndex;
  } else {
    var at = Blockly.MyGen.valueToCode(block, atId, order) ||
        defaultAtIndex;
  }

  if (Blockly.isNumber(at)) {
    // If the index is a naked number, adjust it right now.
    at = Number(at) + delta;
    if (opt_negate) {
      at = -at;
    }
  } else {
    // If the index is dynamic, adjust it in code.
    if (delta > 0) {
      at = at + ' + ' + delta;
      var innerOrder = Blockly.MyGen.ORDER_ADDITION;
    } else if (delta < 0) {
      at = at + ' - ' + -delta;
      var innerOrder = Blockly.MyGen.ORDER_SUBTRACTION;
    }
    if (opt_negate) {
      if (delta) {
        at = '-(' + at + ')';
      } else {
        at = '-' + at;
      }
      var innerOrder = Blockly.MyGen.ORDER_UNARY_NEGATION;
    }
    innerOrder = Math.floor(innerOrder);
    order = Math.floor(order);
    if (innerOrder && order >= innerOrder) {
      at = '(' + at + ')';
    }
  }
  return at;
};

Blockly.MyGen['text_indexOf'] = function(block) {
  // Search the text for a substring.
  var operator = block.getFieldValue('END') == 'FIRST' ? 'indexOf' : 'lastIndexOf';
  var subString = Blockly.JavaScript.valueToCode(block, 'FIND',
      Blockly.JavaScript.ORDER_NONE) || '\'\'';
  var text = Blockly.JavaScript.valueToCode(block, 'VALUE',
      Blockly.JavaScript.ORDER_MEMBER) || '\'\'';
  var code = text + '.' + operator + '(' + subString + ')';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};
