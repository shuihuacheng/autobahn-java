///////////////////////////////////////////////////////////////////////////////
//
//   AutobahnJava - http://crossbar.io/autobahn
//
//   Copyright (c) Crossbar.io Technologies GmbH and contributors
//
//   Licensed under the MIT License.
//   http://www.opensource.org/licenses/mit-license.php
//
///////////////////////////////////////////////////////////////////////////////

package io.crossbar.autobahn.demogallery.android;

import android.util.Log;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.SharedPreferences;

import android.view.Gravity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.crossbar.autobahn.demogallery.R;
import io.crossbar.autobahn.websocket.WebSocket;
import io.crossbar.autobahn.websocket.WebSocketConnection;
import io.crossbar.autobahn.websocket.WebSocketException;
import io.crossbar.autobahn.websocket.WebSocketConnectionHandler;


public class EchoClientActivity extends AppCompatActivity {

    static final String TAG = "io.crossbar.autobahn.echo";
    private static final String PREFS_NAME = "AutobahnAndroidEcho";

    static EditText mHostname;
    static EditText mPort;
    static TextView mStatusline;
    static Button mStart;
    static EditText mMessage;
    static Button mSendMessage;


    private SharedPreferences mSettings;

    private void alert(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    private void loadPrefs() {

        mHostname.setText(mSettings.getString("hostname", "192.168.1.3"));
        mPort.setText(mSettings.getString("port", "9000"));
    }

    private void savePrefs() {

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("hostname", mHostname.getText().toString());
        editor.putString("port", mPort.getText().toString());
        editor.commit();
    }

    private void setButtonConnect() {
        mHostname.setEnabled(true);
        mPort.setEnabled(true);
        mStart.setText("Connect");
        mStart.setOnClickListener(v -> start());
    }

    private void setButtonDisconnect() {
        mHostname.setEnabled(false);
        mPort.setEnabled(false);
        mStart.setText("Disconnect");
        mStart.setOnClickListener(v -> mConnection.disconnect());
    }

    private final WebSocket mConnection = new WebSocketConnection();

    private void start() {

        final String wsuri = "ws://" + mHostname.getText() + ":" + mPort.getText();

        mStatusline.setText("Status: Connecting to " + wsuri + " ..");

        setButtonDisconnect();

        try {
            mConnection.connect(wsuri, new WebSocketConnectionHandler() {
                @Override
                public void onOpen() {
                    mStatusline.setText("Status: Connected to " + wsuri);
                    savePrefs();
                    mSendMessage.setEnabled(true);
                    mMessage.setEnabled(true);
                }

                @Override
                public void onTextMessage(String payload) {
                    alert("Got echo: " + payload);
                }

                @Override
                public void onClose(int code, String reason) {
                    alert("Connection lost.");
                    mStatusline.setText("Status: Ready.");
                    setButtonConnect();
                    mSendMessage.setEnabled(false);
                    mMessage.setEnabled(false);
                }
            });
        } catch (WebSocketException e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_echo_client);

        mHostname = (EditText) findViewById(R.id.hostname);
        mPort = (EditText) findViewById(R.id.port);
        mStatusline = (TextView) findViewById(R.id.statusline);
        mStart = (Button) findViewById(R.id.start);
        mMessage = (EditText) findViewById(R.id.msg);
        mSendMessage = (Button) findViewById(R.id.sendMsg);

        mSettings = getSharedPreferences(PREFS_NAME, 0);
        loadPrefs();

        setButtonConnect();
        mSendMessage.setEnabled(false);
        mMessage.setEnabled(false);

        mSendMessage.setOnClickListener(v -> mConnection.sendTextMessage(mMessage.getText().toString()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnection.isConnected()) {
            mConnection.disconnect();
        }
    }
}
