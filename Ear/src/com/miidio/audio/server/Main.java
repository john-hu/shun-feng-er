package com.miidio.audio.server;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    private static void testRecorder() throws Exception {
        //Get and display a list of
        // available mixers.
        Mixer.Info[] mixerInfo =
                AudioSystem.getMixerInfo();
        System.out.println("Available mixers:");
        for (Mixer.Info info : mixerInfo) {
            System.out.println(info.getName());
        }//end for loop

        Mixer mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[3]);

        AudioCapture capture = new AudioCapture();
        AudioFormat format = AudioCapture.getAudioFormat(AudioCapture.SampleRates.EIGHT_K,
                AudioCapture.Bits.EIGHT);
        Thread.sleep(5 * 1000);
        System.out.println("Capturing");
        FileOutputStream fos = new FileOutputStream(new File("out.tmp"));
        CaptureThread cThread = capture.capture(format, fos, mixer);
        Thread.sleep(20 * 1000);
        cThread.setStop(true);
        cThread.join();
        fos.close();
        System.out.println("Waiting 15 secs for changing settings");
        Thread.sleep(15 * 1000);
        System.out.println("Playing");
        FileInputStream fis = new FileInputStream(new File("out.tmp"));
        PlayerThread t = capture.playAudio(format, fis, true);
        t.join();
        fis.close();
    }

    private static CaptureThread createRecorder(String mixerName) throws UnsupportedFormatException,
            LineUnavailableException {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        if (null == mixers || 0 == mixers.length) {
            throw new RuntimeException("no mixer found??");
        }
        Mixer.Info mixerInfo = mixers[0];
        logger.log(Level.INFO, "query mixer starting with " + mixerName);
        for (Mixer.Info info : mixers) {
            if (info.getName().startsWith(mixerName)) {
                mixerInfo = info;
            }
        }

        Mixer mixer = AudioSystem.getMixer(mixerInfo);
        AudioCapture capture = new AudioCapture();
        AudioFormat format = AudioCapture.getAudioFormat(AudioCapture.SampleRates.FORTY_FOUR_K,
                AudioCapture.Bits.SIXTEEN);
        return capture.capture(format, null, mixer);
    }

    private static void startServer(int port, String mixerName) throws Exception {
        AudioSocket socket = new AudioSocket(port);
        final CaptureThread capturer = createRecorder(mixerName);
        socket.addEventListener(new AudioSocket.Listener() {
            @Override
            public void onConnected(InputStream is, OutputStream os) {
                // This is the first prototype, we should compress the audio stream before
                // sending.
                capturer.setOutputStream(os);
            }

            @Override
            public void onDisconnected() {
                capturer.setOutputStream(null);
            }
        });
        socket.start();
    }

    private static void testClient(String ip, int port) throws Exception {
        Socket client = new Socket();
        InetSocketAddress address = new InetSocketAddress(ip, port);

        client.connect(address, 30 * 1000); // wait for 30 secs
        PrintStream out = new PrintStream(client.getOutputStream(), true);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            String cmd;
            while (true) {
                System.out.println("Please type your command:");
                cmd = br.readLine();
                if (!"".equals(cmd)) {
                    out.write((cmd + "\r\n").getBytes("UTF-8"));
                    out.flush();
                    System.out.println("Command |" + cmd + "| sent");
                } else {
                    break;
                }
            }
        } finally {
            out.close();
            br.close();
            client.close();
        }
    }

    private static int getPort(String[] args) {
        int port = 13579;
        if (args.length > 2) {
            try {
                port = Integer.parseInt(args[2]);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return port;
    }

    public static void main(String[] args) throws Exception {

        if ("test-recorder".equals(args[0])) {
            testRecorder();
        } else if ("server".equals(args[0])) {
            String mixerName = (args.length > 1) ? args[1] : "";
            startServer(getPort(args), mixerName);
        } else if ("test-client".equals(args[0])) {
            testClient("127.0.0.1", getPort(args));
        }
    }
}
