	
goog.provide('Blockly.OttoProcedural');

goog.require('Blockly.Generator');
goog.require('Blockly.utils.global');
goog.require('Blockly.utils.string');

Blockly.OttoProcedural = new Blockly.Generator('OttoProcedural');

// Illegal variable names, https://www.arduino.cc/reference/en/
Blockly.OttoProcedural.addReservedWords(
'Serial,Stream,Keyboard,Mouse,LOW,HIGH,INPUT,OUTPUT,INPUT_PULLUP,LED_BUILTIN,byte,char,float,int,long,word,array,bool,double,short,size_t,string,String,unsigned,void,const,scope,static,volatile,sizeof,PROGMEM,loop,setup,break,continue,do,while,else,for,goto,if,return,switch,else,while,define,include,' + Object.getOwnPropertyNames(Blockly.utils.global).join(','));

/**
 * Order of operation ENUMs.
 * https://developer.mozilla.org/en/OttoProcedural/Reference/Operators/Operator_Precedence
 */
Blockly.OttoProcedural.ORDER_ATOMIC = 0;           // 0 "" ...
Blockly.OttoProcedural.ORDER_NEW = 1.1;            // new
Blockly.OttoProcedural.ORDER_MEMBER = 1.2;         // . []
Blockly.OttoProcedural.ORDER_FUNCTION_CALL = 2;    // ()
Blockly.OttoProcedural.ORDER_INCREMENT = 3;        // ++
Blockly.OttoProcedural.ORDER_DECREMENT = 3;        // --
Blockly.OttoProcedural.ORDER_BITWISE_NOT = 4.1;    // ~
Blockly.OttoProcedural.ORDER_UNARY_PLUS = 4.2;     // +
Blockly.OttoProcedural.ORDER_UNARY_NEGATION = 4.3; // -
Blockly.OttoProcedural.ORDER_LOGICAL_NOT = 4.4;    // !
Blockly.OttoProcedural.ORDER_TYPEOF = 4.5;         // typeof
Blockly.OttoProcedural.ORDER_VOID = 4.6;           // void
Blockly.OttoProcedural.ORDER_DELETE = 4.7;         // delete
Blockly.OttoProcedural.ORDER_AWAIT = 4.8;          // await
Blockly.OttoProcedural.ORDER_EXPONENTIATION = 5.0; // **
Blockly.OttoProcedural.ORDER_MULTIPLICATION = 5.1; // *
Blockly.OttoProcedural.ORDER_DIVISION = 5.2;       // /
Blockly.OttoProcedural.ORDER_MODULUS = 5.3;        // %
Blockly.OttoProcedural.ORDER_SUBTRACTION = 6.1;    // -
Blockly.OttoProcedural.ORDER_ADDITION = 6.2;       // +
Blockly.OttoProcedural.ORDER_BITWISE_SHIFT = 7;    // << >> >>>
Blockly.OttoProcedural.ORDER_RELATIONAL = 8;       // < <= > >=
Blockly.OttoProcedural.ORDER_IN = 8;               // in
Blockly.OttoProcedural.ORDER_INSTANCEOF = 8;       // instanceof
Blockly.OttoProcedural.ORDER_EQUALITY = 9;         // == != === !==
Blockly.OttoProcedural.ORDER_BITWISE_AND = 10;     // &
Blockly.OttoProcedural.ORDER_BITWISE_XOR = 11;     // ^
Blockly.OttoProcedural.ORDER_BITWISE_OR = 12;      // |
Blockly.OttoProcedural.ORDER_LOGICAL_AND = 13;     // &&
Blockly.OttoProcedural.ORDER_LOGICAL_OR = 14;      // ||
Blockly.OttoProcedural.ORDER_CONDITIONAL = 15;     // ?:
Blockly.OttoProcedural.ORDER_ASSIGNMENT = 16;      // = += -= **= *= /= %= <<= >>= ...
Blockly.OttoProcedural.ORDER_YIELD = 17;           // yield
Blockly.OttoProcedural.ORDER_COMMA = 18;           // ,
Blockly.OttoProcedural.ORDER_NONE = 99;            // (...)

/**
 * List of outer-inner pairings that do NOT require parentheses.
 * @type {!Array.<!Array.<number>>}
 */
