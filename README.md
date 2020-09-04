# VibrationKeylogger
A simple app which logs keystrokes along with accelerometer and gyroscope data, exported as a .csv file

This is a rough example of basic implementation steps and was the first app I had ever written with no Android experience. Everything runs on the main thread, 
the permission checks aren't all precise to what is actually required, and the file is created and written to dynamically instead of the data being calculated
and then added, so a lot of dangerous code is present! Use at your own risk!
