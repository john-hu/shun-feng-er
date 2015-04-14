package com.miidio.audio.server;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Main {

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
        System.out.println("Waiting 15 secs for changing settings");
        Thread.sleep(15 * 1000);
        System.out.println("Playing");
        FileInputStream fis = new FileInputStream(new File("out.tmp"));
        PlayerThread t = capture.playAudio(format, fis, true);
        t.join();
    }

    private static void startServer(int port) throws Exception {
        AudioSocket socket = new AudioSocket(port);
        socket.start();
        Thread.sleep(5 * 1000);
        socket.stop();
        socket.join();
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
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
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
            startServer(getPort(args));
        } else if ("test-client".equals(args[0])) {
            testClient("127.0.0.1", getPort(args));
        }
    }
}
