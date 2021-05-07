
// SOUND BLOCKS GENERATOR

Blockly.OttoProcedural['mp3_play_song'] = function(block) {
  let song_no = Blockly.OttoProcedural.valueToCode(block, 'SONG_NO', Blockly.OttoProcedural.ORDER_ATOMIC);
  return 'mp3_play(' + song_no + ');\n';
};

Blockly.OttoProcedural['mp3_set_volume'] = function(block) {
  var volume_level = Blockly.OttoProcedural.valueToCode(block, 'VOLUME_LEVEL', Blockly.OttoProcedural.ORDER_ATOMIC);
  var code = 'mp3_set_volume(' + volume_level + ');\n';
  return code;
};

Blockly.OttoProcedural['mp3_get_volume'] = function(block) {
  let code = 'mp3_volume';
  return [code, Blockly.OttoProcedural.ORDER_NONE];
};

Blockly.OttoProcedural['mp3_complex'] = function(block) {
  let fun = block.getFieldValue('FUNCTION');
  switch (fun) {
    case "PLAYBACK":
      return 'mp3_playback();\n';
    case "PAUSE":
      return 'mp3_pause();\n';
  }
};

Blockly.OttoProcedural['melody_play'] = function(block) {
  var melody_no = Blockly.OttoProcedural.valueToCode(block, 'MELODY_NO', Blockly.OttoProcedural.ORDER_ATOMIC);
  var code = 'melody_play(' + melody_no + ');\n';
  return code;
};

Blockly.OttoProcedural['melody_stop'] = function(block) {
  return 'melody_stop();\n';
};

Blockly.OttoProcedural['tone_play'] = function(block) {
  var frequency = Blockly.OttoProcedural.valueToCode(block, 'FREQUENCY', Blockly.OttoProcedural.ORDER_ATOMIC);
  var duration = Blockly.OttoProcedural.valueToCode(block, 'DURATION', Blockly.OttoProcedural.ORDER_ATOMIC);
  var code = 'tone2(' + frequency + ', ' + duration + ');\n';
  return code;
};

Blockly.OttoProcedural['beep'] = function(block) {
  return 'beep();\n';
};

Blockly.OttoProcedural['play_sound_effect'] = function(block) {
  var effect_no = Blockly.OttoProcedural.valueToCode(block, 'EFFECT_NO', Blockly.OttoProcedural.ORDER_ATOMIC);
  var code = 'sound_effect' + effect_no + '();\n';
  return code;
};
