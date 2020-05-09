# I2CWIIDrums
Java application for Raspberry PI to connect WII Guitar Hero World Tour drums via I2C

## Background
I had an old set of WII Guitar Hero World Tour drums which I wanted to make use of.  

My first attempted involved connecting the wiimote to my Windows 10 laptop using Bluetooth.  This was not as straight-forward as it should be, because with Windows 10, if you try to connect a Bluetooth device using the "Add a Bluetooth Device" option, it insists that you have to enter a pass code.  The way around this is to use "Join a Personal Area Network", which allows you to skip entering a pass code.  However, it didn't always work, and I had to remember to remove the device from the list of BLuetooth devices at the end, because Windows still though it was connected.

Once the wiimote was connected, I tried to use one of the Java libraries to communicate with it, but this wouldn't work because I have the wrong Bluetooth stack on my laptop.  In the end I had to use GlovePIE, which is really, really old.  I installed it and got it working on my laptop, but when I tried it on a different laptop, it complained about a missing library and wouldn't run.  To fix this, I needed to install the "DirectX End-User Runtime Web Installer" from Microsoft.

So, having got GlovePIE running, I created a script:

```
midi.DeviceOut = 10
debug = midi.OutDevName

midi.AcousticBassDrum = WiiDrums1.Pedal
midi.CrashCymbal1 = WiiDrums.Yellow
midi.ClosedHiHat = WiiDrums.Orange
midi.LowTom = WiiDrums.Green
midi.HighTom = WiiDrums.Blue
midi.AcousticSnare = WiiDrums.Red

Wiimote.Led1 = true
```

This worked, sometimes.  Othertimes, it didn't seem to connect to the wiimote.

The final piece of the puzzle was to install "loopMIDI" that allowed me to connect the GlovePIE output to an application that could convert the MIDI commands into audio.

The end result was something that would work, usually, but needed a fair amound of prodding and poking.

## I2C
A little bit of research uncovered that the connector on the Drum kit that plugged into the wiimote used I2C and I happened to have a spare Raspberry PI model B kicking around, so I thought I would try that.  I installed the latest Raspian lite (since I would be running this headless), and bought a "Keyes Wii Nunchuck Adapter Module Shield Breakout Arduino Flux Workshop" for a few pounds from Ebay.  I connected it to the PI GPIO as follows:

```
       Adapter    PI
Clock  1          5
Data   2          3
3v3    3          1
Ground 4          6
```

I used the raspi-config command on the PI to enable the I2C functionality and edited /boot/config.txt to set the I2C speed to 400k, by adding the line

```
dtparam=i2c1_baudrate=400000
```

Using information gleaned from various websites, I got the PI reading the drum data using the PI4J library.  Now, I just had to get some audio out of the PI

## PI Audio using Java
My first attempt to get sound out was to use the Java MIDI implementation.  The first problem was that neither the Oracle JDK or the OpenJDK came with any soundbanks, which meant when my application tried to use MIDI, the Java library tried to generate an emergency soundbank.  After a couple of minutes of waiting for it to do this, I gave up and tried to find a soundbank to install.  I did find some on the Oracle website, but the quality of the sound was terrible, but I found a free soundbank at http://ntonyx.com/soft/32MbGMStereo.sf2, which I put in the jre/lib/audio directory.  This generated a nice sound, but the lag between requesting the sound and hearing it was too great.

My next attempt was to use the Java sampled audio Clip class.  I got some nice drum samples from https://www.musicradar.com.  However, for some reason, on the PI (this didn't happen on my laptop), the Clip would not stop playing in the middle, it would always play to the end.

My final attempt involved creating a SourceDataLine and feeding the audio data to it in small chunks, so that if I wanted to stop it playing, I just stopped sending data to it.  

One thing to note is that the Java sound implementation on the PI only supports 7 simultaneous SourceDataLines, but this wasn't a problem in this case, as there are only 6 drums on the kit.

One other useful thing I came across, was scripts to increase and decrease the volume of the PI from the command line (not directly linked to the project, but useful none-the-less), which I added to my .bashrc:

```
# Increase volume by 5%
alias volu='sudo amixer set PCM -- $[$(amixer get PCM|grep -o [0-9]*%|sed 's/%//')+5]%'
# Decrease volume by 5%
alias vold='sudo amixer set PCM -- $[$(amixer get PCM|grep -o [0-9]*%|sed 's/%//')-5]%'
```



## Running as a service
Once I had my application running, I wanted to have it start when the PI powered up, so I created a file called I2CDrumKit.service in /etc/systemd/system that contained:

```
[Unit]
Description=I2CDrumKit

[Service]
WorkingDirectory=/home/pi/JavaDev/I2CDrumKit
ExecStart=/usr/bin/java -Dlog4j.configurationFile=/home/pi/JavaDev/I2CDrumKit/co
nf/start-service-log4j.xml -jar /home/pi/JavaDev/I2CDrumKit/build/I2CDrumKit_0_1
.jar
KillMode=process
Restart=on-failure
Type=simple

[Install]
WantedBy=multi-user.target
```

## Outstanding Issues
At the moment, the drums always sound at the same level.  My attempt to set the MASTER_GAIN in the code is ignored and the VOLUME control isn't supported



