## What is Shun-Feng-Er

Shun-Feng-Er is a guardian general who supports [Mazu](http://en.wikipedia.org/wiki/Mazu_(goddess)) to protect the sea. His super-power is to hear sound from far far away.

We use Shun-Feng-Er as the name of this project for helping people to hear the sound from his/her home. For an eye impaired person, he/she uses audio and input devices to control his/her computer. So, they can be benefit from Shun-Feng-Er to hear the audio from remote computer and to control their remote computer. It somehow looks like a Remote Desktop Tool for eye impaired persons.

## Modules of Shun-Feng-Er

We create two modules for server(Ear) and client(Mouth).

The server is installed at remote computer. It listens audio from computer and transmit to client.

The client is an app installed at mobile phone, that we only support Android phone now. It plays the audio from server to user and sends commands from input devices to server, that we only support Keyboard Event now.

## Download

Server(Ear) of Shun-Feng-Er is at [here](https://github.com/john-hu/shun-feng-er/raw/v0.1/Release/AudioServer.jar).

Client(Mouth) of Shun-Feng-Er is at [here](https://github.com/john-hu/shun-feng-er/raw/v0.1/Release/app.apk).

## Install

- Please install an audio loopback driver, like [kuwatec audio loopback](http://www.kuwatec.co.jp/synvisum/en/man/contents/audiorec.html).
- Follow the instruction of audio loopback driver to configure the loopback devices.
- Start AudioServer with the following command:
```
  java -jar AudioServer.jar server {driver-name} [port|13579]
  ex:
    java -jar AudioServer.jar server kuwatech
```
- Install app to your android phone
```
  adb install app.apk
```
- Connect USB/Bluetooth Keyboard to your android phone
- Connect to the AudioServer with IP or DNS.

## The port of AudioServer

The default port of AudioServer is 13579. You must check if your android phone can connect to AudioServer with this port.

If you want to change the port, you may use the third argument to change the port of server. In client, you may use `:` to specify the port after the IP or DNS, like `192.168.0.101:24680`.