Blockly.OttoProcedural.ORDER_OVERRIDES = [
  // (foo()).bar -> foo().bar
  // (foo())[0] -> foo()[0]
  [Blockly.OttoProcedural.ORDER_FUNCTION_CALL, Blockly.OttoProcedural.ORDER_MEMBER],
  // (foo())() -> foo()()
  [Blockly.OttoProcedural.ORDER_FUNCTION_CALL, Blockly.OttoProcedural.ORDER_FUNCTION_CALL],
  // (foo.bar).baz -> foo.bar.baz
  // (foo.bar)[0] -> foo.bar[0]
  // (foo[0]).bar -> foo[0].bar
  // (foo[0])[1] -> foo[0][1]
  [Blockly.OttoProcedural.ORDER_MEMBER, Blockly.OttoProcedural.ORDER_MEMBER],
  // (foo.bar)() -> foo.bar()
  // (foo[0])() -> foo[0]()
  [Blockly.OttoProcedural.ORDER_MEMBER, Blockly.OttoProcedural.ORDER_FUNCTION_CALL],

  // !(!foo) -> !!foo
  [Blockly.OttoProcedural.ORDER_LOGICAL_NOT, Blockly.OttoProcedural.ORDER_LOGICAL_NOT],
  // a * (b * c) -> a * b * c
  [Blockly.OttoProcedural.ORDER_MULTIPLICATION, Blockly.OttoProcedural.ORDER_MULTIPLICATION],
  // a + (b + c) -> a + b + c
  [Blockly.OttoProcedural.ORDER_ADDITION, Blockly.OttoProcedural.ORDER_ADDITION],
  // a && (b && c) -> a && b && c
  [Blockly.OttoProcedural.ORDER_LOGICAL_AND, Blockly.OttoProcedural.ORDER_LOGICAL_AND],
  // a || (b || c) -> a || b || c
  [Blockly.OttoProcedural.ORDER_LOGICAL_OR, Blockly.OttoProcedural.ORDER_LOGICAL_OR]
];

/**
 * Initialise the database of variable names.
 * @param {!Blockly.Workspace} workspace Workspace to generate code from.
 */
Blockly.OttoProcedural.init = function(workspace) {
	// Dictionary of variable definitions.
	Blockly.OttoProcedural.variableDefinitions_ = Object.create(null);
	// Dictionaty of functions / methods.
	Blockly.OttoProcedural.definitions_ = Object.create(null);
  // Create a dictionary mapping desired function names in definitions_
  // to actual function names (to avoid collisions with user functions).
  Blockly.OttoProcedural.functionNames_ = Object.create(null);

  if (!Blockly.OttoProcedural.variableDB_) {
    Blockly.OttoProcedural.variableDB_ =
        new Blockly.Names(Blockly.OttoProcedural.RESERVED_WORDS_);
  } else {
    Blockly.OttoProcedural.variableDB_.reset();
  }

  Blockly.OttoProcedural.variableDB_.setVariableMap(workspace.getVariableMap());

  var defvars = [];
  // Add developer variables (not created or named by the user).
  var devVarList = Blockly.Variables.allDeveloperVariables(workspace);
  for (var i = 0; i < devVarList.length; i++) {
    defvars.push(Blockly.OttoProcedural.variableDB_.getName(devVarList[i],
        Blockly.Names.DEVELOPER_VARIABLE_TYPE));
  }

  // Add user variables, but only ones that are being used.
  var variables = Blockly.Variables.allUsedVarModels(workspace);
  for (var i = 0; i < variables.length; i++) {
    defvars.push(Blockly.OttoProcedural.variableDB_.getName(variables[i].getId(),
        Blockly.VARIABLE_CATEGORY_NAME));
  }

  // Declare all of the variables.
  if (defvars.length) {
    Blockly.OttoProcedural.definitions_['variables'] =
        'int16_t ' + defvars.join(', ') + ';';
  }
};

/**
 * Prepend the generated code with the variable definitions.
 * @param {string} code Generated code.
 * @return {string} Completed code.
 */
