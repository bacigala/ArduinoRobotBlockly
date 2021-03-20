
// SOUND BLOCKS GENERATOR

Blockly.OttoProcedural['mp3_play_song'] = function(block) {
  var song_no = block.getFieldValue('SONG_NO');
  var code = 'mp3_play(' + song_no + ');\n';
  return code;
};

Blockly.OttoProcedural['mp3_set_volume'] = function(block) {
  var volume_level = block.getFieldValue('VOLUME_LEVEL');
  var code = 'mp3_set_volume(' + volume_level + ');\n';
  return code;
};

Blockly.OttoProcedural['melody_play'] = function(block) {
  var melody_no = block.getFieldValue('MELODY_NO');
  var code = 'melody_play(' + melody_no + ');\n';
  return code;
};

Blockly.OttoProcedural['melody_stop'] = function(block) {
  return 'melody_stop();\n';
};

Blockly.OttoProcedural['tone_play'] = function(block) {
  var frequency = block.getFieldValue('FREQUENCY');
  var duration = block.getFieldValue('DURATION');
  var code = 'tone2(' + frequency + ', ' + duration + ');\n';
  return code;
};

Blockly.OttoProcedural['beep'] = function(block) {
  return 'beep();\n';
};

Blockly.OttoProcedural['play_sound_effect'] = function(block) {
  var effect_no = block.getFieldValue('EFFECT_NO');
  var code = 'sound_effect' + effect_no + '();\n';
  return code;
};
