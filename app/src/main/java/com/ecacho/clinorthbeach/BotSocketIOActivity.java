package com.ecacho.clinorthbeach;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecacho.androidrecording.audio.AudioRecordingHandler;
import com.ecacho.androidrecording.audio.AudioRecordingThread;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by ecacho on 11/13/16.
 */
public class BotSocketIOActivity extends Activity
{

    private static final String TAG = "app.Bot";
    private static final String EDUBOT_ENDPOINT = "http://192.168.0.101:8008/";

    private Socket _socket;
    private String _fileRecording;
    private String _clientID;

    private Button _startButton;
    private LinearLayout _chatLayout;
    private AudioRecording _Recorder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot);
        setAudioRecordFile();
        _clientID = "none";

        this._startButton = (Button) findViewById(R.id.button1);
        this._chatLayout = (LinearLayout) findViewById(R.id.layout_chat_content);
        this._Recorder = new AudioRecording(_fileRecording, new AudioRecording.Handler() {
            @Override
            public void onSuccess(byte[] data) {
                Log.i(TAG, "talk to server");
                _socket.emit("talk", _clientID, data);
            }
        });

        final BotSocketIOActivity This = this;
        this._startButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Pressed
                    This.startRecording();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Released
                    This.stopRecording();
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // setup the buttons
        try {
            _socket = IO.socket(EDUBOT_ENDPOINT);
            _socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    //Log.i(TAG,"emit from connect");
                    //_socket.emit("talk", "hi");
                }

            }).on("talk", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i(TAG, "message on talk from server");
                    Log.i(TAG, "length = " + args.length);
                }
            }).on("request", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    if(args.length>0){
                        BotSocketIOActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                TextView v = createViewForUser();
                                v.setText(args[0].toString());
                                _chatLayout.addView(v);
                            }
                        });
                    }
                }
            }).on("response", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    Log.i(TAG, "on response from server");
                    if( args.length > 0){
                        BotSocketIOActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                View v = createViewForMachine(args[0].toString());
                                _chatLayout.addView(v);
                            }
                        });
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {}

            });
            _socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setAudioRecordFile() {
        _fileRecording = new java.io.File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/Filename.xml").getAbsolutePath();
    }

    private void startRecording(){
        Log.i(TAG, "start recording");

        _Recorder.start();
    }

    private void stopRecording() {
        Log.i(TAG, "stopRecording");

        _Recorder.stop();
    }


    private TextView createView(String text){

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int pad = convertDpToPx(5);
        int margin = convertDpToPx(10);
        params.setMargins(0, 0, 0, margin);

        TextView view = new TextView(this);
        view.setPadding(pad,pad,pad,pad);
        view.setLayoutParams(params);
        view.setText(text);
        return view;
    }

    //@SuppressLint("NewApi")
    private TextView createViewForUser(){
        TextView view = createView("");
        view.setTextColor(Color.BLACK);
        view.setBackgroundResource(R.drawable.roundrectangle);
/*
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(this.getDrawable(R.drawable.roundrectangle));
        } else {
            Drawable d = getResources().getDrawable(R.drawable.roundrectangle2, null);
            view.setBackground(d);
        }
        */
        return view;
    }

    @SuppressLint("NewApi")
    private TextView createViewForMachine(String text){
        TextView view = createView(text);
        view.setTextColor(Color.BLACK);
        view.setBackgroundResource(R.drawable.roundrectangle2);

        return view;
    }


    private int convertDpToPx(int dp){
        return Math.round(dp*(getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));

    }

}
