
!General info
	name = Otto 2021 Procedural
	
	toolbox =	<xml id='toolbox-otto-procedural' style='display: none'>\
							<category name='Logic' categorystyle='logic_category' toolboxitemid='logic'>\
								<block type='controls_if'></block>\
								<block type='logic_compare'></block>\
								<block type='logic_operation'></block>\
								<block type='logic_negate'></block>\
								<block type='logic_boolean'></block>\
								<block type='logic_ternary'></block>\
							</category>\
							\
							<category name='Loops' categorystyle='loop_category' toolboxitemid='loops'>\
								<block type='controls_repeat_ext'>\
									<value name='TIMES'>\
										<shadow type='math_number'>\
											<field name='NUM'>10</field>\
										</shadow>\
									</value>\
								</block>\
								<block type='controls_whileUntil'></block>\
								<block type='controls_for'>\
									<value name='FROM'>\
										<shadow type='math_number'>\
											<field name='NUM'>1</field>\
										</shadow>\
									</value>\
									<value name='TO'>\
										<shadow type='math_number'>\
											<field name='NUM'>10</field>\
										</shadow>\
									</value>\
									<value name='BY'>\
										<shadow type='math_number'>\
											<field name='NUM'>1</field>\
										</shadow>\
									</value>\
								</block>\
								<block type='controls_flow_statements'></block>	\
							</category>\
							\
							<category name='Math' categorystyle='math_category' toolboxitemid='math'>\
								<block type='math_number' gap='32'>\
									<field name='NUM'>123</field>\
								</block>\
								<block type='math_arithmetic'>\
									<value name='A'>\
										<shadow type='math_number'>\
											<field name='NUM'>1</field>\
										</shadow>\
									</value>\
									<value name='B'>\
										<shadow type='math_number'>\
											<field name='NUM'>1</field>\
										</shadow>\
									</value>\
								</block>\
								<block type='math_single'>\
									<value name='NUM'>\
										<shadow type='math_number'>\
											<field name='NUM'>9</field>\
										</shadow>\
									</value>\
								</block>\
								<block type='math_number_property'>\
									<value name='NUMBER_TO_CHECK'>\
										<shadow type='math_number'>\
											<field name='NUM'>0</field>\
										</shadow>\
									</value>\
								</block>\
								<block type='math_modulo'>\
									<value name='DIVIDEND'>\
										<shadow type='math_number'>\
											<field name='NUM'>64</field>\
										</shadow>\
									</value>\
									<value name='DIVISOR'>\
										<shadow type='math_number'>\
											<field name='NUM'>10</field>\
										</shadow>\
									</value>\
								</block>\
								<block type='math_random_int'>\
									<value name='FROM'>\
										<shadow type='math_number'>\
											<field name='NUM'>1</field>\
										</shadow>\
									</value>\
									<value name='TO'>\
										<shadow type='math_number'>\
											<field name='NUM'>100</field>\
										</shadow>\
									</value>\
								</block>\
							</category>\
							\
							<sep></sep>\
							\
							<category name='Variables' categorystyle='variable_category' custom='VARIABLE_DYNAMIC' toolboxitemid='variables'></category>\
							\
							<category name='Functions' categorystyle='procedure_category' custom='PROCEDURE' toolboxitemid='functions'></category>\
							\
							<sep></sep>\
							\
							<category name='Sound' colour='30' toolboxitemid='sound'>\
								<label text='Mp3 player'></label>\
								<block type='mp3_play_song'>\
									<value name='SONG_NO'>\
										<shadow type='math_number'>\
											<field name='NUM'>1</field>\
										</shadow>\
									</value>				\
								</block>\
								<block type='mp3_set_volume'>\
									<value name='VOLUME_LEVEL'>\
										<shadow type='math_number'>\
											<field name='NUM'>15</field>\
										</shadow>\
									</value>\
								</block>\
								\
								<label text='Melodies'></label>\
								<block type='melody_play'>\
									<value name='MELODY_NO'>\
										<shadow type='math_number'>\
											<field name='NUM'>1</field>\
										</shadow>\
									</value>\
								</block>\
								<block type='melody_stop'></block>\
								\
								<label text='Other'></label>\
								<block type='beep'></block>\
								<block type='play_sound_effect'>\
									<value name='EFFECT_NO'>\
										<shadow type='math_number'>\
											<field name='NUM'>3</field>\
										</shadow>\
									</value>\
								</block>\
								<block type='tone_play'>\
									<value name='FREQUENCY'>\
										<shadow type='math_number'>\
											<field name='NUM'>2000</field>\
										</shadow>\
									</value>\
									<value name='DURATION'>\
										<shadow type='math_number'>\
											<field name='NUM'>30</field>\
										</shadow>\
									</value>\
								</block>\
							</category>\
							\
							<category name='Motion' colour='60' toolboxitemid='motion'>\
								<block type='otto_procedural_motor_move'>\
									<value name='MOTOR_POSITION'>\
										<shadow type='math_number'>\
											<field name='NUM'>3</field>\
										</shadow>\
									</value>\
								</block>\
								<block type='reset_motors'></block>\
								<block type='move_complex'></block>\
								<block type='turn_complex'></block>\
								<block type='tiptoes'></block>\
								<block type='heels'></block>\
								<block type='wave_hand'></block>\
							</category>\
							\
							<category name='Sensors' colour='90' toolboxitemid='ultrasound'>\
								<label text='Ultrasound'></label>\
								<block type='otto_procedural_ultrasound_get_distance'></block>\
								<block type='wait_ultrasound'>\
								<value name='DISTANCE'>\
										<shadow type='math_number'>\
											<field name='NUM'>50</field>\
										</shadow>\
									</value>\
								</block>\
								<block type='ulrasonic_gesture_record'></block>\
								<block type='ulrasonic_gesture_last'></block>\
								<label text='Touch'></label>\
								<block type='button_pressed'></block>\
								<block type='wait_touch'></block>\
								<block type='touch_gesture_record'></block>\
								<block type='touch_gesture_last'></block>\
							</category>\
							\
							<category name='Serial' colour='180' toolboxitemid='serial'>\
								<label text='Send'></label>\
								<block type='serial_println'>\
									<value name='MESSAGE'>\
										<shadow type='text'>\
											<field name='TEXT'>Hello</field>\
										</shadow>\
									</value>\
								</block>\
								<label text='Receive'></label>\
								<block type='serial_get_string'></block>\
								<block type='serial_get_number'></block>\
								<label text='String'></label>\
								<block type='text'>\
									<field name='TEXT'>Message</field>\
								</block>\
							</category>\
							\
							<category name='Time' colour='210' toolboxitemid='time'>\
								<label text='Time'></label>\
								<block type='get_time'></block>\
								<label text='Timers'></label>\
								<block type='reset_timer'></block>\
								<block type='get_timer'></block>\
							</category>\
						</xml>

	workspace = <xml>\
								<block type='otto_basic_loop' deletable='false' movable='false'></block>\
							</xml>

	robotMaxMemory 	= 200
	moduleCount		 	= 8
	categories		 	= logic, math, loops, variables, functions, sound, motion, ultrasound, serial, time
	generator				= OttoProcedural
	programInRam		= false
	codeLoader			= ottoProcedural
	exampleCount		= 2

