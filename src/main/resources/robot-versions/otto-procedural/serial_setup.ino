// MODULE SERIAL - SETUP

Serial.begin(9600);

// offer option to disable battery check
while (Serial.available()) Serial.read();
Serial.println(F("Hi! Press U for USB-powered run."));
delay(5000);
if (Serial.available() && Serial.read() == 'U') {
    ignore_batteries = 1;
    Serial.println(F("USB-powered"));
} else {
  Serial.println(F("Battery-powered"));
}