Blockly.OttoProcedural.finish = function(code) {
	
	var variables = Blockly.OttoProcedural.definitions_['variables'];
	delete Blockly.OttoProcedural.definitions_['variables'];
	
  // Convert the definitions dictionary into a list.
  var definitions = [];
  for (var name in Blockly.OttoProcedural.definitions_) {
    definitions.push(Blockly.OttoProcedural.definitions_[name]);
  }
  // Clean up temporary data.
  delete Blockly.OttoProcedural.definitions_;
  delete Blockly.OttoProcedural.functionNames_;
  Blockly.OttoProcedural.variableDB_.reset();
	
	var result = '';
	if (variables) result += variables + '\n\n';
	// result += 'void setup() {\n'
	// result += '\t\\\\ SETUP CODE HERE\n';
	// result += '}\n\n';
	result += 'void loop() {\n';
	result += code + '\n';
	result += 'check_battery();\n'
	result += 'delay(500);\n'
	result += '}\n\n';
	result += definitions.join('\n\n');
	
  return result;
};

/**
 * Naked values are top-level blocks with outputs that aren't plugged into
 * anything.  A trailing semicolon is needed to make this legal.
 * @param {string} line Line of generated code.
 * @return {string} Legal line of code.
 */
 // COMMENT THESE LINES
Blockly.OttoProcedural.scrubNakedValue = function(line) {
  return '\\\\ ' + line + '\n';
};

/**
 * Encode a string as a properly escaped OttoProcedural string, complete with
 * quotes.
 * @param {string} string Text to encode.
 * @return {string} OttoProcedural string.
 * @private
 */
Blockly.OttoProcedural.quote_ = function(string) {
  // Can't use goog.string.quote since Google's style guide recommends
  // JS string literals use single quotes.
  string = string.replace(/\\/g, '\\\\')
                 .replace(/\n/g, '\\\n')
                 .replace(/'/g, '\\\'');
  return '\'' + string + '\'';
};

/**
 * Encode a string as a properly escaped multiline OttoProcedural string, complete
 * with quotes.
 * @param {string} string Text to encode.
 * @return {string} OttoProcedural string.
 * @private
 */
Blockly.OttoProcedural.multiline_quote_ = function(string) {
  // Can't use goog.string.quote since Google's style guide recommends
  // JS string literals use single quotes.
  var lines = string.split(/\n/g).map(Blockly.OttoProcedural.quote_);
  return lines.join(' + \'\\n\' +\n');
};

// Called on every block to generate code.
Blockly.OttoProcedural.scrub_ = function(block, code, opt_thisOnly) {
  var nextBlock = block.nextConnection && block.nextConnection.targetBlock();
  var nextCode = opt_thisOnly ? '' : Blockly.OttoProcedural.blockToCode(nextBlock);
  return code + nextCode;
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
Blockly.OttoProcedural.getAdjusted = function(block, atId, opt_delta, opt_negate,
    opt_order) {
  var delta = opt_delta || 0;
  var order = opt_order || Blockly.OttoProcedural.ORDER_NONE;
  if (block.workspace.options.oneBasedIndex) {
    delta--;
  }
  var defaultAtIndex = block.workspace.options.oneBasedIndex ? '1' : '0';
  if (delta > 0) {
    var at = Blockly.OttoProcedural.valueToCode(block, atId,
        Blockly.OttoProcedural.ORDER_ADDITION) || defaultAtIndex;
  } else if (delta < 0) {
    var at = Blockly.OttoProcedural.valueToCode(block, atId,
        Blockly.OttoProcedural.ORDER_SUBTRACTION) || defaultAtIndex;
  } else if (opt_negate) {
    var at = Blockly.OttoProcedural.valueToCode(block, atId,
        Blockly.OttoProcedural.ORDER_UNARY_NEGATION) || defaultAtIndex;
  } else {
    var at = Blockly.OttoProcedural.valueToCode(block, atId, order) ||
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
      var innerOrder = Blockly.OttoProcedural.ORDER_ADDITION;
    } else if (delta < 0) {
      at = at + ' - ' + -delta;
      var innerOrder = Blockly.OttoProcedural.ORDER_SUBTRACTION;
    }
    if (opt_negate) {
      if (delta) {
        at = '-(' + at + ')';
      } else {
        at = '-' + at;
      }
      var innerOrder = Blockly.OttoProcedural.ORDER_UNARY_NEGATION;
    }
    innerOrder = Math.floor(innerOrder);
    order = Math.floor(order);
    if (innerOrder && order >= innerOrder) {
      at = '(' + at + ')';
    }
  }
  return at;
};
