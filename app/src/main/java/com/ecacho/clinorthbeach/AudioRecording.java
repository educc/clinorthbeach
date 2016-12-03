package com.ecacho.clinorthbeach;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import simplesound.pcm.PcmAudioHelper;
import simplesound.pcm.WavAudioFormat;

/**
 * Created by ecacho on 11/27/16.
 */
public class AudioRecording {
    AudioRecord ar = null;
    int buffsize = 0;

    int blockSize = 256;
    boolean isRecording = false;
    private Thread recordingThread = null;
    private String mFilename;
    private Handler mHandler;

    public AudioRecording(String filename, Handler h){
        this.mFilename = filename;
        this.mHandler = h;
    }


    public void start(){
        buffsize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffsize);

        ar.startRecording();

        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    public void stop(){
        ar.stop();
        isRecording = false;
    }

    public File getFileRaw(){
        String dir = new File(this.mFilename).getParent();
        return new File(dir + "/audio.raw");
    }

    public File getFileWav(){
        String dir = new File(this.mFilename).getParent();
        return new File(dir + "/audio.wav");
    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte
        int samplingRate = 44100;
        String filePath = this.getFileRaw().getAbsolutePath();
        short sData[] = new short[buffsize/2];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format

            ar.read(sData, 0, buffsize/2);
            Log.d("eray","Short wirting to file" + sData.toString());
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                byte bData[] = short2byte(sData);
                os.write(bData, 0, buffsize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os.close();

            File fileRaw = getFileRaw();
            File fileWav = getFileWav();
            PcmAudioHelper.convertRawToWav(WavAudioFormat.mono16Bit(samplingRate), fileRaw, fileWav);
            this.mHandler.onSuccess(getDataFromFile(fileWav));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private byte[] getDataFromFile(File file){
        byte[] fileData = new byte[(int) file.length()];
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);
            dis.close();
            return fileData;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    public interface Handler{
        void onSuccess(byte[] data);
    }
}
