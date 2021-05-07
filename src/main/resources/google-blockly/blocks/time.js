
// TIME BLOCKS DEFINITION

Blockly.defineBlocksWithJsonArray([

// CURRENT TIME
{
  "type": "get_time",
  "message0": "Current time",
  "output": "Number",
  "colour": 330,
  "tooltip": "",
  "helpUrl": ""
},

// RESET TIMER
{
  "type": "reset_timer",
  "message0": "Reset %1",
  "args0": [
    {
      "type": "field_dropdown",
      "name": "TIMER_NAME",
      "options": [
        [
          "timer1",
          "timer1"
        ],
        [
          "timer2",
          "timer2"
        ],
        [
          "timer3",
          "timer3"
        ]
      ]
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 330,
  "tooltip": "",
  "helpUrl": ""
},

{
// GET TIMER TIME{
  "type": "get_timer",
  "message0": "Time of  %1",
  "args0": [
    {
      "type": "field_dropdown",
      "name": "TIMER_NAME",
      "options": [
        [
          "timer1",
          "timer1"
        ],
        [
          "timer2",
          "timer2"
        ],
        [
          "timer3",
          "timer3"
        ]
      ]
    }
  ],
  "output": "Number",
  "colour": 330,
  "tooltip": "",
  "helpUrl": ""
}
]);