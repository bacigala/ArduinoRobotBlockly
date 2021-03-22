
// SOUND BLOCKS DEFINITION

Blockly.defineBlocksWithJsonArray([
// mp3 player - play song
{
  "type": "mp3_play_song",
  "message0": "Play song number %1",
  "args0": [
	  {
      "type": "input_value",
      "name": "SONG_NO",
      "check": "Number"
    }
  ],
  "previousStatement": null,
	"inputsInline": true,
  "nextStatement": null,
  "colour": 30,
  "tooltip": "Allowed range: 1..6",
  "helpUrl": ""
},
// mp3 player - set volume
{
  "type": "mp3_set_volume",
  "message0": "Set volume level of mp3 to %1",
  "args0": [
		{
      "type": "input_value",
      "name": "VOLUME_LEVEL",
      "check": "Number"
    }
  ],
	"inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 30,
  "tooltip": "Allowed range: 0..30",
  "helpUrl": ""
},
// melody - play
{
  "type": "melody_play",
  "message0": "Play melody number %1",
  "args0": [
		{
      "type": "input_value",
      "name": "MELODY_NO",
      "check": "Number"
    }
  ],
	"inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 30,
  "tooltip": "Allowed range: 1..7",
  "helpUrl": ""
},
// melody - stop
{
  "type": "melody_stop",
  "message0": "Stop playing melody.",
  "previousStatement": null,
  "nextStatement": null,
  "colour": 30,
  "tooltip": "",
  "helpUrl": ""
},
// tone - play
{
  "type": "tone_play",
  "message0": "Play tone frequency %1 , duration %2",
  "args0": [
		{
      "type": "input_value",
      "name": "FREQUENCY",
      "check": "Number"
    },
		{
      "type": "input_value",
      "name": "DURATION",
      "check": "Number"
    }
  ],
	"inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 30,
  "tooltip": "Allowed range: 1..65535",
  "helpUrl": ""
},
// beep
{
  "type": "beep",
  "message0": "Beep",
  "previousStatement": null,
  "nextStatement": null,
  "colour": 30,
  "tooltip": "",
  "helpUrl": ""
},
// sound effect - play
{
  "type": "play_sound_effect",
  "message0": "Play sound effect number %1",
  "args0": [
		{
      "type": "input_value",
      "name": "EFFECT_NO",
      "check": "Number"
    }
  ],
	"inputsInline": true,
  "previousStatement": null,
  "nextStatement": null,
  "colour": 30,
  "tooltip": "Allowed range: 1..16",
  "helpUrl": ""
}
]);