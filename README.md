## What is Shun-Feng-Er

Shun-Feng-Er is a guardian general who supports [Mazu](http://en.wikipedia.org/wiki/Mazu_(goddess)) to protect the sea. His super-power is to hear sound from far far away.

We use Shun-Feng-Er as the name of this project for helping people to hear the sound from his/her home. For an eye impaired person, he/she uses audio and input devices to control his/her computer. So, they can be benefit from Shun-Feng-Er to hear the audio from remote computer and to control their remote computer. It somehow looks like a Remote Desktop Tool for eye impaired persons.

## Modules of Shun-Feng-Er

We create two modules for server(Ear) and client(Mouth).

The server is installed at remote computer. It listens audio from computer and transmit to client.

The client is an app installed at mobile phone, that we only support Android phone now. It plays the audio from server to user and sends commands from input devices to server, that we only support Keyboard Event now.

## Download

Server (Ear) of Shun-Feng-Er is at [here](https://github.com/john-hu/shun-feng-er/raw/latest/Release/AudioServer.jar).

Client (Mouth) of Shun-Feng-Er is at [here](https://github.com/john-hu/shun-feng-er/raw/latest/Release/app.apk).

## Install audio lookback driver

- Please install an audio loopback driver, like [kuwatec audio loopback](http://www.kuwatec.co.jp/synvisum/en/man/contents/audiorec.html).
- Follow the instruction of audio loopback driver to configure the loopback devices.

## Install android app

Please connect your android phone with USB and follow the [instructions to turn on the unknown source](http://developer.android.com/distribute/tools/open-distribution.html). Once done, we can have app installed to android phone through the following command:

```
  adb install app.apk
```

## Usage

Before using it, we should start the server and plugged in USB or BT keyboard to android phone.

### Start the ear of Shun-Feng-Er

The help of Ear can be found with the command `java -jar AudioServer.jar --help`:

```
Usage: java -jar AudioServer.jar
                [(-m|--mixer) <mixer>] [(-p|--port) <port>] [(-P|--password) <password>] [(-K|--key-path) <key-path>] [(-k|--key-pass) <key-pass>]

  [(-m|--mixer) <mixer>]
        The audio loopback driver name. We will use this name to query mixer
        device. (default: kuwatec)

  [(-p|--port) <port>]
        server port (default: 13579)

  [(-P|--password) <password>]
        The password to control and listen your computer

  [(-K|--key-path) <key-path>]
        The path to control and listen your computer

  [(-k|--key-pass) <key-pass>]
        The password to control and listen your computer
```

All arguments are optional if you run the whole procedure described in this doc. But we strongly suggest to set password and key to have secured connection.

#### Password

The password is just a password which is not linked with your windows account. If the app doesn't give password or gives wrong password, the server disconnects the connection automatically. So, please remember the password once you turn on it.

#### Key store

The key is the most confusing part. It is a key store for building SSL connections. Like most of the SSL CA, you can use your SSL key brought by other key certification center. If you don't have one, you may TEST the server with the [sample key](https://github.com/john-hu/shun-feng-er/blob/latest/sample-keys/server1.jks) at our github repo. The password of this sample key is `shun-feng-er`. You may use the following arguments at the end of command: `--key-path server1.jks --key-pass shun-feng-er`.

Please note this is a sample key and we had opened its password here. Everyone can read the private key from this key store. So, please DO NOT use it as a primary key.

If you don't have a valid key, you may generate the key by yourself with the openssl command:
```
$ openssl genrsa -out private/www.example.com.key.pem 4096
$ openssl req -sha256 -new -key private/www.example.com.key.pem \
       -out certs/www.example.com.csr.pem
```

Once you have csr file, please email <im@john.hu>. We can sign your CA with our key. The mouth (android app) of Shun-Feng-Er trusts the root CA published by Shun-Feng-Er. So, you can use your owned key to run the server and connect with our app.

Since we only support Java Key Store format, you need to use the following commands to convert CA and key to Java Key Store format:
```
$ openssl pkcs12 -export -in cert.pem -inkey key.pem > server.p12
$ keytool -importkeystore -srckeystore server.p12 -destkeystore server.jks -srcstoretype pkcs12
```

#### The port of AudioServer

The default port of AudioServer is 13579. You must check if your android phone can connect to AudioServer with this port.

If you want to change the port, you may use the third argument to change the port of server. In client, you may use `:` to specify the port after the IP or DNS, like `192.168.0.101:24680`.