!Module 1
	module1.name 				  = Basic
	module1.description 	= Basic logic, math, loops, aritmetics, variables
	module1.required 			= false
	module1.location			= otto-procedural/basic
	module1.size					= 10
	module1.categories		= logic, math, loops, variables

!Module 2
	module2.name 				  = Procedures and functions
	module2.description 	= 
	module2.required 			= false
	module2.location			= otto-procedural/procedures
	module2.size					= 10
	module2.categories		= functions

!Module 3
	module3.name 				  = Vital
	module3.description 	= Checks batteries.
	module3.required 			= true
	module3.location			= otto-procedural/vital
	module3.size					= 10
	module3.categories		= 

!Module 4
	module4.name 				  = Sound
	module4.description 	= Enables Otto to beep and play mp3 music.
	module4.required 			= false
	module4.location			= otto-procedural/sound
	module4.size					= 15
	module4.categories		= sound
	
!Module 5
	module5.name 				  = Motion
	module5.description 	= 
	module5.required 			= false
	module5.location			= otto-procedural/motion
	module5.size					= 20
	module5.categories		= motion
	
!Module 6
	module6.name 				  = Ultrasound
	module6.description 	= 
	module6.required 			= false
	module6.location			= otto-procedural/ultrasound
	module6.size					= 10
	module6.categories		= ultrasound
	
!Module 7
	module7.name 				  = Serial
	module7.description 	= 
	module7.required 			= false
	module7.location			= otto-procedural/serial
	module7.size					= 10
	module7.categories		= serial
	
!Module 8
	module8.name 				  = Time
	module8.description 	= 
	module8.required 			= false
	module8.location			= otto-procedural/time
	module8.size					= 5
	module8.categories		= time
	
!Example 1
	example1.name					= Wave on proximity
	example1.description	= When Otto sees obstacle, he waves. 
	example1.modules			= 3, 5, 6
	example1.workspace 		= <xml xmlns='https://developers.google.com/blockly/xml'><block type='otto_basic_loop' id='h7W;KMe!nWzO^vGJwpH?' deletable='false' movable='false' x='10' y='10'><statement name='PROGRAM'><block type='wait_ultrasound' id='H4}m1o7mI%%18tKa%kN;'><field name='RELATION'>LESS</field><value name='DISTANCE'><shadow type='math_number' id='{;l*d=]mhlUyG710R(FV'><field name='NUM'>50</field></shadow></value><next><block type='wave_hand' id='y9A1nH9jcCz*~kB7mdil'></block></next></block></statement></block></xml>
	
!Example 2
	example2.name					= On tiptoes, on heels
	example2.description	= Let Otto stand  on his tiptoes / on heels by usig the touch sensors.
	example2.modules			= 1, 3, 5, 6
	example2.workspace 		= <xml xmlns='https://developers.google.com/blockly/xml'><block type='otto_basic_loop' id='yW*nV4NgvDA$!}N_cjP7' deletable='false' movable='false' x='10' y='10'><statement name='PROGRAM'><block type='controls_if' id='7u+4MJcw9J7?]`HG:#O='><value name='IF0'><block type='button_pressed' id='(O5.g_YtM,;BTkXS7iSs'><field name='BUTTON'>LEFT</field></block></value><statement name='DO0'><block type='tiptoes' id='#)k(onH.LZj+%-}_R@p?'></block></statement><next><block type='controls_if' id='I=;_],QvW#V[C6b,k$Kx'><value name='IF0'><block type='button_pressed' id='F*$mg385U@!lk,;].nB['><field name='BUTTON'>RIGHT</field></block></value><statement name='DO0'><block type='heels' id='Em7!`A.hZYL$L.!lxFR+'></block></statement></block></next></block></statement></block></xml>


