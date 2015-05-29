package com.miidio.audio.client;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class AudioSocket {

    private static final String HEADER_PASSWORD = "HELO ";
    private static final String TAG = "AudioSocket";
    private Thread connThread;
    private Socket remote;
    private boolean running = false;
    private PCMPlayer player;
    private int bufferSize;
    private ArrayList<KeyInfo> keys = new ArrayList<>();
    private Thread digesterThread;
    private Listener listener;
    private String password;
    private InputStream caFile;
    private TrustManagerFactory trustMgrFactory;

    public AudioSocket(InputStream caFile, String password) {
        this.password = password;
        this.caFile = caFile;
    }

    public void setListener(Listener l) {
        listener = l;
    }

    public void addKeyInfo(String type, int code) {
        synchronized (AudioSocket.this) {
            keys.add(KeyInfo.create(type, code));
        }
    }

    public synchronized void start(final String address, final int port) {
        this.running = true;
        connThread = new Thread(new Runnable() {
            @Override
            public void run() {
                go(address, port);
            }
        });

        connThread.start();
    }

    public synchronized void stop(boolean join) {
        if (!this.running) {
            return;
        }
        this.running = false;

        if (join) {
            try {
                connThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        connThread = null;
    }

    private void go(final String address, final int port) {
        try {
            prepare();
            connect(address, port);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error while playing", e);
        } finally {
            Log.d(TAG, "free resources");
            player.close();
            if (null != remote) {
                if (!remote.isClosed()) {
                    try {
                        Log.d(TAG, "trying to close socket");
                        remote.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                remote = null;
            }
            running = false;
            connThread = null;
            if (null != digesterThread) {
                try {
                    digesterThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // notify listener
            if (null != listener) {
                listener.onDisconnected();
            }
        }
    }

    private void startKeyDigester(final OutputStream out) {
        digesterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedWriter br = new BufferedWriter(new OutputStreamWriter(out));
                // challenge password.
                try {
                    br.write(HEADER_PASSWORD + AudioSocket.this.password);
                    br.newLine();
                    br.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (running) {
                    synchronized (AudioSocket.this) {
                        if (keys.size() > 0) {
                            for (KeyInfo info : keys) {
                                try {
                                    Log.d(TAG, "write command to server: " + info.toString());
                                    br.write(info.toString());
                                    br.newLine();
                                    br.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            keys.clear();
                        }
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        digesterThread.start();
    }

    private void prepare() {
        player = new PCMPlayer();
        bufferSize = player.prepareDevice();
    }

    private Socket createSocket(String address, int port) throws CertificateException, IOException,
            KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        if (null == this.trustMgrFactory) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca;
            try {
                ca = cf.generateCertificate(this.caFile);
            } finally {
                this.caFile.close();
            }
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("ca", ca);

            this.trustMgrFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            this.trustMgrFactory.init(ks);
        }

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, this.trustMgrFactory.getTrustManagers(), null);
        return context.getSocketFactory().createSocket(address, port);
    }

    private void connect(String address, int port) throws IOException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        Log.d(TAG, "Connecting to " + address);
        remote = createSocket(address, port);
        // prepare variables
        int read;
        byte[] buffer = new byte[bufferSize];
        // start to read.
        InputStream input = remote.getInputStream();

        // notify listener
        if (null != listener) {
            listener.onConnected();
        }
        startKeyDigester(remote.getOutputStream());
        // start playing
        while (running) {
            read = input.read(buffer, 0, buffer.length);
            if (read > 0) {
                player.play(buffer, read);
            } else if (read < 0) {
                // remote disconnected.
                break;
            }
        }
    }

    public interface Listener {
        void onConnected();

        void onDisconnected();
    }

    private static class KeyInfo {
        public String type;
        public int keyCode;

        public static KeyInfo create(String type, int keyCode) {
            KeyInfo ret = new KeyInfo();
            ret.type = type;
            ret.keyCode = keyCode;
            return ret;
        }

        @Override
        public String toString() {
            return type + "," + keyCode;
        }
    }
}
