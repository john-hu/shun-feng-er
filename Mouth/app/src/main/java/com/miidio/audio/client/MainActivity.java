package com.miidio.audio.client;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {
    private final static String CMD_KEYPRESS = "press";
    private final static String CMD_KEYRELEASE = "release";

    private final static String KEY_LAST_CONN_ADDR = "last_conn_addr";

    private Button connectButton;
    private EditText addressText;
    private EditText passwordText;
    private TextView statusLabel;
    private AudioSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.initViews();
    }

    private void initViews() {
        connectButton = (Button) this.findViewById(R.id.button_connect);
        addressText = (EditText) this.findViewById(R.id.text_address);
        passwordText = (EditText) this.findViewById(R.id.text_password);
        statusLabel = (TextView) this.findViewById(R.id.label_status);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == socket) {
                    connect();
                } else {
                    disconnect();
                }
            }
        });

        SharedPreferences setting = getPreferences(MODE_PRIVATE);
        addressText.setText(setting.getString(KEY_LAST_CONN_ADDR, ""));
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void disconnect() {
        connectButton.setEnabled(false);
        socket.stop(false);
    }

    private void connect() {
        if (null == addressText.getText() || "".equals("" + addressText.getText())) {
            statusLabel.setText(R.string.status_error_no_address);
            return;
        }
        connectButton.setEnabled(false);

        String address = addressText.getText().toString();
        int port = 13579;
        if (address.contains(":")) {
            String[] data = address.split(":", 2);
            address = data[0];
            port = Integer.parseInt(data[1]);
        }

        final String finalAddress = address;
        socket = new AudioSocket("" + passwordText.getText());
        socket.setListener(new AudioSocket.Listener() {
            @Override
            public void onConnected() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectButton.setEnabled(true);
                        connectButton.setText(R.string.button_disconnect);
                        statusLabel.setText(String.format(
                                getResources().getString(R.string.status_connected),
                                finalAddress));
                        // A trick to hide keyboard.
                        addressText.setInputType(InputType.TYPE_NULL);
                        addressText.setEnabled(false);
                        passwordText.setInputType(InputType.TYPE_NULL);
                        passwordText.setEnabled(false);
                        passwordText.setText("");
                    }
                });
            }

            @Override
            public void onDisconnected() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectButton.setEnabled(true);
                        statusLabel.setText(R.string.status_disconnected);
                        connectButton.setText(R.string.button_connect);
                        addressText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
                        addressText.setEnabled(true);
                        addressText.requestFocus();
                        passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordText.setEnabled(true);
                        socket = null;
                    }
                });
            }
        });
        socket.start(address, port);
        statusLabel.setText(String.format(
                this.getResources().getString(R.string.status_connecting_to),
                address));

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_LAST_CONN_ADDR, addressText.getText().toString());
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean consumed = false;
        if (null != socket) {
            String type = KeyEvent.ACTION_DOWN == event.getAction() ? CMD_KEYPRESS : CMD_KEYRELEASE;
            int keyCode = PCKeyMapper.get(event.getKeyCode());
            if (keyCode > 0) {
                socket.addKeyInfo(type, keyCode);
                consumed = true;
            } else {
                consumed = false;
            }

        }
        return consumed || super.dispatchKeyEvent(event);
    }
}
