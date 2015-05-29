package com.miidio.audio.server;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import java.io.*;
import java.net.ServerSocket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

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

    private static ServerSocket getServerSocket(int port, String keyPath, String keyPass)
            throws NoSuchAlgorithmException, KeyStoreException, IOException,
            KeyManagementException, CertificateException, UnrecoverableKeyException {

        // we use TLS as default
        SSLContext ctx = SSLContext.getInstance("TLS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");
        char[] passphrase = keyPass.toCharArray();
        ks.load(new FileInputStream(new File(keyPath)), passphrase);
        kmf.init(ks, passphrase);
        ctx.init(kmf.getKeyManagers(), null, null);
        return ctx.getServerSocketFactory().createServerSocket(port);
    }

    private static void startServer(String keyPath, String keyPass, int port, String mixerName,
                                    String password) throws Exception {

        AudioSocket socket = new AudioSocket(getServerSocket(port, keyPath, keyPass), password);
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

    public static void main(String[] args) throws Exception {
        JSAP jsap = new JSAP();
        jsap.registerParameter(new FlaggedOption("mixer")
                .setStringParser(JSAP.STRING_PARSER)
                .setDefault("kuwatec")
                .setShortFlag('m')
                .setLongFlag("mixer")
                .setHelp("The audio loopback driver name. We will use this name to query mixer device."));

        jsap.registerParameter(new FlaggedOption("port")
                .setStringParser(JSAP.INTEGER_PARSER)
                .setDefault("13579")
                .setShortFlag('p')
                .setLongFlag("port")
                .setHelp("server port"));

        jsap.registerParameter(new FlaggedOption("password")
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(false)
                .setShortFlag('P')
                .setLongFlag("password")
                .setHelp("The password to control and listen your computer"));
        jsap.registerParameter(new FlaggedOption("key-path")
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setShortFlag('K')
                .setLongFlag("key-path")
                .setHelp("The path to control and listen your computer"));
        jsap.registerParameter(new FlaggedOption("key-pass")
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setShortFlag('k')
                .setLongFlag("key-pass")
                .setHelp("The password to control and listen your computer"));

        JSAPResult opts = jsap.parse(args);

        if (!opts.success()) {
            System.err.println();
            System.err.println("Usage: java " + Main.class.getName());
            System.err.println("                " + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.exit(1);
        } else if (opts.getInt("port") < 1 || opts.getInt("port") > 65535) {
            System.err.println("port should between 1 and 65535");
            System.exit(2);
        }
        startServer(opts.getString("key-path"),
                opts.getString("key-pass"),
                opts.getInt("port"),
                opts.getString("mixer"),
                opts.getString("password", null));
    }
}
