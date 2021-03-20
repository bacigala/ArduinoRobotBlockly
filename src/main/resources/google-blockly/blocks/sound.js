
// SOUND BLOCKS DEFINITION

Blockly.defineBlocksWithJsonArray([
// mp3 player - play song
{
  "type": "mp3_play_song",
  "message0": "Play song number %1",
  "args0": [
    {
      "type": "field_number",
      "name": "SONG_NO",
      "value": 1,
      "min": 1,
      "max": 6,
      "precision": 1
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
// mp3 player - set volume
{
  "type": "mp3_set_volume",
  "message0": "Set volume level of mp3 to %1",
  "args0": [
    {
      "type": "field_number",
      "name": "VOLUME_LEVEL",
      "value": 0,
      "min": 0,
      "max": 30,
      "precision": 1
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
// melody - play
{
  "type": "melody_play",
  "message0": "Play melody number %1",
  "args0": [
    {
      "type": "field_number",
      "name": "MELODY_NO",
      "value": 1,
      "min": 1,
      "max": 7,
      "precision": 1
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
// melody - stop
{
  "type": "melody_stop",
  "message0": "Stop playing melody.",
  "previousStatement": null,
  "nextStatement": null,
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
// tone - play
{
  "type": "tone_play",
  "message0": "Play tone frequency %1 , duration %2",
  "args0": [
    {
      "type": "field_number",
      "name": "FREQUENCY",
      "value": 0,
      "min": 1,
      "max": 65535,
      "precision": 1
    },
    {
      "type": "field_number",
      "name": "DURATION",
      "value": 0,
      "min": 1,
      "max": 65535,
      "precision": 1
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
// beep
{
  "type": "beep",
  "message0": "Beep",
  "previousStatement": null,
  "nextStatement": null,
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
},
// sound effect - play
{
  "type": "play_sound_effect",
  "message0": "Play sound effect number %1",
  "args0": [
    {
      "type": "field_number",
      "name": "EFFECT_NO",
      "value": 1,
      "min": 1,
      "max": 16,
      "precision": 1
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 315,
  "tooltip": "",
  "helpUrl": ""
}
]);